package chess.model;

import java.util.Objects;

/**
 * Represents a position on the chess board.
 * Uses 0-indexed coordinates where (0,0) is a1 (bottom-left for white).
 */
public class Position {
    private final int file; // Column (0-7, a-h)
    private final int rank; // Row (0-7, 1-8)

    public Position(int file, int rank) {
        this.file = file;
        this.rank = rank;
    }

    /**
     * Creates a Position from algebraic notation (e.g., "e4").
     */
    public static Position fromAlgebraic(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid notation: " + notation);
        }
        int file = notation.charAt(0) - 'a';
        int rank = notation.charAt(1) - '1';
        return new Position(file, rank);
    }

    public int getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    /**
     * Returns true if this position is within the board bounds.
     */
    public boolean isValid() {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }

    /**
     * Returns a new position offset by the given deltas.
     */
    public Position offset(int deltaFile, int deltaRank) {
        return new Position(file + deltaFile, rank + deltaRank);
    }

    /**
     * Converts to algebraic notation (e.g., "e4").
     */
    public String toAlgebraic() {
        return "" + (char) ('a' + file) + (char) ('1' + rank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return file == position.file && rank == position.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, rank);
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}
