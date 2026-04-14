"""Tests for Life Counter for MTG Alexa skill."""
from unittest.mock import MagicMock
from ask_sdk_model import LaunchRequest, IntentRequest, Intent, Slot

import lambda_function as lf

DEFAULT_LIFE = lf.DEFAULT_LIFE  # 20


def make_hi(request, session_attrs=None):
    hi = MagicMock()
    hi.request_envelope.request = request
    hi.attributes_manager.session_attributes = {} if session_attrs is None else dict(session_attrs)
    rb = MagicMock()
    for m in ("speak", "ask", "set_card", "set_should_end_session"):
        getattr(rb, m).return_value = rb
    hi.response_builder = rb
    return hi


def make_intent(name, slots=None):
    slot_objs = {k: Slot(name=k, value=str(v)) for k, v in (slots or {}).items()}
    return IntentRequest(intent=Intent(name=name, slots=slot_objs))


class TestHelpers:
    def test_get_player_name_capitalizes(self):
        assert lf.get_player_name("alice") == "Alice"

    def test_get_player_name_first_word_only(self):
        assert lf.get_player_name("alice and bob") == "Alice"

    def test_get_player_name_blacklisted(self):
        assert lf.get_player_name("player") is None
        assert lf.get_player_name("players") is None

    def test_get_player_name_none(self):
        assert lf.get_player_name(None) is None

    def test_get_player_name_empty(self):
        assert lf.get_player_name("") is None

    def test_get_life_amount_valid(self):
        assert lf.get_life_amount("5") == 5

    def test_get_life_amount_none(self):
        assert lf.get_life_amount(None) is None

    def test_get_life_amount_invalid(self):
        assert lf.get_life_amount("not a number") is None

    def test_all_totals_speech_empty(self):
        assert "Nobody" in lf.all_totals_speech({})

    def test_all_totals_speech_one_player(self):
        speech = lf.all_totals_speech({"Alice": 20})
        assert "Alice" in speech and "20" in speech

    def test_all_totals_speech_sorted_by_life_descending(self):
        speech = lf.all_totals_speech({"Alice": 10, "Bob": 20})
        assert speech.index("Bob") < speech.index("Alice")


class TestLaunchRequest:
    def test_can_handle(self):
        assert lf.LaunchRequestHandler().can_handle(make_hi(LaunchRequest()))

    def test_no_players_asks_for_first_player(self):
        hi = make_hi(LaunchRequest())
        lf.LaunchRequestHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "first player" in speech.lower()

    def test_existing_players_shows_count(self):
        hi = make_hi(LaunchRequest(), session_attrs={"players": {"Alice": 20, "Bob": 15}})
        lf.LaunchRequestHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "2" in speech

    def test_keeps_session_open(self):
        hi = make_hi(LaunchRequest())
        lf.LaunchRequestHandler().handle(hi)
        hi.response_builder.ask.assert_called_once()


class TestAddPlayerIntent:
    def test_can_handle(self):
        assert lf.AddPlayerIntentHandler().can_handle(make_hi(make_intent("AddPlayerIntent")))

    def test_adds_player_with_default_life(self):
        hi = make_hi(make_intent("AddPlayerIntent", slots={"PlayerName": "Alice"}))
        lf.AddPlayerIntentHandler().handle(hi)
        players = hi.attributes_manager.session_attributes["players"]
        assert "Alice" in players
        assert players["Alice"] == DEFAULT_LIFE

    def test_no_name_prompts_for_name(self):
        hi = make_hi(make_intent("AddPlayerIntent"))
        lf.AddPlayerIntentHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "add" in speech.lower()

    def test_confirms_player_joined(self):
        hi = make_hi(make_intent("AddPlayerIntent", slots={"PlayerName": "Bob"}))
        lf.AddPlayerIntentHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "Bob" in speech


