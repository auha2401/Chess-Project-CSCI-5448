Name of Project: Java 2D Chess Implementation

Team Members: Nick Bolger, Austin Hardy

Development Language / Platform: Java (OO), using Java 2D/Swing for the UI

Project Description:

We will implement a fully playable chess game with a 2D graphical board rendered using Java 2D/Swing. The application will support standard chess rules and allow two human players to play on the same machine by clicking squares on the board. The game state will be persisted to a file using a simple chess notation so games can be saved and loaded.

To satisfy the design requirements, we will build the core domain model around abstractions such as a Piece interface/abstract class, with concrete types like Pawn, Rook, and Knight treated polymorphically. We will also use dependency injection to pass in abstractions like MoveValidator, RuleSet, and PlayerController into a main Game class, which will make everything easy to test and to refactor if needed.


The project must:

Use and identify 5 design patterns
You should use dependency injection where possible
Code to abstractions
Treat things polymorphically
Generally means, no big if-then-else or switch statements
Must have either a UI or some form of persisted state (ORM, file, database)
