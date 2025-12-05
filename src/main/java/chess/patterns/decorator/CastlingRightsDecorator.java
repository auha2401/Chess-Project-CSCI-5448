package chess.patterns.decorator;

import chess.model.*;
import java.util.List;

/**
 * Decorator Pattern: Tracks castling rights for kings and rooks.
 * 
 * Adds castling-specific behavior to kings and rooks, including tracking
 * whether they can still castle and providing castling move options.
 */
public class CastlingRightsDecorator extends PieceDecorator {
    
    private boolean canCastleKingside;
    private boolean canCastleQueenside;
    
    public CastlingRightsDecorator(Piece piece) {
        super(piece);
        // Initially can castle if piece hasn't moved
        boolean isKingOrRook = piece.getType() == PieceType.KING || 
                               piece.getType() == PieceType.ROOK;
        this.canCastleKingside = isKingOrRook && !piece.hasMoved();
        this.canCastleQueenside = isKingOrRook && !piece.hasMoved();
    }
    
    @Override
    public List<Position> getPotentialMoves(Position from) {
        List<Position> moves = super.getPotentialMoves(from);
        
        // Add castling moves for king
        if (wrappedPiece.getType() == PieceType.KING && !wrappedPiece.hasMoved()) {
            int rank = wrappedPiece.getColor() == Color.WHITE ? 0 : 7;
            
            if (canCastleKingside) {
                moves.add(new Position(6, rank)); // g1 or g8
            }
            if (canCastleQueenside) {
                moves.add(new Position(2, rank)); // c1 or c8
            }
        }
        
        return moves;
    }
    
    @Override
    public void setMoved(boolean moved) {
        super.setMoved(moved);
        if (moved) {
            // Once moved, can no longer castle
            canCastleKingside = false;
            canCastleQueenside = false;
        }
    }
    
    @Override
    public Piece copy() {
        CastlingRightsDecorator copy = new CastlingRightsDecorator(wrappedPiece.copy());
        copy.canCastleKingside = this.canCastleKingside;
        copy.canCastleQueenside = this.canCastleQueenside;
        return copy;
    }
    
    public boolean canCastleKingside() {
        return canCastleKingside && !wrappedPiece.hasMoved();
    }
    
    public boolean canCastleQueenside() {
        return canCastleQueenside && !wrappedPiece.hasMoved();
    }
    
    public void disableKingsideCastling() {
        this.canCastleKingside = false;
    }
    
    public void disableQueensideCastling() {
        this.canCastleQueenside = false;
    }
}
