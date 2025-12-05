package chess.model;

import chess.patterns.strategy.DiagonalMoveStrategy;

/**
 * Represents a Bishop piece.
 */
public class Bishop extends AbstractPiece {
    
    private static final DiagonalMoveStrategy STRATEGY = new DiagonalMoveStrategy();
    
    public Bishop(Color color) {
        super(PieceType.BISHOP, color, STRATEGY);
    }
    
    private Bishop(Color color, boolean moved) {
        super(PieceType.BISHOP, color, STRATEGY);
        this.moved = moved;
    }
    
    @Override
    public Piece copy() {
        return new Bishop(color, moved);
    }
}
