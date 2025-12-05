package chess.controller;

import chess.model.*;
import chess.patterns.command.CommandHistory;
import chess.patterns.command.MoveCommand;
import chess.patterns.observer.GameObserver;
import chess.patterns.observer.GameSubject;

import java.util.ArrayList;
import java.util.List;

/**
 * Main game controller that orchestrates the chess game.
 * 
 * Implements the Observer pattern (GameSubject) for notifying UI components.
 * Uses the Command pattern (CommandHistory) for undo/redo functionality.
 * Uses dependency injection for the board and move validator.
 */
public class Game implements GameSubject {
    
    private final Board board;
    private final MoveValidator moveValidator;
    private final CommandHistory commandHistory;
    private final List<GameObserver> observers;
    private final List<Piece> capturedWhitePieces;
    private final List<Piece> capturedBlackPieces;
    private final boolean undoEnabled;
    
    private Color currentPlayer;
    private GameState gameState;
    private int moveNumber;
    
    /**
     * Creates a new game with dependency injection.
     */
    public Game(Board board, MoveValidator moveValidator, Color startingPlayer, boolean undoEnabled) {
        this.board = board;
        this.moveValidator = moveValidator;
        this.currentPlayer = startingPlayer;
        this.undoEnabled = undoEnabled;
        this.commandHistory = new CommandHistory();
        this.observers = new ArrayList<>();
        this.capturedWhitePieces = new ArrayList<>();
        this.capturedBlackPieces = new ArrayList<>();
        this.gameState = GameState.IN_PROGRESS;
        this.moveNumber = 1;
    }
    
    /**
     * Attempts to make a move from one position to another.
     * @return true if the move was made successfully
     */
    public boolean makeMove(Position from, Position to) {
        return makeMove(from, to, null);
    }
    
    /**
     * Attempts to make a move with optional promotion type.
     * @return true if the move was made successfully
     */
    public boolean makeMove(Position from, Position to, PieceType promotionType) {
        if (gameState == GameState.CHECKMATE || gameState == GameState.STALEMATE) {
            return false;
        }
        
        Move move = moveValidator.validateMove(board, from, to, currentPlayer);
        if (move == null) {
            return false;
        }
        
        // Handle promotion type
        if (move.isPromotion() && promotionType != null) {
            move = new Move(from, to, move.getPiece(), move.getCapturedPiece(),
                           move.getMoveType(), promotionType);
        }
        
        // Execute the move using Command pattern
        MoveCommand command = new MoveCommand(board, move);
        commandHistory.executeCommand(command);
        
        // Track captured pieces
        if (command.getCapturedPiece() != null) {
            Piece captured = command.getCapturedPiece();
            if (captured.getColor() == Color.WHITE) {
                capturedWhitePieces.add(captured);
            } else {
                capturedBlackPieces.add(captured);
            }
            notifyPieceCaptured(captured);
        }
        
        // Notify observers of the move
        notifyMoveMade(move);
        notifyBoardChanged();
        
        // Switch players
        currentPlayer = currentPlayer.opposite();
        if (currentPlayer == Color.WHITE) {
            moveNumber++;
        }
        notifyTurnChanged();
        
        // Update game state
        updateGameState();
        
        return true;
    }
    
    /**
     * Undoes the last move.
     * @return true if a move was undone
     */
    public boolean undoMove() {
        if (!undoEnabled || !commandHistory.canUndo()) {
            return false;
        }
        
        MoveCommand lastCommand = (MoveCommand) commandHistory.getLastCommand();
        Move lastMove = lastCommand.getMove();
        
        // Restore captured piece tracking
        if (lastCommand.getCapturedPiece() != null) {
            Piece captured = lastCommand.getCapturedPiece();
            if (captured.getColor() == Color.WHITE) {
                capturedWhitePieces.remove(captured);
            } else {
                capturedBlackPieces.remove(captured);
            }
        }
        
        commandHistory.undo();
        
        // Switch back to previous player
        currentPlayer = currentPlayer.opposite();
        if (currentPlayer == Color.BLACK) {
            moveNumber--;
        }
        
        // Notify observers
        notifyMoveUndone(lastMove);
        notifyBoardChanged();
        notifyTurnChanged();
        updateGameState();
        
        return true;
    }
    
