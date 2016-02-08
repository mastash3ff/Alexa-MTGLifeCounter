/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Brandon Sheffield
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package mtglifecounter;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.Card;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import mtglifecounter.storage.MtgLifeCounterDao;
import mtglifecounter.storage.MtgLifeCounterDynamoDbClient;
import mtglifecounter.storage.MtgLifeCounterGame;
import mtglifecounter.storage.MtgLifeCounterGameData;

/**
 * The {@link MtgLifeCounterManager} receives various events and intents and manages the flow of the
 * game.
 */
public class MtgLifeCounterManager {
    /**
     * Intent slot for player name.
     */
    private static final String SLOT_PLAYER_NAME = "PlayerName";

    /**
     * Intent slot for player score.
     */
    private static final String SLOT_SCORE_NUMBER = "ScoreNumber";

    /**
     * Maximum number of players for which scores must be announced while adding a score.
     */
    private static final int MAX_PLAYERS_FOR_SPEECH = 3;

    private final MtgLifeCounterDao scoreKeeperDao;

    public MtgLifeCounterManager(final AmazonDynamoDBClient amazonDynamoDbClient) {
        MtgLifeCounterDynamoDbClient dynamoDbClient =
                new MtgLifeCounterDynamoDbClient(amazonDynamoDbClient);
        scoreKeeperDao = new MtgLifeCounterDao(dynamoDbClient);
    }

    /**
     * Creates and returns response for Launch request.
     *
     * @param request
     *            {@link LaunchRequest} for this request
     * @param session
     *            Speechlet {@link Session} for this request
     * @return response for launch request
     */
    public SpeechletResponse getLaunchResponse(LaunchRequest request, Session session) {
        // Speak welcome message and ask user questions
        // based on whether there are players or not.
        String speechText, repromptText;
        MtgLifeCounterGame game = scoreKeeperDao.getMTGLifeCounterGame(session);

        if (game == null || !game.hasPlayers()) {
            speechText = "MTGLifeCounter, Let's start your game. Who's your first player?";
            repromptText = "Please tell me who is your first player?";
        } else if (!game.hasScores()) {
            speechText =
                    "MTGLifeCounter, you have " + game.getNumberOfPlayers()
                            + (game.getNumberOfPlayers() == 1 ? " player" : " players")
                            + " in the game. You can give a player points, add another player,"
                            + " reset all players or exit. Which would you like?";
            repromptText = MtgLifeCounterTextUtil.COMPLETE_HELP;
        } else {
            speechText = "MTGLifeCounter, What can I do for you?";
            repromptText = MtgLifeCounterTextUtil.NEXT_HELP;
        }

        return getAskSpeechletResponse(speechText, repromptText);
    }

    /**
     * Creates and returns response for the new game intent.
     *
     * @param session
     *            {@link Session} for the request
     * @param skillContext
     *            {@link MtgLifeCounterSkillContext} for this request
     * @return response for the new game intent.
     */
    public SpeechletResponse getNewGameIntentResponse(Session session, MtgLifeCounterSkillContext skillContext) {
        MtgLifeCounterGame game = scoreKeeperDao.getMTGLifeCounterGame(session);

        if (game == null) {
            return getAskSpeechletResponse("New game started. Who's your first player?",
                    "Please tell me who\'s your first player?");
        }

        // Reset current game
        game.resetScores(20); //default is 20
        scoreKeeperDao.saveMTGLifeCounterGame(game);

        String speechText =
                "New game started with " + game.getNumberOfPlayers() + " existing player"
                        + (game.getNumberOfPlayers() != 1 ? "" : "s") + ".";

        if (skillContext.needsMoreHelp()) {
            String repromptText =
                    "You can give a player points, add another player, reset all players or "
                            + "exit. What would you like?";
            speechText += repromptText;
            return getAskSpeechletResponse(speechText, repromptText);
        } else {
            return getTellSpeechletResponse(speechText);
        }
    }

