package chess.patterns.observer;

import chess.model.*;

/**
 * Observer Pattern: Interface for objects that want to be notified of game events.
 * 
 * UI components implement this interface to react to game state changes
 * without the Game class needing to know about specific UI implementations.
 */
public interface GameObserver {
    
    /**
     * Called when a move has been made.
     */
    void onMoveMade(Move move);
    
    /**
     * Called when the board state has changed.
     */
    void onBoardChanged(Board board);
    
    /**
     * Called when the current player changes.
     */
    void onTurnChanged(Color currentPlayer);
    
    /**
     * Called when the game state changes (check, checkmate, etc.).
     */
    void onGameStateChanged(GameState state, Color affectedPlayer);
    
    /**
     * Called when a piece is captured.
     */
    void onPieceCaptured(Piece piece);
    
    /**
     * Called when a move is undone.
     */
    void onMoveUndone(Move move);
    
    /**
     * Called when legal moves are calculated for a selected piece.
     */
    default void onLegalMovesCalculated(Position from, java.util.List<Position> legalMoves) {
        // Default empty implementation for observers that don't need this
    }
}
