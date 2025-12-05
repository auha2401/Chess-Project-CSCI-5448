package chess.patterns.decorator;

import chess.model.*;
import chess.patterns.strategy.MoveStrategy;
import java.util.List;

/**
 * Decorator Pattern: Decorates a promoted pawn with the abilities of another piece.
 * 
 * Maintains the pawn's identity (for move history/notation) while giving it
 * the movement capabilities of the promotion piece (queen, rook, bishop, knight).
 */
public class PromotedPawnDecorator extends PieceDecorator {
    
    private final PieceType promotedTo;
    private final Piece promotionPiece;
    private final int moveNumberPromoted;
    
    public PromotedPawnDecorator(Piece pawn, PieceType promotedTo, int moveNumber) {
        super(pawn);
        this.promotedTo = promotedTo;
        this.moveNumberPromoted = moveNumber;
        this.promotionPiece = createPromotionPiece(promotedTo, pawn.getColor());
    }
    
    private Piece createPromotionPiece(PieceType type, Color color) {
        return switch (type) {
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            default -> new Queen(color);
        };
    }
    
    @Override
    public PieceType getType() {
        return promotedTo; // Report as the promoted piece type
    }
    
    @Override
    public MoveStrategy getMoveStrategy() {
        return promotionPiece.getMoveStrategy();
    }
    
    @Override
    public List<Position> getPotentialMoves(Position from) {
        return promotionPiece.getPotentialMoves(from);
    }
    
    @Override
    public String getSymbol() {
        return promotionPiece.getSymbol();
    }
    
    @Override
    public String getNotation() {
        return promotionPiece.getNotation();
    }
    
    @Override
    public int getValue() {
        return promotionPiece.getValue();
    }
    
    @Override
    public Piece copy() {
        PromotedPawnDecorator copy = new PromotedPawnDecorator(
            wrappedPiece.copy(), promotedTo, moveNumberPromoted);
        copy.setMoved(this.hasMoved());
        return copy;
    }
    
    /**
     * Returns the piece type this pawn was promoted to.
     */
    public PieceType getPromotedTo() {
        return promotedTo;
    }
    
    /**
     * Returns true if this was originally a pawn.
     */
    public boolean wasPromotedPawn() {
        return true;
    }
    
    /**
     * Returns the move number when promotion occurred.
     */
    public int getMoveNumberPromoted() {
        return moveNumberPromoted;
    }
}
