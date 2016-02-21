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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class MtgLifeCounterSpeechlet implements Speechlet {

	private static final Logger log = LoggerFactory.getLogger(MtgLifeCounterSpeechlet.class);
	private AmazonDynamoDBClient amazonDynamoDBClient;
	private MtgLifeCounterManager scoreKeeperManager;
	private MtgLifeCounterSkillContext skillContext;

	@Override
	public void onSessionStarted(final SessionStartedRequest request, final Session session)
			throws SpeechletException {
		log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());

		initializeComponents(); //init db, manager, and context

		// if user said a one shot command that triggered an intent event,
		// it will start a new session, and then we should avoid speaking too many words.
		skillContext.setNeedsMoreHelp(false);
	}

	@Override
	public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
			throws SpeechletException {
		log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());

		skillContext.setNeedsMoreHelp(true);
		return scoreKeeperManager.getLaunchResponse(request, session);
	}

	@Override
	public SpeechletResponse onIntent(IntentRequest request, Session session)
			throws SpeechletException {
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		initializeComponents();

		Intent intent = request.getIntent();
		switch (intent.getName()) {
		case "NewGameIntent":
			return scoreKeeperManager.getNewGameIntentResponse(session, skillContext);
		case "AddPlayerIntent":
			return scoreKeeperManager.getAddPlayerIntentResponse(intent, session, skillContext);
		case "AddLifeIntent":
			return scoreKeeperManager.getAddLifeIntentResponse(intent, session, skillContext);
		case "SubLifeIntent":
			return scoreKeeperManager.getSubLifeIntentResponse(intent, session, skillContext);
		case "TellLifeTotalsIntent":
			return scoreKeeperManager.getTellLifeTotalsIntentResponse(intent, session);
		case "ResetPlayersIntent":
			return scoreKeeperManager.getResetPlayersIntentResponse(intent, session);
		case "AMAZON.HelpIntent":
			return scoreKeeperManager.getHelpIntentResponse(intent, session, skillContext);
		case "AMAZON.CancelIntent":
			return scoreKeeperManager.getExitIntentResponse(intent, session, skillContext);
		case "AMAZON.StopIntent":
			return scoreKeeperManager.getExitIntentResponse(intent, session, skillContext);
		default:
			throw new IllegalArgumentException("Unrecognized intent: " + intent.getName());
		}
	}

	@Override
	public void onSessionEnded(final SessionEndedRequest request, final Session session)
			throws SpeechletException {
		log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
				session.getSessionId());
		// any cleanup logic goes here
	}

	/**
	 * Initializes the instance components if needed.
	 */
	private void initializeComponents() {
		if (amazonDynamoDBClient == null) {
			amazonDynamoDBClient = new AmazonDynamoDBClient();
			scoreKeeperManager = new MtgLifeCounterManager(amazonDynamoDBClient);
			skillContext = new MtgLifeCounterSkillContext();
		}
	}
}
