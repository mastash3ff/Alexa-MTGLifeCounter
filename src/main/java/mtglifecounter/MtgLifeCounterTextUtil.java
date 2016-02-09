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

import java.util.Arrays;
import java.util.List;

/**
 * Utililty class containing helpers for manipulating and handling text.
 */
public final class MtgLifeCounterTextUtil {

    private MtgLifeCounterTextUtil() {
    }

    /**
     * List of player names blacklisted for this app.
     */
    private static final List<String> NAME_BLACKLIST = Arrays.asList("player", "players");

    /**
     * Text of complete help.
     */
    public static final String COMPLETE_HELP =
            "Here's some things you can say. Add Brandon, take Brandon 5 points, tell me the score, "
                    + "new game, reset, and exit.";

    /**
     * Text of next help.
     */
    public static final String NEXT_HELP = "You can give a player points, add a player, get the "
            + "current score, or say help. What would you like?";

    /**
     * Cleans up the player name, and sanitizes it against the blacklist.
     *
     * @param recognizedPlayerName
     * @return
     */
    public static String getPlayerName(String recognizedPlayerName) {
        if (recognizedPlayerName == null || recognizedPlayerName.isEmpty()) {
            return null;
        }

        String cleanedName;
        if (recognizedPlayerName.contains(" ")) {
            // the name should only contain a first name, so ignore the second part if any
            cleanedName = recognizedPlayerName.substring(recognizedPlayerName.indexOf(" "));
        } else {
            cleanedName = recognizedPlayerName;
        }

        // if the name is on our blacklist, it must be mis-recognition
        if (NAME_BLACKLIST.contains(cleanedName)) {
            return null;
        }

        return cleanedName;
    }
}
