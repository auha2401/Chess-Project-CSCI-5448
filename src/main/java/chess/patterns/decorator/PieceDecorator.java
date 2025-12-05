package chess.patterns.decorator;

import chess.model.*;
import chess.patterns.strategy.MoveStrategy;
import java.util.List;

/**
 * Decorator Pattern: Abstract decorator for chess pieces.
 * 
 * Allows adding behavior to pieces without modifying the original classes.
 * Used for tracking additional state like castling rights, promotion status, etc.
 */
public abstract class PieceDecorator implements Piece {
    
    protected final Piece wrappedPiece;
    
    protected PieceDecorator(Piece piece) {
        this.wrappedPiece = piece;
    }
    
    @Override
    public PieceType getType() {
        return wrappedPiece.getType();
    }
    
    @Override
    public Color getColor() {
        return wrappedPiece.getColor();
    }
    
    @Override
    public MoveStrategy getMoveStrategy() {
        return wrappedPiece.getMoveStrategy();
    }
    
    @Override
    public List<Position> getPotentialMoves(Position from) {
        return wrappedPiece.getPotentialMoves(from);
    }
    
    @Override
    public String getSymbol() {
        return wrappedPiece.getSymbol();
    }
    
    @Override
    public String getNotation() {
        return wrappedPiece.getNotation();
    }
    
    @Override
    public int getValue() {
        return wrappedPiece.getValue();
    }
    
    @Override
    public boolean hasMoved() {
        return wrappedPiece.hasMoved();
    }
    
    @Override
    public void setMoved(boolean moved) {
        wrappedPiece.setMoved(moved);
    }
    
    /**
     * Returns the underlying piece.
     */
    public Piece getWrappedPiece() {
        return wrappedPiece;
    }
}
