package chess.controller;

import chess.model.*;
import chess.patterns.strategy.MoveStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Validates chess moves according to the rules.
 * Uses the Strategy pattern via piece movement strategies.
 */
public class MoveValidator {
    
    /**
     * Returns all legal moves for a piece at the given position.
     */
    public List<Move> getLegalMoves(Board board, Position from, Color currentPlayer) {
        List<Move> legalMoves = new ArrayList<>();
        Piece piece = board.getPieceAt(from);
        
        if (piece == null || piece.getColor() != currentPlayer) {
            return legalMoves;
        }
        
        // Get potential moves from the piece's strategy
        List<Position> potentialTargets = piece.getPotentialMoves(from);
        
        for (Position to : potentialTargets) {
            Move move = validateMove(board, from, to, currentPlayer);
            if (move != null) {
                legalMoves.add(move);
            }
        }
        
        // Add castling moves for king
        if (piece.getType() == PieceType.KING && !piece.hasMoved()) {
            addCastlingMoves(board, from, piece, currentPlayer, legalMoves);
        }
        
        return legalMoves;
    }
    
    /**
     * Validates a specific move and returns the Move object if legal, null otherwise.
     */
    public Move validateMove(Board board, Position from, Position to, Color currentPlayer) {
        Piece piece = board.getPieceAt(from);
        
        if (piece == null || piece.getColor() != currentPlayer) {
            return null;
        }
        
        // Check if target is valid for piece type
        if (!isValidTargetForPiece(board, piece, from, to)) {
            return null;
        }
        
        // Check for blocking pieces (for sliding pieces)
        MoveStrategy strategy = piece.getMoveStrategy();
        if (strategy.isSliding() && !board.isPathClear(from, to)) {
            return null;
        }
        
        // Determine move type and captured piece
        Piece capturedPiece = board.getPieceAt(to);
        Move.MoveType moveType = determineMoveType(board, piece, from, to, capturedPiece);
        
        // Handle en passant capture
        if (moveType == Move.MoveType.EN_PASSANT) {
            int direction = piece.getColor().getPawnDirection();
            Position capturePos = to.offset(0, -direction);
            capturedPiece = board.getPieceAt(capturePos);
        }
        
        // Create move with promotion type if applicable
        PieceType promotionType = null;
        if (piece.getType() == PieceType.PAWN && to.getRank() == piece.getColor().getPromotionRank()) {
            promotionType = PieceType.QUEEN; // Default to queen, UI can change this
            moveType = capturedPiece != null ? Move.MoveType.PROMOTION_CAPTURE : Move.MoveType.PROMOTION;
        }
        
        Move move = new Move(from, to, piece, capturedPiece, moveType, promotionType);
        
        // Check if move leaves own king in check
        if (wouldLeaveKingInCheck(board, move, currentPlayer)) {
            return null;
        }
        
        return move;
    }
    
    private boolean isValidTargetForPiece(Board board, Piece piece, Position from, Position to) {
        // Can't capture own pieces
        Piece targetPiece = board.getPieceAt(to);
        if (targetPiece != null && targetPiece.getColor() == piece.getColor()) {
            return false;
        }
        
        // Special handling for pawns
        if (piece.getType() == PieceType.PAWN) {
            return isValidPawnMove(board, piece, from, to);
        }
        
        // Check if target is in piece's potential moves
        return piece.getPotentialMoves(from).contains(to);
    }
    
    private boolean isValidPawnMove(Board board, Piece pawn, Position from, Position to) {
        int direction = pawn.getColor().getPawnDirection();
        int fileDiff = to.getFile() - from.getFile();
        int rankDiff = to.getRank() - from.getRank();
        
        // Forward moves (must be to empty square)
        if (fileDiff == 0) {
            if (rankDiff == direction) {
                return board.isEmpty(to);
            }
            if (rankDiff == 2 * direction && from.getRank() == pawn.getColor().getPawnStartRank()) {
                Position intermediate = from.offset(0, direction);
                return board.isEmpty(intermediate) && board.isEmpty(to);
            }
        }
        
        // Diagonal captures
        if (Math.abs(fileDiff) == 1 && rankDiff == direction) {
            // Regular capture
            Piece target = board.getPieceAt(to);
            if (target != null && target.getColor() != pawn.getColor()) {
                return true;
            }
            // En passant
            return to.equals(board.getEnPassantTarget());
        }
        
        return false;
    }
    
    private Move.MoveType determineMoveType(Board board, Piece piece, Position from, 
                                            Position to, Piece capturedPiece) {
        // En passant
        if (piece.getType() == PieceType.PAWN && to.equals(board.getEnPassantTarget())) {
            return Move.MoveType.EN_PASSANT;
        }
        
        // Double pawn push
        if (piece.getType() == PieceType.PAWN) {
            int rankDiff = Math.abs(to.getRank() - from.getRank());
            if (rankDiff == 2) {
                return Move.MoveType.DOUBLE_PAWN_PUSH;
            }
        }
        
        // Regular capture or normal move
        if (capturedPiece != null) {
            return Move.MoveType.CAPTURE;
        }
        
        return Move.MoveType.NORMAL;
    }
    
