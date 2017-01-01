# Alexa- Life Counter for MTG
Alexa application that keeps track of life totals in Magic: The Gathering.  By default, players start with life totals of 20.  Some example usages can be found below.

## Short Description
Alexa application that keeps track of life totals for Magic:  The Gathering(c) trading card game.

## Long Description
Alexa application that keeps track of life totals for Magic:  The Gathering(c) trading card game.  Each player is started with 20 life when initiating this application.  You can give it voice commands to add/subtract life totals from each player.  Other voice commands are supported such as adding players and resetting the game.  See examples for some voice command usages.

How to contribute
- See issues (easy documentation stuff to fix)
- Add additional documentation.
- Contribute code fixes n shit

## Things that can be improved on now
- Add more player names to 'speechAssets/customSlotTypes/LIST_OF_NAMES'
- More dialogue variety for Add/Subtracting scores.
- More documentation in this README particularly dialogue examples
- Application Icon

## Future Features

- Poison counter support.
- Supported game formats beyond 1v1.

## Examples

Interact with the skill by invoking a phrase with 'life counter'.  Some usage examples can be found below.

## How to launch the skill and interact with it:
    User: "Alexa, start life counter."
    Alexa: "MTG Life Counter, Let's start your game. Who's your first player?"
    User: "Add Brandon"
    Alexa: "Brandon has joined your game"
    User: "Add player Jessica"
    Alexa: "Jessica has joined your game"

    (skill saves the new game and ends)

    User: "Alexa, tell life counter to give Jessica three life."
    Alexa: "3 for Jessica. Jessica has 23 life. and Brandon has 20 life."

    (skill saves the latest life totals and ends)

## Some things you can ask/tell the skill:
    User: "Alexa, ask life counter what are the life totals?"
    Alexa: "Brandon has twenty points and Jessica has twenty-three."

    User: "Alexa, tell life counter to take twenty life from Brandon."
    Alexa: "Player Jessica has won the game!"

    User: "Alexa, tell life counter to start a new game"
    Alexa: "New game started with 2 existing player."

    User: "Alexa, tell life counter to reset."
    Alexa: "New game started without players. Who do you want to add first?"

    User: "Alexa, ask life counter for help."
    Alexa: "Here's some things you can say. Add Brandon, take five life from Brandon, tell me the life totals, new game, reset, and exit."

## Development Setup

To run this example skill you need to do two things. The first is to deploy the example code in lambda, and the second is to configure the Alexa skill to use Lambda.

### AWS Lambda Setup
Go to the AWS Console and click on the Lambda link. Note: ensure you are in us-east or you wont be able to use Alexa with Lambda.
Click on the Create a Lambda Function or Get Started Now button.
Skip the blueprint
Name the Lambda Function "MtgLifeCounter-Skill".
Select the runtime as Java 8
Go to the the samples/ directory containing pom.xml, and run 'mvn assembly:assembly -DdescriptorId=jar-with-dependencies package'. This will generate a zip file named "alexa-skills-kit-samples-1.0-jar-with-dependencies.jar" in the target directory.
Select Code entry type as "Upload a .ZIP file" and then upload the "mtglifecounter-1.0-jar-with-dependencies.jar" file from the build directory to Lambda
Set the Handler as mtglifecounter.MtgLifeCounterSpeechletRequestStreamHandler (this refers to the Lambda RequestStreamHandler file in the zip).
Create a "Basic with DynamoDB" role and click create.
Leave the Advanced settings as the defaults.
Click "Next" and review the settings then click "Create Function"
Click the "Event Sources" tab and select "Add event source"
Set the Event Source type as Alexa Skills kit and Enable it now. Click Submit.
Copy the ARN from the top right to be used later in the Alexa Skill Setup.

### AWS DynamoDB Setup
Go to the AWS Console and click on DynamoDB link. Note: ensure you are in us-east (same as your Lambda)
Click on CreateTable: set "MtgLifeCounterUserData" as the table name, use Hash for the primary key type and set "CustomerId" as the hash attribute name.
Continue the steps with the default settings to finish the setup of DynamoDB table.
Alexa Skill Setup
Go to the Alexa Console and click Add a New Skill.
Set "MtgLifeCounter" as the skill name and "life counter" as the invocation name, this is what is used to activate your skill. For example you would say: "Alexa, Ask life counter for the current score."
Select the Lambda ARN for the skill Endpoint and paste the ARN copied from above. Click Next.
Copy the custom slot types from the customSlotTypes folder. Each file in the folder represents a new custom slot type. The name of the file is the name of the custom slot type, and the values in the file are the values for the custom slot.
Copy the Intent Schema from the included IntentSchema.json.
Copy the Sample Utterances from the included SampleUtterances.txt. Click Next.
Go back to the skill Information tab and copy the appId. Paste the appId into the MtgLifeCounterSpeechletRequestStreamHandler.java file for the variable supportedApplicationIds, then update the lambda source zip file with this change and upload to lambda again, this step makes sure the lambda function only serves request from authorized source.
You are now able to start testing your sample skill! You should be able to go to the Echo webpage and see your skill enabled.
In order to test it, try to say some of the Sample Utterances from the Examples section below.
Your skill is now saved and once you are finished testing you can continue to publish your skill

# Alexa-MTGLifeCounter
Alexa skill app that keeps track of player life total in MTG card game.
