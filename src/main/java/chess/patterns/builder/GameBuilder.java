package chess.patterns.builder;

import chess.controller.Game;
import chess.controller.HumanPlayerController;
import chess.controller.PlayerController;
import chess.controller.RuleSet;
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
    private RuleSet ruleSet;
    private Color startingPlayer;
    private List<GameObserver> observers;
    private boolean enableUndo;
    private PlayerController whiteController;
    private PlayerController blackController;
    
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
     * Sets a custom rule set (dependency injection).
     */
    public GameBuilder withRuleSet(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
        return this;
    }

    /**
     * Backwards-compatible helper for injecting a move validator as the rule set.
     */
    public GameBuilder withMoveValidator(MoveValidator validator) {
        this.ruleSet = validator;
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
     * Supplies controllers for white and black players.
     */
    public GameBuilder withPlayerControllers(PlayerController white, PlayerController black) {
        this.whiteController = white;
        this.blackController = black;
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
        
        if (ruleSet == null) {
            ruleSet = new MoveValidator();
        }
        
        // Create the game with dependency injection
        Game game = new Game(board, ruleSet, startingPlayer, enableUndo);
        game.setPlayerControllers(
                whiteController != null ? whiteController : new HumanPlayerController(Color.WHITE),
                blackController != null ? blackController : new HumanPlayerController(Color.BLACK)
        );
        
        // Register all observers
        for (GameObserver observer : observers) {
            game.addObserver(observer);
        }
        
        return game;
    }
}
