"""
Life Counter for MTG — Alexa skill
Python 3.12 / ASK SDK v2 rewrite of the original Java 8 skill.

Game state is stored in session attributes under the key "players":
    { "PlayerName": life_total (int), ... }

Player order (insertion order) is preserved via a plain dict (Python 3.7+).
"""

import logging
from ask_sdk_core.skill_builder import SkillBuilder
from ask_sdk_core.dispatch_components import AbstractRequestHandler, AbstractExceptionHandler
from ask_sdk_core.utils import is_intent_name, is_request_type
from ask_sdk_model.ui import SimpleCard
from ask_sdk_model import Response

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

sb = SkillBuilder()

APPLICATION_NAME = "Life Counter for MTG"
DEFAULT_LIFE = 20
MAX_PLAYERS_FOR_SPEECH = 3

NAME_BLACKLIST = {"player", "players"}

COMPLETE_HELP = (
    "Here's some things you can say. Add Brandon, take five life from Brandon, "
    "tell me the life totals, new game, reset, and exit."
)
NEXT_HELP = (
    "You can give a player life, take away a player's life total, add a player, "
    "get the current life totals, or say help. What would you like?"
)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def get_players(handler_input) -> dict[str, int]:
    """Return the players dict from session attributes (may be empty)."""
    attrs = handler_input.attributes_manager.session_attributes
    return attrs.get("players", {})


def save_players(handler_input, players: dict[str, int]) -> None:
    handler_input.attributes_manager.session_attributes["players"] = players


def get_player_name(raw: str | None) -> str | None:
    """Sanitise a slot value into a usable player name."""
    if not raw or not raw.strip():
        return None
    # Take only the first word (Alexa may add extra words)
    name = raw.strip().split()[0].capitalize()
    if name.lower() in NAME_BLACKLIST:
        return None
    return name


def get_life_amount(raw: str | None) -> int | None:
    """Parse the LifeAmount slot into an integer, or None on failure."""
    if not raw:
        return None
    try:
        return int(raw)
    except (ValueError, TypeError):
        return None


def all_totals_speech(players: dict[str, int]) -> str:
    """Build a speech string listing every player's current life total."""
    if not players:
        return "Nobody has joined the game."
    sorted_players = sorted(players.items(), key=lambda kv: (-kv[1], kv[0]))
    parts = [f"{name} has {life} life" for name, life in sorted_players]
    if len(parts) == 1:
        return parts[0] + "."
    return ", ".join(parts[:-1]) + " and " + parts[-1] + "."


def leaderboard_card_text(players: dict[str, int]) -> str:
    sorted_players = sorted(players.items(), key=lambda kv: (-kv[1], kv[0]))
    lines = [f"No. {i + 1} - {name} : {life}" for i, (name, life) in enumerate(sorted_players)]
    return "\n".join(lines)


def ask_response(handler_input, speech: str, reprompt: str, card_title: str = "Session") -> Response:
    return (
        handler_input.response_builder
        .speak(speech)
        .ask(reprompt)
        .set_card(SimpleCard(card_title, speech))
        .response
    )


def tell_response(handler_input, speech: str, card_title: str = "Session", card_text: str | None = None) -> Response:
    return (
        handler_input.response_builder
        .speak(speech)
        .set_should_end_session(True)
        .set_card(SimpleCard(card_title, card_text or speech))
        .response
    )


# ---------------------------------------------------------------------------
# Request handlers
# ---------------------------------------------------------------------------

class LaunchRequestHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_request_type("LaunchRequest")(handler_input)

    def handle(self, handler_input):
        players = get_players(handler_input)
        if not players:
            speech = f"{APPLICATION_NAME}. Let's start your game. Who's your first player?"
            reprompt = "Please tell me who is your first player?"
        else:
            count = len(players)
            word = "player" if count == 1 else "players"
            speech = (
                f"{APPLICATION_NAME}, you have {count} {word} in the game. "
                "You can add or subtract life from a player, add another player, "
                "reset all players or exit. Which would you like?"
            )
            reprompt = COMPLETE_HELP
        return ask_response(handler_input, speech, reprompt)


class NewGameIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_intent_name("NewGameIntent")(handler_input)

    def handle(self, handler_input):
        players = get_players(handler_input)
        if not players:
            # No existing players — fresh start
            save_players(handler_input, {})
            return ask_response(
                handler_input,
                "New game started. Who's your first player?",
                "Please tell me who's your first player?",
            )

        # Reset all existing players to DEFAULT_LIFE
        reset = {name: DEFAULT_LIFE for name in players}
        save_players(handler_input, reset)
        count = len(reset)
        word = "player" if count == 1 else "players"
        speech = f"New game started with {count} existing {word}."
        return tell_response(handler_input, speech)


class AddPlayerIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_intent_name("AddPlayerIntent")(handler_input)

    def handle(self, handler_input):
        slots = handler_input.request_envelope.request.intent.slots
        raw_name = slots.get("PlayerName") and slots["PlayerName"].value
        player_name = get_player_name(raw_name)

        if not player_name:
            speech = "OK. Who do you want to add?"
            return ask_response(handler_input, speech, speech)

        players = get_players(handler_input)
        players[player_name] = DEFAULT_LIFE
        save_players(handler_input, players)

        count = len(players)
        if count == 1:
            speech = (
                f"{player_name} has joined your game. "
                "You can say, I am done adding players. Now who's your next player?"
            )
        else:
            speech = f"{player_name} has joined your game. Who is your next player?"

        return ask_response(handler_input, speech, NEXT_HELP)


class AddLifeIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_intent_name("AddLifeIntent")(handler_input)

    def handle(self, handler_input):
        slots = handler_input.request_envelope.request.intent.slots
        raw_name = slots.get("PlayerName") and slots["PlayerName"].value
        player_name = get_player_name(raw_name)

        if not player_name:
            speech = "Sorry, I did not hear the player name. Please say again?"
            return ask_response(handler_input, speech, speech)

        raw_amount = slots.get("LifeAmount") and slots["LifeAmount"].value
        amount = get_life_amount(raw_amount)
        if amount is None:
            speech = "Sorry, I did not hear the amount of life. Please say again?"
            return ask_response(handler_input, speech, speech)

        players = get_players(handler_input)
        if not players:
            return tell_response(
                handler_input,
                "A game has not been started. Please say New Game to start a new game before adding life.",
            )

        if player_name not in players:
            speech = f"Sorry, {player_name} has not joined the game. What else?"
            return ask_response(handler_input, speech, speech)

        players[player_name] += amount
        save_players(handler_input, players)

        if len(players) > MAX_PLAYERS_FOR_SPEECH:
            speech = f"{amount} for {player_name}. {player_name} has {players[player_name]} in total."
        else:
            speech = f"{amount} for {player_name}. {all_totals_speech(players)}"

        return tell_response(handler_input, speech)


class SubLifeIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_intent_name("SubLifeIntent")(handler_input)

    def handle(self, handler_input):
        slots = handler_input.request_envelope.request.intent.slots
        raw_name = slots.get("PlayerName") and slots["PlayerName"].value
        player_name = get_player_name(raw_name)

        if not player_name:
            speech = "Sorry, I did not hear the player name. Please say again?"
            return ask_response(handler_input, speech, speech)

        raw_amount = slots.get("LifeAmount") and slots["LifeAmount"].value
        amount = get_life_amount(raw_amount)
        if amount is None:
            speech = "Sorry, I did not hear the amount of life. Please say again?"
            return ask_response(handler_input, speech, speech)

        players = get_players(handler_input)
        if not players:
            return tell_response(
                handler_input,
                "A game has not been started. Please say New Game to start a new game before subtracting life.",
            )

        if player_name not in players:
            speech = f"Sorry, {player_name} has not joined the game. What else?"
            return ask_response(handler_input, speech, speech)

        players[player_name] -= amount
        save_players(handler_input, players)

        # Check for game-over condition
        if players[player_name] <= 0:
            remaining = {n: l for n, l in players.items() if n != player_name}
            if len(remaining) == 1:
                winner = next(iter(remaining))
                speech = f"Player {winner} has won the game!"
                return tell_response(handler_input, speech)

        if len(players) > MAX_PLAYERS_FOR_SPEECH:
            speech = f"{amount} from {player_name}. {player_name} has {players[player_name]} in total."
        else:
            speech = f"{amount} from {player_name}. {all_totals_speech(players)}"

        return tell_response(handler_input, speech)


class TellLifeTotalsIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_intent_name("TellLifeTotalsIntent")(handler_input)

    def handle(self, handler_input):
        players = get_players(handler_input)
        if not players:
            return tell_response(handler_input, "Nobody has joined the game.")

        speech = all_totals_speech(players)
        card_text = leaderboard_card_text(players)
        return tell_response(handler_input, speech, card_title="Leaderboard", card_text=card_text)


class ResetPlayersIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_intent_name("ResetPlayersIntent")(handler_input)

    def handle(self, handler_input):
        save_players(handler_input, {})
        speech = "New game started without players. Who do you want to add first?"
        return ask_response(handler_input, speech, speech)


class HelpIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_intent_name("AMAZON.HelpIntent")(handler_input)

    def handle(self, handler_input):
        speech = COMPLETE_HELP + " So, how can I help?"
        return ask_response(handler_input, speech, NEXT_HELP)


class CancelAndStopIntentHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return (
            is_intent_name("AMAZON.CancelIntent")(handler_input)
            or is_intent_name("AMAZON.StopIntent")(handler_input)
        )

    def handle(self, handler_input):
        speech = "Okay. Whenever you're ready, you can start adding or subtracting life from the players in your game."
        return tell_response(handler_input, speech)


class SessionEndedRequestHandler(AbstractRequestHandler):
    def can_handle(self, handler_input):
        return is_request_type("SessionEndedRequest")(handler_input)

    def handle(self, handler_input):
        logger.info("Session ended: %s", handler_input.request_envelope.request.reason)
        return handler_input.response_builder.response


class CatchAllExceptionHandler(AbstractExceptionHandler):
    def can_handle(self, handler_input, exception):
        return True

    def handle(self, handler_input, exception):
        logger.exception("Unhandled exception", exc_info=exception)
        speech = "Sorry, I had trouble doing what you asked. Please try again."
        return ask_response(handler_input, speech, speech)


# ---------------------------------------------------------------------------
# Skill assembly
# ---------------------------------------------------------------------------

sb.add_request_handler(LaunchRequestHandler())
sb.add_request_handler(NewGameIntentHandler())
sb.add_request_handler(AddPlayerIntentHandler())
sb.add_request_handler(AddLifeIntentHandler())
sb.add_request_handler(SubLifeIntentHandler())
sb.add_request_handler(TellLifeTotalsIntentHandler())
sb.add_request_handler(ResetPlayersIntentHandler())
sb.add_request_handler(HelpIntentHandler())
sb.add_request_handler(CancelAndStopIntentHandler())
sb.add_request_handler(SessionEndedRequestHandler())
sb.add_exception_handler(CatchAllExceptionHandler())

lambda_handler = sb.lambda_handler()
