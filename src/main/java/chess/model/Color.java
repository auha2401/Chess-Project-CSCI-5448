package chess.model;

/**
 * Represents the two colors in chess.
 */
public enum Color {
    WHITE,
    BLACK;

    /**
     * Returns the opposite color.
     */
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /**
     * Returns the direction pawns move for this color (1 for white, -1 for black).
     */
    public int getPawnDirection() {
        return this == WHITE ? 1 : -1;
    }

    /**
     * Returns the starting rank for pawns of this color.
     */
    public int getPawnStartRank() {
        return this == WHITE ? 1 : 6;
    }

    /**
     * Returns the promotion rank for pawns of this color.
     */
    public int getPromotionRank() {
        return this == WHITE ? 7 : 0;
    }
}
