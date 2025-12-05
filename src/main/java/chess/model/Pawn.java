package chess.model;

import chess.patterns.strategy.PawnMoveStrategy;

/**
 * Represents a Pawn piece.
 */
public class Pawn extends AbstractPiece {
    
    public Pawn(Color color) {
        super(PieceType.PAWN, color, new PawnMoveStrategy(color));
    }
    
    private Pawn(Color color, boolean moved) {
        super(PieceType.PAWN, color, new PawnMoveStrategy(color));
        this.moved = moved;
    }
    
    @Override
    public Piece copy() {
        return new Pawn(color, moved);
    }
}