    /**
     * Creates and returns response for the add player intent.
     *
     * @param intent
     *            {@link Intent} for this request
     * @param session
     *            Speechlet {@link Session} for this request
     * @param skillContext
     * @return response for the add player intent.
     */
    public SpeechletResponse getAddPlayerIntentResponse(Intent intent, Session session,
            MtgLifeCounterSkillContext skillContext) {
        // add a player to the current game,
        // terminate or continue the conversation based on whether the intent
        // is from a one shot command or not.
        String newPlayerName =
                MtgLifeCounterTextUtil.getPlayerName(intent.getSlot(SLOT_PLAYER_NAME).getValue());
        if (newPlayerName == null) {
            String speechText = "OK. Who do you want to add?";
            return getAskSpeechletResponse(speechText, speechText);
        }

        // Load the previous game
        MtgLifeCounterGame game = scoreKeeperDao.getMTGLifeCounterGame(session);
        if (game == null) {
            game = MtgLifeCounterGame.newInstance(session, MtgLifeCounterGameData.newInstance());
        }

        game.addPlayer(newPlayerName);

        // Save the updated game
        scoreKeeperDao.saveMTGLifeCounterGame(game);

        String speechText = newPlayerName + " has joined your game. ";
        String repromptText = null;

        if (skillContext.needsMoreHelp()) {
            if (game.getNumberOfPlayers() == 1) {
                speechText += "You can say, I am done adding players. Now who's your next player?";

            } else {
                speechText += "Who is your next player?";
            }
            repromptText = MtgLifeCounterTextUtil.NEXT_HELP;
        }

        if (repromptText != null) {
            return getAskSpeechletResponse(speechText, repromptText);
        } else {
            return getTellSpeechletResponse(speechText);
        }
    }

    /**
     * Creates and returns response for the add score intent.
     *
     * @param intent
     *            {@link Intent} for this request
     * @param session
     *            {@link Session} for this request
     * @param skillContext
     *            {@link MtgLifeCounterSkillContext} for this request
     * @return response for the add score intent
     */
    public SpeechletResponse getAddScoreIntentResponse(Intent intent, Session session,
            MtgLifeCounterSkillContext skillContext) {
        String playerName =
                MtgLifeCounterTextUtil.getPlayerName(intent.getSlot(SLOT_PLAYER_NAME).getValue());
        if (playerName == null) {
            String speechText = "Sorry, I did not hear the player name. Please say again?";
            return getAskSpeechletResponse(speechText, speechText);
        }

        int score = 0;
        try {
            score = Integer.parseInt(intent.getSlot(SLOT_SCORE_NUMBER).getValue());
        } catch (NumberFormatException e) {
            String speechText = "Sorry, I did not hear the points. Please say again?";
            return getAskSpeechletResponse(speechText, speechText);
        }

        MtgLifeCounterGame game = scoreKeeperDao.getMTGLifeCounterGame(session);
        if (game == null) {
            return getTellSpeechletResponse("A game has not been started. Please say New Game to "
                    + "start a new game before adding scores.");
        }

        if (game.getNumberOfPlayers() == 0) {
            String speechText = "Sorry, no player has joined the game yet. What can I do for you?";
            return getAskSpeechletResponse(speechText, speechText);
        }

        // Update score
        if (!game.addScoreForPlayer(playerName, score)) {
            String speechText = "Sorry, " + playerName + " has not joined the game. What else?";
            return getAskSpeechletResponse(speechText, speechText);
        }

        // Save game
        scoreKeeperDao.saveMTGLifeCounterGame(game);

        // Prepare speech text. If the game has less than 3 players, skip reading scores for each
        // player for brevity.
        String speechText = score + " for " + playerName + ". ";
        if (game.getNumberOfPlayers() > MAX_PLAYERS_FOR_SPEECH) {
            speechText += playerName + " has " + game.getScoreForPlayer(playerName) + " in total.";
        } else {
            speechText += getAllScoresAsSpeechText(game.getAllScoresInDescndingOrder());
        }

        return getTellSpeechletResponse(speechText);
    }

