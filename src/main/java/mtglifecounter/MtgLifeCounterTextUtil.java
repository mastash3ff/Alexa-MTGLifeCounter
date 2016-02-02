package mtglifecounter;

import java.util.Arrays;
import java.util.List;

/**
 * Util containing various text related utils.
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
            "Here's some things you can say. Add John, give John 5 points, tell me the score, "
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
