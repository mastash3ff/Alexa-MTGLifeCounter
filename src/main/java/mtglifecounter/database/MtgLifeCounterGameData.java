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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains player and score data to represent a score keeper game.
 */
public class MtgLifeCounterGameData {
    private List<String> players;
    private Map<String, Long> scores;

    public MtgLifeCounterGameData() {
        // public no-arg constructor required for DynamoDBMapper marshalling
    }

    /**
     * Creates a new instance of {@link MtgLifeCounterGameData} with initialized but empty player and
     * score information.
     * 
     * @return
     */
    public static MtgLifeCounterGameData newInstance() {
        MtgLifeCounterGameData newInstance = new MtgLifeCounterGameData();
        newInstance.setPlayers(new ArrayList<String>());
        newInstance.setScores(new HashMap<String, Long>());
        return newInstance;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public Map<String, Long> getScores() {
        return scores;
    }

    public void setScores(Map<String, Long> scores) {
        this.scores = scores;
    }

    @Override
    public String toString() {
        return "[MTGLifeCounterGameData players: " + players + "] scores: " + scores + "]";
    }
}
