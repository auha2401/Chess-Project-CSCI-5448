package chess.patterns.command;

import chess.model.*;

/**
 * Command Pattern: Encapsulates a chess move as an executable command.
 * 
 * Stores all information needed to execute and undo a move,
 * including special moves like castling, en passant, and promotion.
 */
public class MoveCommand implements Command {
    
    private final Board board;
    private final Move move;
    
    // State needed for undo
    private final Piece movedPiece;
    private final Piece capturedPiece;
    private final Position capturePosition; // Different from 'to' for en passant
    private final boolean pieceHadMoved;
    private final Position previousEnPassantTarget;
    
    // For castling
    private final Position rookFrom;
    private final Position rookTo;
    private final Piece rook;
    private final boolean rookHadMoved;
    
    // For promotion
    private final Piece promotedPiece;
    
    public MoveCommand(Board board, Move move) {
        this.board = board;
        this.move = move;
        this.movedPiece = board.getPieceAt(move.getFrom());
        this.pieceHadMoved = movedPiece != null && movedPiece.hasMoved();
        this.previousEnPassantTarget = board.getEnPassantTarget();
        
        // Determine capture position (different for en passant)
        if (move.getMoveType() == Move.MoveType.EN_PASSANT) {
            int direction = movedPiece.getColor().getPawnDirection();
            this.capturePosition = move.getTo().offset(0, -direction);
            this.capturedPiece = board.getPieceAt(capturePosition);
        } else {
            this.capturePosition = move.getTo();
            this.capturedPiece = move.getCapturedPiece();
        }
        
        // Set up castling info
        if (move.isCastling()) {
            int rank = move.getFrom().getRank();
            if (move.getMoveType() == Move.MoveType.CASTLE_KINGSIDE) {
                this.rookFrom = new Position(7, rank);
                this.rookTo = new Position(5, rank);
            } else {
                this.rookFrom = new Position(0, rank);
                this.rookTo = new Position(3, rank);
            }
            this.rook = board.getPieceAt(rookFrom);
            this.rookHadMoved = rook != null && rook.hasMoved();
        } else {
            this.rookFrom = null;
            this.rookTo = null;
            this.rook = null;
            this.rookHadMoved = false;
        }
        
        // Set up promotion piece
        if (move.isPromotion() && move.getPromotionType() != null) {
            this.promotedPiece = createPromotionPiece(move.getPromotionType(), movedPiece.getColor());
        } else {
            this.promotedPiece = null;
        }
    }
    
    private Piece createPromotionPiece(PieceType type, Color color) {
        return switch (type) {
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            default -> new Queen(color); // Default to queen
        };
    }
    
    @Override
    public void execute() {
        // Remove piece from starting position
        board.removePiece(move.getFrom());
        
        // Handle capture (including en passant)
        if (capturedPiece != null) {
            board.removePiece(capturePosition);
        }
        
        // Place piece at destination (or promoted piece)
        Piece pieceToPlace = promotedPiece != null ? promotedPiece : movedPiece;
        board.setPiece(move.getTo(), pieceToPlace);
        pieceToPlace.setMoved(true);
        
        // Handle castling - move the rook
        if (move.isCastling() && rook != null) {
            board.removePiece(rookFrom);
            board.setPiece(rookTo, rook);
            rook.setMoved(true);
        }
        
        // Update en passant target
        if (move.getMoveType() == Move.MoveType.DOUBLE_PAWN_PUSH) {
            int direction = movedPiece.getColor().getPawnDirection();
            board.setEnPassantTarget(move.getFrom().offset(0, direction));
        } else {
            board.setEnPassantTarget(null);
        }
    }
    
    @Override
    public void undo() {
        // Restore piece to starting position
        board.setPiece(move.getFrom(), movedPiece);
        movedPiece.setMoved(pieceHadMoved);
        
        // Remove piece from destination
        board.removePiece(move.getTo());
        
        // Restore captured piece
        if (capturedPiece != null) {
            board.setPiece(capturePosition, capturedPiece);
        }
        
        // Undo castling - restore the rook
        if (move.isCastling() && rook != null) {
            board.removePiece(rookTo);
            board.setPiece(rookFrom, rook);
            rook.setMoved(rookHadMoved);
        }
        
        // Restore en passant target
        board.setEnPassantTarget(previousEnPassantTarget);
    }
    
    @Override
    public String getDescription() {
        return move.toAlgebraic();
    }
    
    public Move getMove() {
        return move;
    }
    
    public Piece getCapturedPiece() {
        return capturedPiece;
    }
}
