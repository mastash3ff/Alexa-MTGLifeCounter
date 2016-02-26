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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * Client for DynamoDB persistance layer for the Score Keeper skill.
 */
public class LifeCounterDynamoDbClient {
    private final AmazonDynamoDBClient dynamoDBClient;

    public LifeCounterDynamoDbClient(final AmazonDynamoDBClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    /**
     * Loads an item from DynamoDB by primary Hash Key. Callers of this method should pass in an
     * object which represents an item in the DynamoDB table item with the primary key populated.
     * 
     * @param tableItem
     * @return
     */
    public LifeCounterUserDataItem loadItem(final LifeCounterUserDataItem tableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        LifeCounterUserDataItem item = mapper.load(tableItem);
        return item;
    }

    /**
     * Stores an item to DynamoDB.
     * 
     * @param tableItem
     */
    public void saveItem(final LifeCounterUserDataItem tableItem) {
        DynamoDBMapper mapper = createDynamoDBMapper();
        mapper.save(tableItem);
    }

    /**
     * Creates a {@link DynamoDBMapper} using the default configurations.
     * 
     * @return
     */
    private DynamoDBMapper createDynamoDBMapper() {
        return new DynamoDBMapper(dynamoDBClient);
    }
}
