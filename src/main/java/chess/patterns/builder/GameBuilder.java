package chess.patterns.builder;

import chess.controller.Game;
import chess.controller.MoveValidator;
import chess.model.Board;
import chess.model.Color;
import chess.patterns.observer.GameObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder Pattern: Constructs Game instances with flexible configuration.
 * 
 * Allows configuring the game with different boards, validators,
 * and observers through a fluent interface.
 */
public class GameBuilder {
    
    private Board board;
    private MoveValidator moveValidator;
    private Color startingPlayer;
    private List<GameObserver> observers;
    private boolean enableUndo;
    
    public GameBuilder() {
        this.observers = new ArrayList<>();
        this.startingPlayer = Color.WHITE;
        this.enableUndo = true;
    }
    
    /**
     * Sets the initial board configuration.
     */
    public GameBuilder withBoard(Board board) {
        this.board = board;
        return this;
    }
    
    /**
     * Uses a standard starting position.
     */
    public GameBuilder withStandardSetup() {
        this.board = new BoardBuilder().withStandardSetup().build();
        return this;
    }
    
    /**
     * Sets a custom move validator (dependency injection).
     */
    public GameBuilder withMoveValidator(MoveValidator validator) {
        this.moveValidator = validator;
        return this;
    }
    
    /**
     * Sets which player moves first.
     */
    public GameBuilder withStartingPlayer(Color color) {
        this.startingPlayer = color;
        return this;
    }
    
    /**
     * Adds an observer to be notified of game events.
     */
    public GameBuilder withObserver(GameObserver observer) {
        this.observers.add(observer);
        return this;
    }
    
    /**
     * Enables or disables undo functionality.
     */
    public GameBuilder withUndoEnabled(boolean enabled) {
        this.enableUndo = enabled;
        return this;
    }
    
    /**
     * Builds and returns the configured Game instance.
     */
    public Game build() {
        // Use defaults if not specified
        if (board == null) {
            board = new BoardBuilder().withStandardSetup().build();
        }
        
        if (moveValidator == null) {
            moveValidator = new MoveValidator();
        }
        
        // Create the game with dependency injection
        Game game = new Game(board, moveValidator, startingPlayer, enableUndo);
        
        // Register all observers
        for (GameObserver observer : observers) {
            game.addObserver(observer);
        }
        
        return game;
    }
}
