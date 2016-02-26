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
package lifecounter.database;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.amazon.speech.speechlet.Session;

/**
 * Represents a score keeper game.
 */
public final class LifeCounterGame {
	private Session session;
	private LifeCounterGameData gameData;

	private LifeCounterGame() {
	}

	/**
	 * Creates a new instance of {@link LifeCounterGame} with the provided {@link Session} and
	 * {@link LifeCounterGameData}.
	 * <p>
	 * To create a new instance of {@link LifeCounterGameData}, see
	 * {@link LifeCounterGameData#newInstance()}
	 * 
	 * @param session
	 * @param gameData
	 * @return
	 * @see LifeCounterGameData#newInstance()
	 */
	public static LifeCounterGame newInstance(Session session, LifeCounterGameData gameData) {
		LifeCounterGame game = new LifeCounterGame();
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

	protected LifeCounterGameData getGameData() {
		return gameData;
	}

	protected void setGameData(LifeCounterGameData gameData) {
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
	public boolean hasLifeTotals() {
		return !gameData.getLifeTotals().isEmpty();
	}

	/**
	 * Returns the life total for a player.
	 * 
	 * @param playerName
	 *            Name of the player
	 * @return score for a player
	 */
	public long getLifeTotalForPlayer(String playerName) {
		return gameData.getLifeTotals().get(playerName);
	}

	/**
	 * Sets Player to provide life provided.
	 * @param playerName
	 *            Name of the player
	 * @param life
	 * 			  Life to be provided to the player
	 * @return true if the player existed, false otherwise
	 */
	public boolean setLifeForPlayer(String playerName, long life){
		if (!hasPlayer(playerName)) {
			return false;
		}

		gameData.getLifeTotals().put(playerName, Long.valueOf(life));
		return true;
	}

	/**
	 * Adds the life passed to it to the current score for a player. Returns true if the player
	 * existed, false otherwise.
	 * 
	 * @param playerName
	 *            Name of the player
	 * @param life
	 *            life to be added
	 * @return true if the player existed, false otherwise.
	 */
	public boolean addLifeForPlayer(String playerName, long life) {
		if (!hasPlayer(playerName)) {
			return false;
		}

		long currentScore = 0L;
		if (gameData.getLifeTotals().containsKey(playerName)) {
			currentScore = gameData.getLifeTotals().get(playerName);
		}

		gameData.getLifeTotals().put(playerName, Long.valueOf(currentScore + life));
		return true;
	}

	/**
	 * Subtracts the life passed to it to the current score for a player. Returns true if the player
	 * existed, false otherwise.
	 * 
	 * @param playerName
	 *            Name of the player
	 * @param life
	 *            life to be added
	 * @return true if the player existed, false otherwise.
	 */
	public boolean subtractLifeForPlayer(String playerName, long life) {
		if (!hasPlayer(playerName)) {
			return false;
		}

		long currentLife = 0L;
		if (gameData.getLifeTotals().containsKey(playerName)) {
			currentLife = gameData.getLifeTotals().get(playerName);
		}

		gameData.getLifeTotals().put(playerName, Long.valueOf(currentLife - life));

		return true;
	}

	public boolean didPlayerLose(String playerName, long score){
		if (!hasPlayer(playerName)) {
			return false;
		}

		long currentScore = 0L;
		if (gameData.getLifeTotals().containsKey(playerName)) {
			currentScore = gameData.getLifeTotals().get(playerName);
		}

		if (currentScore <= 0){
			return true;
		}

		return false;
	}

	/**
	 * Resets the life totals for all players to zero.
	 */
	public void resetLifeTotals() {
		for (String playerName : gameData.getPlayers()) {
			gameData.getLifeTotals().put(playerName, Long.valueOf(0L));
		}
	}

	/**
	 * Resets life totals to give amount
	 * @param score
	 */
	public void resetLifeTotals(Integer life){
		for (String playerName : gameData.getPlayers()) {
			gameData.getLifeTotals().put(playerName, Long.valueOf(life));
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
		Map<String, Long> scores = gameData.getLifeTotals();

		for (String playerName : gameData.getPlayers()) {
			if (!gameData.getLifeTotals().containsKey(playerName)) {
				scores.put(playerName, Long.valueOf(0L));
			}
		}

		SortedMap<String, Long> sortedScores =
				new TreeMap<String, Long>(new LifeTotalComparator(scores));
		sortedScores.putAll(gameData.getLifeTotals());
		return sortedScores;
	}

	/**
	 * This comparator takes a map of player name and scores and uses that to sort a collection of
	 * player names in the descending order of their scores.
	 * <p>
	 * Note: this comparator imposes orderings that are inconsistent with equals.
	 */
	private static final class LifeTotalComparator implements Comparator<String>, Serializable {
		private static final long serialVersionUID = 7849926209990327190L;
		private final Map<String, Long> baseMap;

		private LifeTotalComparator(Map<String, Long> baseMap) {
			this.baseMap = baseMap;
		}

		@Override
		public int compare(String a, String b) {
			int longCompare = Long.compare(baseMap.get(b), baseMap.get(a));
			return longCompare != 0 ? longCompare : a.compareTo(b);
		}
	}
}
