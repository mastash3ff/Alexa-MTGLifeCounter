# Life Counter for MTG

![Deploy](https://github.com/mastash3ff/Alexa-MTGLifeCounter/actions/workflows/deploy.yml/badge.svg)

An Alexa skill for tracking Magic: The Gathering life totals by voice. Add players, adjust life totals, and check standings hands-free during a game. All state is session-based — no persistent storage required.

## Usage

**Invocation:** `life counter`

| Say... | Response |
|--------|----------|
| "Alexa, open life counter" | Starts a new game or resumes an existing session |
| "Add [name]" | Adds a player at 20 life |
| "Give [name] [N] life" | Adds N life to that player |
| "Take [N] life from [name]" | Subtracts N life from that player |
| "Tell me the life totals" | Reads all current life totals |
| "New game" | Resets all players to 20 life |
| "Reset" | Clears all players from the session |
| "Help" | Lists available commands |
| "Stop" / "Exit" | Ends the skill |

## How to play

Open the skill and add each player by name. As the game progresses, tell Alexa to add or subtract life. Alexa announces totals after each change. When a player reaches zero with one opponent remaining, the winner is declared.

## Development

**Stack:** Python 3.12 · ASK SDK v2 · AWS Lambda (us-east-1)

```bash
# Install dependencies
pip install -r requirements.txt

# Run tests
PYTHONPATH=. pytest tests/ -v

# Deploy — automatic on push to master via GitHub Actions
```

## Project structure

```
lambda_function.py      Intent handlers, game logic, and helpers
requirements.txt        ask-sdk-core dependency
tests/test_skill.py     Unit tests (34 cases)
.github/workflows/      CI/CD — tests gate deployment to Lambda
```
