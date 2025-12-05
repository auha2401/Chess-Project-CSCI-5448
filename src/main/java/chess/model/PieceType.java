package chess.model;

/**
 * Represents the different types of chess pieces.
 */
public enum PieceType {
    PAWN("P", "♟", "♙", 1),
    KNIGHT("N", "♞", "♘", 3),
    BISHOP("B", "♝", "♗", 3),
    ROOK("R", "♜", "♖", 5),
    QUEEN("Q", "♛", "♕", 9),
    KING("K", "♚", "♔", 0);

    private final String notation;
    private final String blackSymbol;
    private final String whiteSymbol;
    private final int value;

    PieceType(String notation, String blackSymbol, String whiteSymbol, int value) {
        this.notation = notation;
        this.blackSymbol = blackSymbol;
        this.whiteSymbol = whiteSymbol;
        this.value = value;
    }

    public String getNotation() {
        return notation;
    }

    public String getSymbol(Color color) {
        return color == Color.WHITE ? whiteSymbol : blackSymbol;
    }

    public int getValue() {
        return value;
    }

    /**
     * Parse a piece type from algebraic notation.
     */
    public static PieceType fromNotation(String notation) {
        for (PieceType type : values()) {
            if (type.notation.equalsIgnoreCase(notation)) {
                return type;
            }
        }
        return PAWN; // Default for empty notation
    }
}
