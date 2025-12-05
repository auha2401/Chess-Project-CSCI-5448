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
     * Saves the current board position in FEN format.
     */
    public String toFEN(Board board, Color currentPlayer, int moveNumber) {
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
        
        // Castling availability (simplified - would need to track this properly)
        fen.append(" KQkq");
        
        // En passant target
        Position epTarget = board.getEnPassantTarget();
        if (epTarget != null) {
            fen.append(" ").append(epTarget.toAlgebraic());
        } else {
            fen.append(" -");
        }
        
        // Halfmove clock (simplified)
        fen.append(" 0");
        
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
        
        return new GameBuilder()
            .withBoard(board)
            .withStartingPlayer(currentPlayer)
            .build();
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