class TestAddLifeIntent:
    def test_can_handle(self):
        assert lf.AddLifeIntentHandler().can_handle(make_hi(make_intent("AddLifeIntent")))

    def test_adds_life_to_player(self):
        attrs = {"players": {"Alice": 20}}
        hi = make_hi(
            make_intent("AddLifeIntent", slots={"PlayerName": "Alice", "LifeAmount": "5"}),
            session_attrs=attrs,
        )
        lf.AddLifeIntentHandler().handle(hi)
        assert hi.attributes_manager.session_attributes["players"]["Alice"] == 25

    def test_unknown_player_returns_error(self):
        attrs = {"players": {"Bob": 20}}
        hi = make_hi(
            make_intent("AddLifeIntent", slots={"PlayerName": "Alice", "LifeAmount": "5"}),
            session_attrs=attrs,
        )
        lf.AddLifeIntentHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "not joined" in speech.lower()

    def test_no_game_returns_error(self):
        hi = make_hi(
            make_intent("AddLifeIntent", slots={"PlayerName": "Alice", "LifeAmount": "5"})
        )
        lf.AddLifeIntentHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "not been started" in speech.lower()


class TestSubLifeIntent:
    def test_can_handle(self):
        assert lf.SubLifeIntentHandler().can_handle(make_hi(make_intent("SubLifeIntent")))

    def test_subtracts_life_from_player(self):
        attrs = {"players": {"Alice": 20, "Bob": 20}}
        hi = make_hi(
            make_intent("SubLifeIntent", slots={"PlayerName": "Alice", "LifeAmount": "3"}),
            session_attrs=attrs,
        )
        lf.SubLifeIntentHandler().handle(hi)
        assert hi.attributes_manager.session_attributes["players"]["Alice"] == 17

    def test_win_condition_announced(self):
        attrs = {"players": {"Alice": 1, "Bob": 20}}
        hi = make_hi(
            make_intent("SubLifeIntent", slots={"PlayerName": "Alice", "LifeAmount": "1"}),
            session_attrs=attrs,
        )
        lf.SubLifeIntentHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "Bob" in speech and "won" in speech


class TestTellLifeTotalsIntent:
    def test_can_handle(self):
        assert lf.TellLifeTotalsIntentHandler().can_handle(
            make_hi(make_intent("TellLifeTotalsIntent"))
        )

    def test_lists_all_players(self):
        attrs = {"players": {"Alice": 18, "Bob": 15}}
        hi = make_hi(make_intent("TellLifeTotalsIntent"), session_attrs=attrs)
        lf.TellLifeTotalsIntentHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "Alice" in speech and "Bob" in speech

    def test_no_players_says_nobody(self):
        hi = make_hi(make_intent("TellLifeTotalsIntent"))
        lf.TellLifeTotalsIntentHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "Nobody" in speech


class TestNewGameIntent:
    def test_can_handle(self):
        assert lf.NewGameIntentHandler().can_handle(make_hi(make_intent("NewGameIntent")))

    def test_resets_existing_players_to_default_life(self):
        attrs = {"players": {"Alice": 5, "Bob": 3}}
        hi = make_hi(make_intent("NewGameIntent"), session_attrs=attrs)
        lf.NewGameIntentHandler().handle(hi)
        players = hi.attributes_manager.session_attributes["players"]
        assert players["Alice"] == DEFAULT_LIFE
        assert players["Bob"] == DEFAULT_LIFE

    def test_no_players_asks_for_first(self):
        hi = make_hi(make_intent("NewGameIntent"))
        lf.NewGameIntentHandler().handle(hi)
        speech = hi.response_builder.speak.call_args[0][0]
        assert "first player" in speech.lower()


class TestResetPlayersIntent:
    def test_can_handle(self):
        assert lf.ResetPlayersIntentHandler().can_handle(
            make_hi(make_intent("ResetPlayersIntent"))
        )

    def test_clears_all_players(self):
        attrs = {"players": {"Alice": 20, "Bob": 15}}
        hi = make_hi(make_intent("ResetPlayersIntent"), session_attrs=attrs)
        lf.ResetPlayersIntentHandler().handle(hi)
        assert hi.attributes_manager.session_attributes["players"] == {}
