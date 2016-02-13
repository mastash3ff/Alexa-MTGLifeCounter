#MTG Life Counter for Alexa
Alexa application that keeps track of life totals in Magic:  The Gathering.  By default, players start with life totals of 20.  Some example usages can be found below.

## Things that can be improved on now
- Add more player names to 'speechAssets/customSlotTypes/LIST_OF_NAMES'
- More documentation in this README particularly dialogue examplles

## Future Features

- Poison counter support.
- Supported game formats beyond 1v1.

## Examples

### Dialog model:
    User: "Alexa, tell life counter to reset."
    Alexa: "New game started without players. Who do you want to add first?"
    User: "Add Jessica"
    Alexa: "Jessica has joined your game"
    User: "Add player Brandon"
    Alexa: "Brandon has joined your game"

    (skill saves the new game and ends)

    User: "Alexa, tell life counter to give Jessica three points."
    Alexa: "Updating your score, three points for Jessica"

    (skill saves the latest score and ends)

### Simple one Request/Response:
    User: "Alexa, ask life counter what's the current score?"
    Alexa: "Brandon has twenty points and Jessica has twenty-three"

## Development Setup

To run this example skill you need to do two things. The first is to deploy the example code in lambda, and the second is to configure the Alexa skill to use Lambda.

### AWS Lambda Setup
Go to the AWS Console and click on the Lambda link. Note: ensure you are in us-east or you wont be able to use Alexa with Lambda.
Click on the Create a Lambda Function or Get Started Now button.
Skip the blueprint
Name the Lambda Function "Score-Keeper-Example-Skill".
Select the runtime as Java 8
Go to the the samples/ directory containing pom.xml, and run 'mvn assembly:assembly -DdescriptorId=jar-with-dependencies package'. This will generate a zip file named "alexa-skills-kit-samples-1.0-jar-with-dependencies.jar" in the target directory.
Select Code entry type as "Upload a .ZIP file" and then upload the "alexa-skills-kit-samples-1.0-jar-with-dependencies.jar" file from the build directory to Lambda
Set the Handler as scorekeeper.ScoreKeeperSpeechletRequestStreamHandler (this refers to the Lambda RequestStreamHandler file in the zip).
Create a "Basic with DynamoDB" role and click create.
Leave the Advanced settings as the defaults.
Click "Next" and review the settings then click "Create Function"
Click the "Event Sources" tab and select "Add event source"
Set the Event Source type as Alexa Skills kit and Enable it now. Click Submit.
Copy the ARN from the top right to be used later in the Alexa Skill Setup.

### AWS DynamoDB Setup
Go to the AWS Console and click on DynamoDB link. Note: ensure you are in us-east (same as your Lambda)
Click on CreateTable: set "ScoreKeeperUserData" as the table name, use Hash for the primary key type and set "CustomerId" as the hash attribute name.
Continue the steps with the default settings to finish the setup of DynamoDB table.
Alexa Skill Setup
Go to the Alexa Console and click Add a New Skill.
Set "ScoreKeeper" as the skill name and "score keeper" as the invocation name, this is what is used to activate your skill. For example you would say: "Alexa, Ask score keeper for the current score."
Select the Lambda ARN for the skill Endpoint and paste the ARN copied from above. Click Next.
Copy the custom slot types from the customSlotTypes folder. Each file in the folder represents a new custom slot type. The name of the file is the name of the custom slot type, and the values in the file are the values for the custom slot.
Copy the Intent Schema from the included IntentSchema.json.
Copy the Sample Utterances from the included SampleUtterances.txt. Click Next.
Go back to the skill Information tab and copy the appId. Paste the appId into the ScoreKeeperSpeechletRequestStreamHandler.java file for the variable supportedApplicationIds, then update the lambda source zip file with this change and upload to lambda again, this step makes sure the lambda function only serves request from authorized source.
You are now able to start testing your sample skill! You should be able to go to the Echo webpage and see your skill enabled.
In order to test it, try to say some of the Sample Utterances from the Examples section below.
Your skill is now saved and once you are finished testing you can continue to publish your skill

