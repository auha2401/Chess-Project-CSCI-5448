package chess.util;

import chess.controller.Game;
import chess.model.*;
import chess.patterns.builder.BoardBuilder;
import chess.patterns.builder.GameBuilder;
import chess.patterns.command.Command;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles saving and loading chess games using algebraic notation.
 */
public class GamePersistence {
    
    private static final Pattern MOVE_PATTERN = Pattern.compile(
        "^(\\d+)\\.\\s*(\\S+)(?:\\s+(\\S+))?$"
    );
    
    /**
     * Saves the game to a file in PGN-like format.
     */
    public void saveGame(Game game, String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write headers
            writer.println("[Event \"Chess Game\"]");
            writer.println("[Date \"" + java.time.LocalDate.now() + "\"]");
            writer.println("[White \"Player 1\"]");
            writer.println("[Black \"Player 2\"]");
            writer.println("[Result \"" + getResultString(game.getGameState()) + "\"]");
            writer.println();
            
            // Write moves
            List<Command> commands = game.getCommandHistory().getAllCommands();
            int moveNum = 1;
            StringBuilder line = new StringBuilder();
            
            for (int i = 0; i < commands.size(); i++) {
                if (i % 2 == 0) {
                    if (line.length() > 60) {
                        writer.println(line);
                        line = new StringBuilder();
                    }
                    line.append(moveNum).append(". ");
                    moveNum++;
                }
                line.append(commands.get(i).getDescription()).append(" ");
            }
            
            if (line.length() > 0) {
                writer.println(line.toString().trim());
            }
            
            // Write result
            writer.println(getResultString(game.getGameState()));
        }
    }

    /**
     * Saves the current position to a file as a single FEN string.
     */
    public void saveFEN(Game game, String filename) throws IOException {
        String fen = toFEN(game.getBoard(), game.getCurrentPlayer(), game.getMoveNumber(), game.getHalfMoveClock());
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println(fen);
        }
    }
    
    /**
     * Saves the current board position in FEN format.
     */
    public String toFEN(Board board, Color currentPlayer, int moveNumber) {
        return toFEN(board, currentPlayer, moveNumber, 0);
    }

    /**
     * Saves the current board position in FEN format including halfmove clock.
     */
    public String toFEN(Board board, Color currentPlayer, int moveNumber, int halfMoveClock) {
        StringBuilder fen = new StringBuilder();
        
        // Piece placement
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file < 8; file++) {
                Piece piece = board.getPieceAt(new Position(file, rank));
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    fen.append(pieceToFENChar(piece));
                }
            }
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            if (rank > 0) {
                fen.append("/");
            }
        }
        
        // Active color
        fen.append(" ").append(currentPlayer == Color.WHITE ? "w" : "b");
        
        // Castling availability based on piece moved flags
        fen.append(" ").append(getCastlingRights(board));
        
        // En passant target
        Position epTarget = board.getEnPassantTarget();
        if (epTarget != null) {
            fen.append(" ").append(epTarget.toAlgebraic());
        } else {
            fen.append(" -");
        }
        
        // Halfmove clock
        fen.append(" ").append(halfMoveClock);
        
        // Fullmove number
        fen.append(" ").append(moveNumber);
        
        return fen.toString();
    }
    
    /**
     * Loads a game from FEN position.
     */
    public Game loadFromFEN(String fen) {
        String[] parts = fen.split(" ");
        
        Board board = new BoardBuilder().fromFEN(fen).build();
        Color currentPlayer = parts[1].equals("w") ? Color.WHITE : Color.BLACK;

        // Castling rights adjust moved flags
        applyCastlingRights(board, parts.length > 2 ? parts[2] : "-");

        // En passant target
        if (parts.length > 3 && !parts[3].equals("-")) {
            board.setEnPassantTarget(Position.fromAlgebraic(parts[3]));
        }

        int halfMoveClock = parts.length > 4 ? parseIntSafe(parts[4], 0) : 0;
        int moveNumber = parts.length > 5 ? parseIntSafe(parts[5], 1) : 1;
        
        Game game = new GameBuilder()
            .withBoard(board)
            .withStartingPlayer(currentPlayer)
            .build();

        game.initializeFromPosition(currentPlayer, moveNumber, halfMoveClock);

        return game;
    }
    
    private char pieceToFENChar(Piece piece) {
        char c = switch (piece.getType()) {
            case PAWN -> 'P';
            case KNIGHT -> 'N';
            case BISHOP -> 'B';
            case ROOK -> 'R';
            case QUEEN -> 'Q';
            case KING -> 'K';
        };
        return piece.getColor() == Color.WHITE ? c : Character.toLowerCase(c);
    }
    
    private String getResultString(GameState state) {
        return switch (state) {
            case CHECKMATE -> "0-1"; // Would need to track who won
            case STALEMATE, DRAW_BY_REPETITION, DRAW_BY_FIFTY_MOVES,
                 DRAW_BY_INSUFFICIENT_MATERIAL, DRAW_BY_AGREEMENT -> "1/2-1/2";
            default -> "*";
        };
    }

    private String getCastlingRights(Board board) {
        StringBuilder rights = new StringBuilder();
        Piece whiteKing = board.getPieceAt(Position.fromAlgebraic("e1"));
        Piece whiteRookA = board.getPieceAt(Position.fromAlgebraic("a1"));
        Piece whiteRookH = board.getPieceAt(Position.fromAlgebraic("h1"));
        Piece blackKing = board.getPieceAt(Position.fromAlgebraic("e8"));
        Piece blackRookA = board.getPieceAt(Position.fromAlgebraic("a8"));
        Piece blackRookH = board.getPieceAt(Position.fromAlgebraic("h8"));

        if (whiteKing instanceof King wk && !wk.hasMoved()) {
            if (whiteRookH instanceof Rook wr && !wr.hasMoved()) rights.append('K');
            if (whiteRookA instanceof Rook wr && !wr.hasMoved()) rights.append('Q');
        }
        if (blackKing instanceof King bk && !bk.hasMoved()) {
            if (blackRookH instanceof Rook br && !br.hasMoved()) rights.append('k');
            if (blackRookA instanceof Rook br && !br.hasMoved()) rights.append('q');
        }
        return rights.length() == 0 ? "-" : rights.toString();
    }

    private void applyCastlingRights(Board board, String rights) {
        // If a right is missing, mark the corresponding king/rook as moved to disable castling.
        Piece whiteKing = board.getPieceAt(Position.fromAlgebraic("e1"));
        Piece whiteRookA = board.getPieceAt(Position.fromAlgebraic("a1"));
        Piece whiteRookH = board.getPieceAt(Position.fromAlgebraic("h1"));
        Piece blackKing = board.getPieceAt(Position.fromAlgebraic("e8"));
        Piece blackRookA = board.getPieceAt(Position.fromAlgebraic("a8"));
        Piece blackRookH = board.getPieceAt(Position.fromAlgebraic("h8"));

        boolean whiteKingSide = rights.contains("K");
        boolean whiteQueenSide = rights.contains("Q");
        boolean blackKingSide = rights.contains("k");
        boolean blackQueenSide = rights.contains("q");

        if (!whiteKingSide && !whiteQueenSide && whiteKing instanceof King wk) {
            wk.setMoved(true);
        }
        if (!whiteKingSide && whiteRookH instanceof Rook wr) {
            wr.setMoved(true);
        }
        if (!whiteQueenSide && whiteRookA instanceof Rook wr) {
            wr.setMoved(true);
        }
        if (!blackKingSide && !blackQueenSide && blackKing instanceof King bk) {
            bk.setMoved(true);
        }
        if (!blackKingSide && blackRookH instanceof Rook br) {
            br.setMoved(true);
        }
        if (!blackQueenSide && blackRookA instanceof Rook br) {
            br.setMoved(true);
        }
    }

    private int parseIntSafe(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
    
    /**
     * Exports the move list in algebraic notation.
     */
    public String exportMoveList(Game game) {
        StringBuilder sb = new StringBuilder();
        List<Command> commands = game.getCommandHistory().getAllCommands();
        
        for (int i = 0; i < commands.size(); i++) {
            if (i % 2 == 0) {
                sb.append((i / 2 + 1)).append(". ");
            }
            sb.append(commands.get(i).getDescription());
            if (i % 2 == 1) {
                sb.append("\n");
            } else {
                sb.append(" ");
            }
        }
        
        return sb.toString().trim();
    }
}