    /**
     * Creates and returns response for the tell scores intent.
     *
     * @param intent
     *            {@link Intent} for this request
     * @param session
     *            {@link Session} for this request
     * @return response for the tell scores intent
     */
    public SpeechletResponse getTellScoresIntentResponse(Intent intent, Session session) {
        // tells the scores in the leaderboard and send the result in card.
        MtgLifeCounterGame game = scoreKeeperDao.getMTGLifeCounterGame(session);

        if (game == null || !game.hasPlayers()) {
            return getTellSpeechletResponse("Nobody has joined the game.");
        }

        SortedMap<String, Long> sortedScores = game.getAllScoresInDescndingOrder();
        String speechText = getAllScoresAsSpeechText(sortedScores);
        Card leaderboardScoreCard = getLeaderboardScoreCard(sortedScores);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, leaderboardScoreCard);
    }

    /**
     * Creates and returns response for the reset players intent.
     *
     * @param intent
     *            {@link Intent} for this request
     * @param session
     *            {@link Session} for this request
     * @return response for the reset players intent
     */
    public SpeechletResponse getResetPlayersIntentResponse(Intent intent, Session session) {
        // Remove all players
        MtgLifeCounterGame game =
                MtgLifeCounterGame.newInstance(session, MtgLifeCounterGameData.newInstance());
        scoreKeeperDao.saveMTGLifeCounterGame(game);

        String speechText = "New game started without players. Who do you want to add first?";
        return getAskSpeechletResponse(speechText, speechText);
    }

    /**
     * Creates and returns response for the help intent.
     *
     * @param intent
     *            {@link Intent} for this request
     * @param session
     *            {@link Session} for this request
     * @param skillContext
     *            {@link MtgLifeCounterSkillContext} for this request
     * @return response for the help intent
     */
    public SpeechletResponse getHelpIntentResponse(Intent intent, Session session,
            MtgLifeCounterSkillContext skillContext) {
        return skillContext.needsMoreHelp() ? getAskSpeechletResponse(
                MtgLifeCounterTextUtil.COMPLETE_HELP + " So, how can I help?",
                MtgLifeCounterTextUtil.NEXT_HELP)
                : getTellSpeechletResponse(MtgLifeCounterTextUtil.COMPLETE_HELP);
    }

    /**
     * Creates and returns response for the exit intent.
     *
     * @param intent
     *            {@link Intent} for this request
     * @param session
     *            {@link Session} for this request
     * @param skillContext
     *            {@link MtgLifeCounterSkillContext} for this request
     * @return response for the exit intent
     */
    public SpeechletResponse getExitIntentResponse(Intent intent, Session session,
            MtgLifeCounterSkillContext skillContext) {
        return skillContext.needsMoreHelp() ? getTellSpeechletResponse("Okay. Whenever you're "
                + "ready, you can start giving points to the players in your game.")
                : getTellSpeechletResponse("");
    }

    /**
     * Returns an ask Speechlet response for a speech and reprompt text.
     *
     * @param speechText
     *            Text for speech output
     * @param repromptText
     *            Text for reprompt output
     * @return ask Speechlet response for a speech and reprompt text
     */
    private SpeechletResponse getAskSpeechletResponse(String speechText, String repromptText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
        repromptSpeech.setText(repromptText);
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptSpeech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Returns a tell Speechlet response for a speech and reprompt text.
     *
     * @param speechText
     *            Text for speech output
     * @return a tell Speechlet response for a speech and reprompt text
     */
    private SpeechletResponse getTellSpeechletResponse(String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Converts a {@link Map} of scores into text for speech. The order of the entries in the text
     * is determined by the order of entries in {@link Map#entrySet()}.
     *
     * @param scores
     *            A {@link Map} of scores
     * @return a speech ready text containing scores
     */
    private String getAllScoresAsSpeechText(Map<String, Long> scores) {
        StringBuilder speechText = new StringBuilder();
        int index = 0;
        for (Entry<String, Long> entry : scores.entrySet()) {
            if (scores.size() > 1 && index == scores.size() - 1) {
                speechText.append(" and ");
            }
            String singularOrPluralPoints = entry.getValue() == 1 ? " point, " : " points, ";
            speechText
                    .append(entry.getKey())
                    .append(" has ")
                    .append(entry.getValue())
                    .append(singularOrPluralPoints);
            index++;
        }

        return speechText.toString();
    }

    /**
     * Creates and returns a {@link Card} with a formatted text containing all scores in the game.
     * The order of the entries in the text is determined by the order of entries in
     * {@link Map#entrySet()}.
     *
     * @param scores
     *            A {@link Map} of scores
     * @return leaderboard text containing all scores in the game
     */
    private Card getLeaderboardScoreCard(Map<String, Long> scores) {
        StringBuilder leaderboard = new StringBuilder();
        int index = 0;
        for (Entry<String, Long> entry : scores.entrySet()) {
            index++;
            leaderboard
                    .append("No. ")
                    .append(index)
                    .append(" - ")
                    .append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue())
                    .append("\n");
        }

        SimpleCard card = new SimpleCard();
        card.setTitle("Leaderboard");
        card.setContent(leaderboard.toString());
        return card;
    }

    /**
     * Creates and returns response for the subtract score intent.
     *
     * @param intent
     *            {@link Intent} for this request
     * @param session
     *            {@link Session} for this request
     * @param skillContext
     *            {@link MtgLifeCounterSkillContext} for this request
     * @return response for the add score intent
     */
	public SpeechletResponse getSubScoreIntentResponse(Intent intent, Session session, MtgLifeCounterSkillContext skillContext) {
        String playerName =
                MtgLifeCounterTextUtil.getPlayerName(intent.getSlot(SLOT_PLAYER_NAME).getValue());
        if (playerName == null) {
            String speechText = "Sorry, I did not hear the player name. Please say again?";
            return getAskSpeechletResponse(speechText, speechText);
        }

        int score = 0;
        try {
            score = Integer.parseInt(intent.getSlot(SLOT_SCORE_NUMBER).getValue());
        } catch (NumberFormatException e) {
            String speechText = "Sorry, I did not hear the points. Please say again?";
            return getAskSpeechletResponse(speechText, speechText);
        }

        MtgLifeCounterGame game = scoreKeeperDao.getMTGLifeCounterGame(session);
        if (game == null) {
            return getTellSpeechletResponse("A game has not been started. Please say New Game to "
                    + "start a new game before subtracting scores.");
        }

        if (game.getNumberOfPlayers() == 0) {
            String speechText = "Sorry, no player has joined the game yet. What can I do for you?";
            return getAskSpeechletResponse(speechText, speechText);
        }

        // Update scores by subtracting
        if (!game.subtractScoreForPlayer(playerName, score)) {
            String speechText = "Sorry, " + playerName + " has not joined the game. What else?";
            return getAskSpeechletResponse(speechText, speechText);
        }
        
        // Save game
        scoreKeeperDao.saveMTGLifeCounterGame(game);
        
        //check for winner & loser
        if (game.didPlayerLose(playerName,score)){
        	//if that player is at <= 0 life total.
        	//TODO implement multiple player logic.  Currently supports one.
        	
        	SortedMap<String, Long> allScoresInDescndingOrder = game.getAllScoresInDescndingOrder();
        	allScoresInDescndingOrder.remove(playerName); //remove player from collection of players
        	
        	if (allScoresInDescndingOrder.size() == 1){
        		String lastPlayer = allScoresInDescndingOrder.lastKey();
        		String speechText = "Player " + lastPlayer + " has won the game!";
        		return getTellSpeechletResponse(speechText);
        	}
        }

        // Prepare speech text. If the game has less than 3 players, skip reading scores for each
        // player for brevity.
        String speechText = score + " from " + playerName + ". ";
        if (game.getNumberOfPlayers() > MAX_PLAYERS_FOR_SPEECH) {
            speechText += playerName + " has " + game.getScoreForPlayer(playerName) + " in total.";
        } else {
            speechText += getAllScoresAsSpeechText(game.getAllScoresInDescndingOrder());
        }

        return getTellSpeechletResponse(speechText);
	}
}
