package chess.model;

import chess.patterns.strategy.CombinedMoveStrategy;

/**
 * Represents a Queen piece.
 */
public class Queen extends AbstractPiece {
    
    private static final CombinedMoveStrategy STRATEGY = new CombinedMoveStrategy();
    
    public Queen(Color color) {
        super(PieceType.QUEEN, color, STRATEGY);
    }
    
    private Queen(Color color, boolean moved) {
        super(PieceType.QUEEN, color, STRATEGY);
        this.moved = moved;
    }
    
    @Override
    public Piece copy() {
        return new Queen(color, moved);
    }
}
