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

public class MTGLifeCounterSpeechlet implements Speechlet {

	private static final Logger log = LoggerFactory.getLogger(MTGLifeCounterSpeechlet.class);
	private AmazonDynamoDBClient amazonDynamoDBClient;
	private MtgLifeCounterManager scoreKeeperManager;
	private SkillContext skillContext;

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
		case "AddScoreIntent":
			return scoreKeeperManager.getAddScoreIntentResponse(intent, session, skillContext);
		case "SubScoreIntent":
			return scoreKeeperManager.getSubScoreIntentResponse(intent, session, skillContext);
		case "TellScoresIntent":
			return scoreKeeperManager.getTellScoresIntentResponse(intent, session);
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

		/*
		if ("NewGameIntent".equals(intent.getName())) {
			return scoreKeeperManager.getNewGameIntentResponse(session, skillContext);

		} else if ("AddPlayerIntent".equals(intent.getName())) {
			return scoreKeeperManager.getAddPlayerIntentResponse(intent, session, skillContext);

		} else if ("AddScoreIntent".equals(intent.getName())) {
			return scoreKeeperManager.getAddScoreIntentResponse(intent, session, skillContext);
		} 
		else if ("SubScoreIntent".equals(intent.getName())) {
			return scoreKeeperManager.getSubScoreIntentResponse(intent, session, skillContext);
		} 
		else if ("TellScoresIntent".equals(intent.getName())) {
			return scoreKeeperManager.getTellScoresIntentResponse(intent, session);

		} else if ("ResetPlayersIntent".equals(intent.getName())) {
			return scoreKeeperManager.getResetPlayersIntentResponse(intent, session);

		} else if ("AMAZON.HelpIntent".equals(intent.getName())) {
			return scoreKeeperManager.getHelpIntentResponse(intent, session, skillContext);

		} else if ("AMAZON.CancelIntent".equals(intent.getName())) {
			return scoreKeeperManager.getExitIntentResponse(intent, session, skillContext);

		} else if ("AMAZON.StopIntent".equals(intent.getName())) {
			return scoreKeeperManager.getExitIntentResponse(intent, session, skillContext);

		} else {
			throw new IllegalArgumentException("Unrecognized intent: " + intent.getName());
		}
		 */

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
			skillContext = new SkillContext();
		}
	}
}
