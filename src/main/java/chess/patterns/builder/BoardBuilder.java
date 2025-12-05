package chess.patterns.builder;

import chess.model.*;

/**
 * Builder Pattern: Constructs chess boards with flexible configuration.
 * 
 * Allows creating standard starting positions or custom board setups
 * without exposing the complexity of board construction.
 */
public class BoardBuilder {
    
    private Board board;
    
    public BoardBuilder() {
        this.board = new Board();
    }
    
    /**
     * Sets up the standard starting position.
     */
    public BoardBuilder withStandardSetup() {
        // Clear any existing pieces
        board = new Board();
        
        // Set up white pieces
        setupRank(0, Color.WHITE);
        setupPawns(1, Color.WHITE);
        
        // Set up black pieces
        setupRank(7, Color.BLACK);
        setupPawns(6, Color.BLACK);
        
        return this;
    }
    
    private void setupRank(int rank, Color color) {
        board.setPiece(new Position(0, rank), new Rook(color));
        board.setPiece(new Position(1, rank), new Knight(color));
        board.setPiece(new Position(2, rank), new Bishop(color));
        board.setPiece(new Position(3, rank), new Queen(color));
        board.setPiece(new Position(4, rank), new King(color));
        board.setPiece(new Position(5, rank), new Bishop(color));
        board.setPiece(new Position(6, rank), new Knight(color));
        board.setPiece(new Position(7, rank), new Rook(color));
    }
    
    private void setupPawns(int rank, Color color) {
        for (int file = 0; file < 8; file++) {
            board.setPiece(new Position(file, rank), new Pawn(color));
        }
    }
    
    /**
     * Places a piece at the specified position.
     */
    public BoardBuilder withPiece(String position, Piece piece) {
        board.setPiece(Position.fromAlgebraic(position), piece);
        return this;
    }
    
    /**
     * Places a piece at the specified position.
     */
    public BoardBuilder withPiece(Position position, Piece piece) {
        board.setPiece(position, piece);
        return this;
    }
    
    /**
     * Sets up a custom position from FEN notation (simplified - just piece placement).
     */
    public BoardBuilder fromFEN(String fen) {
        board = new Board();
        String[] parts = fen.split(" ");
        String[] ranks = parts[0].split("/");
        
        for (int rank = 7; rank >= 0; rank--) {
            int file = 0;
            for (char c : ranks[7 - rank].toCharArray()) {
                if (Character.isDigit(c)) {
                    file += Character.getNumericValue(c);
                } else {
                    Color color = Character.isUpperCase(c) ? Color.WHITE : Color.BLACK;
                    Piece piece = createPiece(Character.toUpperCase(c), color);
                    if (piece != null) {
                        board.setPiece(new Position(file, rank), piece);
                    }
                    file++;
                }
            }
        }
        
        return this;
    }
    
    private Piece createPiece(char type, Color color) {
        return switch (type) {
            case 'P' -> new Pawn(color);
            case 'N' -> new Knight(color);
            case 'B' -> new Bishop(color);
            case 'R' -> new Rook(color);
            case 'Q' -> new Queen(color);
            case 'K' -> new King(color);
            default -> null;
        };
    }
    
    /**
     * Creates an empty board.
     */
    public BoardBuilder empty() {
        board = new Board();
        return this;
    }
    
    /**
     * Builds and returns the configured board.
     */
    public Board build() {
        return board;
    }
}
