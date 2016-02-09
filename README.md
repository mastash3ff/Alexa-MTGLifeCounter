#MTG Life Counter for Alexa
Alexa application that keeps track of life totals in Magic:  The Gathering.  By default, players start with life totals of 20.  Some example usages can be found below.

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

### One-shot model:
    User: "Alexa, ask life counter what's the current score?"
    Alexa: "Brandon has twenty points and Jessica has twenty-three"
