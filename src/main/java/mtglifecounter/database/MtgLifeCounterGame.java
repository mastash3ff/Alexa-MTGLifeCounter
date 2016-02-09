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
package mtglifecounter.database;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.amazon.speech.speechlet.Session;

/**
 * Represents a score keeper game.
 */
public final class MtgLifeCounterGame {
    private Session session;
    private MtgLifeCounterGameData gameData;

    private MtgLifeCounterGame() {
    }

    /**
     * Creates a new instance of {@link MtgLifeCounterGame} with the provided {@link Session} and
     * {@link MtgLifeCounterGameData}.
     * <p>
     * To create a new instance of {@link MtgLifeCounterGameData}, see
     * {@link MtgLifeCounterGameData#newInstance()}
     * 
     * @param session
     * @param gameData
     * @return
     * @see MtgLifeCounterGameData#newInstance()
     */
    public static MtgLifeCounterGame newInstance(Session session, MtgLifeCounterGameData gameData) {
        MtgLifeCounterGame game = new MtgLifeCounterGame();
        game.setSession(session);
        game.setGameData(gameData);
        return game;
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    protected Session getSession() {
        return session;
    }

    protected MtgLifeCounterGameData getGameData() {
        return gameData;
    }

    protected void setGameData(MtgLifeCounterGameData gameData) {
        this.gameData = gameData;
    }

    /**
     * Returns true if the game has any players, false otherwise.
     * 
     * @return true if the game has any players, false otherwise
     */
    public boolean hasPlayers() {
        return !gameData.getPlayers().isEmpty();
    }

    /**
     * Returns the number of players in the game.
     * 
     * @return the number of players in the game
     */
    public int getNumberOfPlayers() {
        return gameData.getPlayers().size();
    }

    /**
     * Add a player to the game.
     * 
     * @param playerName
     *            Name of the player
     */
    public void addPlayer(String playerName) {
        gameData.getPlayers().add(playerName);
    }

    /**
     * Returns true if the player exists in the game, false otherwise.
     * 
     * @param playerName
     *            Name of the player
     * @return true if the player exists in the game, false otherwise
     */
    public boolean hasPlayer(String playerName) {
        return gameData.getPlayers().contains(playerName);
    }

    /**
     * Returns true if the game has any scores listed, false otherwise.
     * 
     * @return true if the game has any scores listed, false otherwise
     */
    public boolean hasScores() {
        return !gameData.getScores().isEmpty();
    }

    /**
     * Returns the score for a player.
     * 
     * @param playerName
     *            Name of the player
     * @return score for a player
     */
    public long getScoreForPlayer(String playerName) {
        return gameData.getScores().get(playerName);
    }

    /**
     * Adds the score passed to it to the current score for a player. Returns true if the player
     * existed, false otherwise.
     * 
     * @param playerName
     *            Name of the player
     * @param score
     *            score to be added
     * @return true if the player existed, false otherwise.
     */
    public boolean addScoreForPlayer(String playerName, long score) {
        if (!hasPlayer(playerName)) {
            return false;
        }

        long currentScore = 0L;
        if (gameData.getScores().containsKey(playerName)) {
            currentScore = gameData.getScores().get(playerName);
        }

        gameData.getScores().put(playerName, Long.valueOf(currentScore + score));
        return true;
    }
    
    /**
     * Subtracts the score passed to it to the current score for a player. Returns true if the player
     * existed, false otherwise.
     * 
     * @param playerName
     *            Name of the player
     * @param score
     *            score to be added
     * @return true if the player existed, false otherwise.
     */
    public boolean subtractScoreForPlayer(String playerName, long score) {
        if (!hasPlayer(playerName)) {
            return false;
        }

        long currentScore = 0L;
        if (gameData.getScores().containsKey(playerName)) {
            currentScore = gameData.getScores().get(playerName);
        }

        gameData.getScores().put(playerName, Long.valueOf(currentScore - score));

        return true;
    }
    
    public boolean didPlayerLose(String playerName, long score){
        if (!hasPlayer(playerName)) {
            return false;
        }

        long currentScore = 0L;
        if (gameData.getScores().containsKey(playerName)) {
            currentScore = gameData.getScores().get(playerName);
        }
        
        if (currentScore <= 0){
        	return true;
        }

        return false;
    }

    /**
     * Resets the scores for all players to zero.
     */
    public void resetScores() {
        for (String playerName : gameData.getPlayers()) {
            gameData.getScores().put(playerName, Long.valueOf(0L));
        }
    }
    
    public void resetScores(Integer score){
        for (String playerName : gameData.getPlayers()) {
            gameData.getScores().put(playerName, Long.valueOf(score));
        }
    }

    /**
     * Returns a {@link SortedMap} of player names mapped to scores with the map sorted in
     * decreasing order of scores.
     * 
     * @return a {@link SortedMap} of player names mapped to scores with the map sorted in
     *         decreasing order of scores
     */
    public SortedMap<String, Long> getAllScoresInDescndingOrder() {
		Map<String, Long> scores = gameData.getScores();

		for (String playerName : gameData.getPlayers()) {
			if (!gameData.getScores().containsKey(playerName)) {
				scores.put(playerName, Long.valueOf(0L));
			}
		}

        SortedMap<String, Long> sortedScores =
                new TreeMap<String, Long>(new ScoreComparator(scores));
        sortedScores.putAll(gameData.getScores());
        return sortedScores;
    }

    /**
     * This comparator takes a map of player name and scores and uses that to sort a collection of
     * player names in the descending order of their scores.
     * <p>
     * Note: this comparator imposes orderings that are inconsistent with equals.
     */
    private static final class ScoreComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 7849926209990327190L;
        private final Map<String, Long> baseMap;

        private ScoreComparator(Map<String, Long> baseMap) {
            this.baseMap = baseMap;
        }

        @Override
        public int compare(String a, String b) {
            int longCompare = Long.compare(baseMap.get(b), baseMap.get(a));
            return longCompare != 0 ? longCompare : a.compareTo(b);
        }
    }
}