    /**
     * Redoes the last undone move.
     * @return true if a move was redone
     */
    public boolean redoMove() {
        if (!undoEnabled || !commandHistory.canRedo()) {
            return false;
        }
        
        commandHistory.redo();
        
        MoveCommand command = (MoveCommand) commandHistory.getLastCommand();
        
        // Update captured piece tracking
        if (command.getCapturedPiece() != null) {
            Piece captured = command.getCapturedPiece();
            if (captured.getColor() == Color.WHITE) {
                capturedWhitePieces.add(captured);
            } else {
                capturedBlackPieces.add(captured);
            }
        }
        
        // Switch players
        currentPlayer = currentPlayer.opposite();
        if (currentPlayer == Color.WHITE) {
            moveNumber++;
        }
        
        // Notify observers
        notifyMoveMade(command.getMove());
        notifyBoardChanged();
        notifyTurnChanged();
        updateGameState();
        
        return true;
    }
    
    /**
     * Gets all legal moves for the piece at the given position.
     */
    public List<Move> getLegalMoves(Position position) {
        return moveValidator.getLegalMoves(board, position, currentPlayer);
    }
    
    /**
     * Gets legal target positions for a piece (for UI highlighting).
     */
    public List<Position> getLegalTargets(Position from) {
        List<Move> moves = getLegalMoves(from);
        List<Position> targets = new ArrayList<>();
        for (Move move : moves) {
            targets.add(move.getTo());
        }
        notifyLegalMovesCalculated(from, targets);
        return targets;
    }
    
    private void updateGameState() {
        gameState = moveValidator.getGameState(board, currentPlayer);
        notifyGameStateChanged();
    }
    
    // Observer pattern implementation
    
    @Override
    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        notifyBoardChanged();
        notifyTurnChanged();
        notifyGameStateChanged();
    }
    
    private void notifyMoveMade(Move move) {
        for (GameObserver observer : observers) {
            observer.onMoveMade(move);
        }
    }
    
    private void notifyMoveUndone(Move move) {
        for (GameObserver observer : observers) {
            observer.onMoveUndone(move);
        }
    }
    
    private void notifyBoardChanged() {
        for (GameObserver observer : observers) {
            observer.onBoardChanged(board);
        }
    }
    
    private void notifyTurnChanged() {
        for (GameObserver observer : observers) {
            observer.onTurnChanged(currentPlayer);
        }
    }
    
    private void notifyGameStateChanged() {
        for (GameObserver observer : observers) {
            observer.onGameStateChanged(gameState, currentPlayer);
        }
    }
    
    private void notifyPieceCaptured(Piece piece) {
        for (GameObserver observer : observers) {
            observer.onPieceCaptured(piece);
        }
    }
    
    private void notifyLegalMovesCalculated(Position from, List<Position> moves) {
        for (GameObserver observer : observers) {
            observer.onLegalMovesCalculated(from, moves);
        }
    }
    
    // Getters
    
    public Board getBoard() {
        return board;
    }
    
    public Color getCurrentPlayer() {
        return currentPlayer;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public int getMoveNumber() {
        return moveNumber;
    }
    
    public List<Piece> getCapturedPieces(Color color) {
        return color == Color.WHITE ? 
               new ArrayList<>(capturedWhitePieces) : 
               new ArrayList<>(capturedBlackPieces);
    }
    
    public boolean canUndo() {
        return undoEnabled && commandHistory.canUndo();
    }
    
    public boolean canRedo() {
        return undoEnabled && commandHistory.canRedo();
    }
    
    public CommandHistory getCommandHistory() {
        return commandHistory;
    }
}
