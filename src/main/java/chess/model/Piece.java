package chess.model;

import chess.patterns.strategy.MoveStrategy;
import java.util.List;

/**
 * Interface representing a chess piece.
 * This is the core abstraction that allows polymorphic treatment of all pieces.
 */
public interface Piece {
    
    /**
     * Returns the type of this piece.
     */
    PieceType getType();
    
    /**
     * Returns the color of this piece.
     */
    Color getColor();
    
    /**
     * Returns the movement strategy for this piece.
     */
    MoveStrategy getMoveStrategy();
    
    /**
     * Returns a list of all possible target positions this piece can move to,
     * without considering board state (other pieces, check, etc.).
     */
    List<Position> getPotentialMoves(Position from);
    
    /**
     * Returns the Unicode symbol for displaying this piece.
     */
    default String getSymbol() {
        return getType().getSymbol(getColor());
    }
    
    /**
     * Returns the algebraic notation character for this piece.
     */
    default String getNotation() {
        return getType().getNotation();
    }
    
    /**
     * Returns the material value of this piece.
     */
    default int getValue() {
        return getType().getValue();
    }
    
    /**
     * Returns true if this piece has moved (relevant for castling and pawn double-move).
     */
    boolean hasMoved();
    
    /**
     * Marks this piece as having moved.
     */
    void setMoved(boolean moved);
    
    /**
     * Creates a copy of this piece.
     */
    Piece copy();
}
