package mtglifecounter.storage;

import com.amazon.speech.speechlet.Session;

/**
 * Contains the methods to interact with the persistence layer for ScoreKeeper in DynamoDB.
 */
public class MtgLifeCounterDao {
    private final MtgLifeCounterDynamoDbClient dynamoDbClient;

    public MtgLifeCounterDao(MtgLifeCounterDynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    /**
     * Reads and returns the {@link MtgLifeCounterGame} using user information from the session.
     * <p>
     * Returns null if the item could not be found in the database.
     * 
     * @param session
     * @return
     */
    public MtgLifeCounterGame getScoreKeeperGame(Session session) {
        MtgLifeCounterUserDataItem item = new MtgLifeCounterUserDataItem();
        item.setCustomerId(session.getUser().getUserId());

        item = dynamoDbClient.loadItem(item);

        if (item == null) {
            return null;
        }

        return MtgLifeCounterGame.newInstance(session, item.getGameData());
    }

    /**
     * Saves the {@link MtgLifeCounterGame} into the database.
     * 
     * @param game
     */
    public void saveScoreKeeperGame(MtgLifeCounterGame game) {
        MtgLifeCounterUserDataItem item = new MtgLifeCounterUserDataItem();
        item.setCustomerId(game.getSession().getUser().getUserId());
        item.setGameData(game.getGameData());

        dynamoDbClient.saveItem(item);
    }
}
