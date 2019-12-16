# [jeopardy](https://github.com/jojojo8359/jeopardy)
A Jeopardy! game made with Java that uses [jservice.io](http://jservice.io)

# How to Play
## Modes
Currently, there are two Singleplayer Modes: Classic and Trivia Mode.

*(Note: There is no time limit on any question in any mode!)*
## Classic Mode
Within the Classic mode, you can choose from 4 options:
- Single Jeopardy!
- Double Jeopardy!
- Single and Double Jeopardy!
- Single, Double, and Final Jeopardy!
### Single and Double Jeopardy!
In Single and Double Jeopardy!, a full Jeopardy! board (six categories, five questions per category) will be generated. The player chooses a category and a question in that category, and then must try to guess the answer in Jeopardy style (by asking "What is..." or "Who is..."). However, the player will be prompted with this text, and will not need to include the start of the question in their answer. Each correct or incorrect answer will add or subtract money from the player's balance.
### Daily Double
During a Single or Double Jeopardy! round, a Daily Double question will appear at an unknown location on the board, just like the show. If one is found, then the player will be able to wager any amount of money from $1 to their balance. If the player's balance is less than $1, they will receive a maximum of $1000 to wager instead.
### Final Jeopardy!
In Final Jeopardy!, the player will be able to wager money, as explained in the Daily Double section. A single question will then be generated for the user to answer like a normal question.
## Trivia Mode
In Trivia Mode, a random question (values ranging from $200-1000) will be generated, and the player may answer as many questions as they please.
## Special Commands
### Classic Mode
If category text is not readable (not including formatting): When a column is selected and the player is prompted to choose a question value, typing `!r` will display the category id, and will not penalize your balance. Leave this id number in a GitHub issue for it to be fixed.

If question text is not readable (not including formatting): When you are prompted for the answer to a question, typing `!r` will display the question id, and will not penalize your balance. Leave this id number in a GitHub issue for it to be fixed.
### Trivia Mode
When prompted with `[Enter/q]` after answering a question, pressing enter will continue to the next question, while typing `q` only and pressing enter will exit trivia mode.
# External Libraries Used
- jservice.io (API)              ([GitHub](https://github.com/sottenad/jService), [Website](http://jservice.io))
- org.json                       ([GitHub](https://github.com/stleary/JSON-java), [Maven](https://mvnrepository.com/artifact/org.json/json))
- Java Text Tables               ([GitHub](https://github.com/iNamik/java_text_tables))
- Log4j2 (Used for development)  ([Download](http://logging.apache.org/log4j/2.x/download.html))

# Planned Features
- [X] Single-Player
  - [X] Classic mode (with standard board)
  - [X] Trivia mode (nonstop questions)
- [ ] Multi-Player (Not anytime soon!)
  - [ ] Local
  - [ ] Online/LAN
