package chess.model;

import java.util.Objects;

/**
 * Represents a chess move from one position to another.
 */
public class Move {
    
    private final Position from;
    private final Position to;
    private final Piece piece;
    private final Piece capturedPiece;
    private final MoveType moveType;
    private final PieceType promotionType;
    
    public enum MoveType {
        NORMAL,
        CAPTURE,
        CASTLE_KINGSIDE,
        CASTLE_QUEENSIDE,
        EN_PASSANT,
        PROMOTION,
        PROMOTION_CAPTURE,
        DOUBLE_PAWN_PUSH
    }
    
    public Move(Position from, Position to, Piece piece) {
        this(from, to, piece, null, MoveType.NORMAL, null);
    }
    
    public Move(Position from, Position to, Piece piece, Piece capturedPiece) {
        this(from, to, piece, capturedPiece, 
             capturedPiece != null ? MoveType.CAPTURE : MoveType.NORMAL, null);
    }
    
    public Move(Position from, Position to, Piece piece, Piece capturedPiece, 
                MoveType moveType, PieceType promotionType) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.moveType = moveType;
        this.promotionType = promotionType;
    }
    
    public Position getFrom() {
        return from;
    }
    
    public Position getTo() {
        return to;
    }
    
    public Piece getPiece() {
        return piece;
    }
    
    public Piece getCapturedPiece() {
        return capturedPiece;
    }
    
    public MoveType getMoveType() {
        return moveType;
    }
    
    public PieceType getPromotionType() {
        return promotionType;
    }
    
    public boolean isCapture() {
        return capturedPiece != null || moveType == MoveType.EN_PASSANT;
    }
    
    public boolean isCastling() {
        return moveType == MoveType.CASTLE_KINGSIDE || moveType == MoveType.CASTLE_QUEENSIDE;
    }
    
    public boolean isPromotion() {
        return moveType == MoveType.PROMOTION || moveType == MoveType.PROMOTION_CAPTURE;
    }
    
    /**
     * Converts this move to algebraic notation.
     */
    public String toAlgebraic() {
        StringBuilder sb = new StringBuilder();
        
        if (moveType == MoveType.CASTLE_KINGSIDE) {
            return "O-O";
        } else if (moveType == MoveType.CASTLE_QUEENSIDE) {
            return "O-O-O";
        }
        
        // Piece notation (except for pawns)
        if (piece.getType() != PieceType.PAWN) {
            sb.append(piece.getNotation());
        }
        
        // From file for pawn captures
        if (piece.getType() == PieceType.PAWN && isCapture()) {
            sb.append((char) ('a' + from.getFile()));
        }
        
        // Capture symbol
        if (isCapture()) {
            sb.append('x');
        }
        
        // Destination
        sb.append(to.toAlgebraic());
        
        // Promotion
        if (isPromotion() && promotionType != null) {
            sb.append('=').append(promotionType.getNotation());
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Objects.equals(from, move.from) && 
               Objects.equals(to, move.to) &&
               moveType == move.moveType &&
               promotionType == move.promotionType;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(from, to, moveType, promotionType);
    }
    
    @Override
    public String toString() {
        return toAlgebraic();
    }
}