    private void addCastlingMoves(Board board, Position kingPos, Piece king, 
                                  Color color, List<Move> moves) {
        int rank = color == Color.WHITE ? 0 : 7;
        
        // Check kingside castling (O-O)
        if (canCastleKingside(board, color, rank)) {
            Position to = new Position(6, rank);
            Move castleMove = new Move(kingPos, to, king, null, 
                                       Move.MoveType.CASTLE_KINGSIDE, null);
            if (!wouldLeaveKingInCheck(board, castleMove, color)) {
                moves.add(castleMove);
            }
        }
        
        // Check queenside castling (O-O-O)
        if (canCastleQueenside(board, color, rank)) {
            Position to = new Position(2, rank);
            Move castleMove = new Move(kingPos, to, king, null, 
                                       Move.MoveType.CASTLE_QUEENSIDE, null);
            if (!wouldLeaveKingInCheck(board, castleMove, color)) {
                moves.add(castleMove);
            }
        }
    }
    
    private boolean canCastleKingside(Board board, Color color, int rank) {
        // Check rook exists and hasn't moved
        Position rookPos = new Position(7, rank);
        Piece rook = board.getPieceAt(rookPos);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.hasMoved()) {
            return false;
        }
        
        // Check path is clear
        if (!board.isEmpty(new Position(5, rank)) || !board.isEmpty(new Position(6, rank))) {
            return false;
        }
        
        // Check king doesn't pass through check
        Position kingPos = new Position(4, rank);
        if (isSquareAttacked(board, kingPos, color.opposite())) return false;
        if (isSquareAttacked(board, new Position(5, rank), color.opposite())) return false;
        if (isSquareAttacked(board, new Position(6, rank), color.opposite())) return false;
        
        return true;
    }
    
    private boolean canCastleQueenside(Board board, Color color, int rank) {
        // Check rook exists and hasn't moved
        Position rookPos = new Position(0, rank);
        Piece rook = board.getPieceAt(rookPos);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.hasMoved()) {
            return false;
        }
        
        // Check path is clear
        if (!board.isEmpty(new Position(1, rank)) || 
            !board.isEmpty(new Position(2, rank)) || 
            !board.isEmpty(new Position(3, rank))) {
            return false;
        }
        
        // Check king doesn't pass through check
        Position kingPos = new Position(4, rank);
        if (isSquareAttacked(board, kingPos, color.opposite())) return false;
        if (isSquareAttacked(board, new Position(3, rank), color.opposite())) return false;
        if (isSquareAttacked(board, new Position(2, rank), color.opposite())) return false;
        
        return true;
    }
    
    /**
     * Checks if a square is attacked by any piece of the given color.
     */
    public boolean isSquareAttacked(Board board, Position square, Color attackerColor) {
        for (Map.Entry<Position, Piece> entry : board.getAllPieces().entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getColor() != attackerColor) continue;
            
            Position from = entry.getKey();
            
            // Special handling for pawns (they attack diagonally, not where they move)
            if (piece.getType() == PieceType.PAWN) {
                int direction = piece.getColor().getPawnDirection();
                Position leftAttack = from.offset(-1, direction);
                Position rightAttack = from.offset(1, direction);
                if (square.equals(leftAttack) || square.equals(rightAttack)) {
                    return true;
                }
                continue;
            }
            
            // Check if piece can reach the square
            List<Position> potentialMoves = piece.getPotentialMoves(from);
            if (potentialMoves.contains(square)) {
                // For sliding pieces, check if path is clear
                MoveStrategy strategy = piece.getMoveStrategy();
                if (!strategy.isSliding() || board.isPathClear(from, square)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if the given player's king is in check.
     */
    public boolean isInCheck(Board board, Color color) {
        Position kingPos = board.findKing(color);
        if (kingPos == null) return false;
        return isSquareAttacked(board, kingPos, color.opposite());
    }
    
    /**
     * Checks if making a move would leave the moving player's king in check.
     */
    public boolean wouldLeaveKingInCheck(Board board, Move move, Color color) {
        // Make a copy of the board and apply the move
        Board testBoard = board.copy();
        applyMoveToBoard(testBoard, move);
        return isInCheck(testBoard, color);
    }
    
    private void applyMoveToBoard(Board board, Move move) {
        board.removePiece(move.getFrom());
        
        // Handle en passant capture
        if (move.getMoveType() == Move.MoveType.EN_PASSANT) {
            int direction = move.getPiece().getColor().getPawnDirection();
            board.removePiece(move.getTo().offset(0, -direction));
        }
        
        board.setPiece(move.getTo(), move.getPiece());
        
        // Handle castling
        if (move.isCastling()) {
            int rank = move.getFrom().getRank();
            if (move.getMoveType() == Move.MoveType.CASTLE_KINGSIDE) {
                Piece rook = board.removePiece(new Position(7, rank));
                board.setPiece(new Position(5, rank), rook);
            } else {
                Piece rook = board.removePiece(new Position(0, rank));
                board.setPiece(new Position(3, rank), rook);
            }
        }
    }
    
    /**
     * Checks if the given player has any legal moves.
     */
    public boolean hasLegalMoves(Board board, Color color) {
        for (Map.Entry<Position, Piece> entry : board.getAllPieces().entrySet()) {
            if (entry.getValue().getColor() == color) {
                List<Move> moves = getLegalMoves(board, entry.getKey(), color);
                if (!moves.isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Determines the game state for the given player.
     */
    public GameState getGameState(Board board, Color currentPlayer) {
        boolean inCheck = isInCheck(board, currentPlayer);
        boolean hasLegalMoves = hasLegalMoves(board, currentPlayer);
        
        if (!hasLegalMoves) {
            if (inCheck) {
                return GameState.CHECKMATE;
            } else {
                return GameState.STALEMATE;
            }
        }
        
        if (inCheck) {
            return GameState.CHECK;
        }
        
        return GameState.IN_PROGRESS;
    }
}
