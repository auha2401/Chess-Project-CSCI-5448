package chess.model;

import chess.patterns.strategy.KingMoveStrategy;

/**
 * Represents a King piece.
 */
public class King extends AbstractPiece {
    
    private static final KingMoveStrategy STRATEGY = new KingMoveStrategy();
    
    public King(Color color) {
        super(PieceType.KING, color, STRATEGY);
    }
    
    private King(Color color, boolean moved) {
        super(PieceType.KING, color, STRATEGY);
        this.moved = moved;
    }
    
    @Override
    public Piece copy() {
        return new King(color, moved);
    }
}
