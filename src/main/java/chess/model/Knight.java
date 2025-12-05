package chess.model;

import chess.patterns.strategy.KnightMoveStrategy;

/**
 * Represents a Knight piece.
 */
public class Knight extends AbstractPiece {
    
    private static final KnightMoveStrategy STRATEGY = new KnightMoveStrategy();
    
    public Knight(Color color) {
        super(PieceType.KNIGHT, color, STRATEGY);
    }
    
    private Knight(Color color, boolean moved) {
        super(PieceType.KNIGHT, color, STRATEGY);
        this.moved = moved;
    }
    
    @Override
    public Piece copy() {
        return new Knight(color, moved);
    }
}
