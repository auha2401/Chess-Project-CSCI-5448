package chess.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the chess board.
 * Uses a map-based representation for flexibility and easy iteration.
 */
public class Board {
    
    private final Map<Position, Piece> pieces;
    private Position enPassantTarget; // Square where en passant capture is possible
    
    public Board() {
        this.pieces = new HashMap<>();
        this.enPassantTarget = null;
    }
    
    /**
     * Copy constructor for creating board states.
     */
    public Board(Board other) {
        this.pieces = new HashMap<>();
        for (Map.Entry<Position, Piece> entry : other.pieces.entrySet()) {
            this.pieces.put(entry.getKey(), entry.getValue().copy());
        }
        this.enPassantTarget = other.enPassantTarget;
    }
    
    /**
     * Places a piece at the given position.
     */
    public void setPiece(Position position, Piece piece) {
        if (piece == null) {
            pieces.remove(position);
        } else {
            pieces.put(position, piece);
        }
    }
    
    /**
     * Gets the piece at the given position, if any.
     */
    public Optional<Piece> getPiece(Position position) {
        return Optional.ofNullable(pieces.get(position));
    }
    
    /**
     * Gets the piece at the given position, or null if empty.
     */
    public Piece getPieceAt(Position position) {
        return pieces.get(position);
    }
    
    /**
     * Removes and returns the piece at the given position.
     */
    public Piece removePiece(Position position) {
        return pieces.remove(position);
    }
    
    /**
     * Checks if a position is empty.
     */
    public boolean isEmpty(Position position) {
        return !pieces.containsKey(position);
    }
    
    /**
     * Checks if a position is occupied by a piece of the given color.
     */
    public boolean isOccupiedByColor(Position position, Color color) {
        Piece piece = pieces.get(position);
        return piece != null && piece.getColor() == color;
    }
    
    /**
     * Returns all pieces on the board with their positions.
     */
    public Map<Position, Piece> getAllPieces() {
        return new HashMap<>(pieces);
    }
    
    /**
     * Finds the position of the king of the given color.
     */
    public Position findKing(Color color) {
        for (Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getType() == PieceType.KING && piece.getColor() == color) {
                return entry.getKey();
            }
        }
        return null; // Should never happen in a valid game
    }
    
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }
    
    public void setEnPassantTarget(Position enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }
    
    /**
     * Checks if the path between two positions is clear (no pieces blocking).
     * Used for validating sliding piece moves and castling.
     */
    public boolean isPathClear(Position from, Position to) {
        int deltaFile = Integer.compare(to.getFile(), from.getFile());
        int deltaRank = Integer.compare(to.getRank(), from.getRank());
        
        Position current = from.offset(deltaFile, deltaRank);
        while (!current.equals(to)) {
            if (!isEmpty(current)) {
                return false;
            }
            current = current.offset(deltaFile, deltaRank);
        }
        return true;
    }
    
    /**
     * Creates a deep copy of this board.
     */
    public Board copy() {
        return new Board(this);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h\n");
        for (int rank = 7; rank >= 0; rank--) {
            sb.append(rank + 1).append(" ");
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                Piece piece = pieces.get(pos);
                if (piece != null) {
                    sb.append(piece.getSymbol()).append(" ");
                } else {
                    sb.append(". ");
                }
            }
            sb.append(rank + 1).append("\n");
        }
        sb.append("  a b c d e f g h");
        return sb.toString();
    }
}
