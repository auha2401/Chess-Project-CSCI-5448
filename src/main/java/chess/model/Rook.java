package chess.model;

import chess.patterns.strategy.StraightMoveStrategy;

/**
 * Represents a Rook piece.
 */
public class Rook extends AbstractPiece {
    
    private static final StraightMoveStrategy STRATEGY = new StraightMoveStrategy();
    
    public Rook(Color color) {
        super(PieceType.ROOK, color, STRATEGY);
    }
    
    private Rook(Color color, boolean moved) {
        super(PieceType.ROOK, color, STRATEGY);
        this.moved = moved;
    }
    
    @Override
    public Piece copy() {
        return new Rook(color, moved);
    }
}
