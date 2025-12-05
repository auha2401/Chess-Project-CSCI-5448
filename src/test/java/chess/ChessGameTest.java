package chess;

import chess.controller.Game;
import chess.controller.MoveValidator;
import chess.model.*;
import chess.patterns.builder.BoardBuilder;
import chess.patterns.builder.GameBuilder;
import chess.patterns.command.CommandHistory;
import chess.patterns.command.MoveCommand;
import chess.patterns.observer.GameObserver;
import chess.patterns.strategy.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the chess game implementation.
 */
class ChessGameTest {

    private Game game;
    private Board board;
    private MoveValidator validator;

    @BeforeEach
    void setUp() {
        game = new GameBuilder()
                .withStandardSetup()
                .withUndoEnabled(true)
                .build();
        board = game.getBoard();
        validator = new MoveValidator();
    }

    // ========== Strategy Pattern Tests ==========

    @Test
    @DisplayName("Strategy Pattern: Knight moves in L-shape")
    void testKnightMoveStrategy() {
        KnightMoveStrategy strategy = new KnightMoveStrategy();
        Position from = new Position(4, 4); // e5
        List<Position> moves = strategy.getPotentialMoves(from);

        assertEquals(8, moves.size());
        assertTrue(moves.contains(new Position(6, 5))); // g6
        assertTrue(moves.contains(new Position(6, 3))); // g4
        assertTrue(moves.contains(new Position(2, 5))); // c6
        assertTrue(moves.contains(new Position(2, 3))); // c4
    }

    @Test
    @DisplayName("Strategy Pattern: Bishop moves diagonally")
    void testDiagonalMoveStrategy() {
        DiagonalMoveStrategy strategy = new DiagonalMoveStrategy();
        assertTrue(strategy.isSliding());

        Position from = new Position(4, 4); // e5
        List<Position> moves = strategy.getPotentialMoves(from);

        assertTrue(moves.contains(new Position(5, 5))); // f6
        assertTrue(moves.contains(new Position(3, 3))); // d4
        assertTrue(moves.contains(new Position(5, 3))); // f4
        assertTrue(moves.contains(new Position(3, 5))); // d6
    }

    @Test
    @DisplayName("Strategy Pattern: Rook moves straight")
    void testStraightMoveStrategy() {
        StraightMoveStrategy strategy = new StraightMoveStrategy();
        assertTrue(strategy.isSliding());

        Position from = new Position(4, 4); // e5
        List<Position> moves = strategy.getPotentialMoves(from);

        assertTrue(moves.contains(new Position(4, 5))); // e6
        assertTrue(moves.contains(new Position(4, 3))); // e4
        assertTrue(moves.contains(new Position(5, 4))); // f5
        assertTrue(moves.contains(new Position(3, 4))); // d5
    }

    @Test
    @DisplayName("Strategy Pattern: Queen combines diagonal and straight")
    void testCombinedMoveStrategy() {
        CombinedMoveStrategy strategy = new CombinedMoveStrategy();
        assertTrue(strategy.isSliding());
        assertEquals(8, strategy.getDirections().length);
    }

    // ========== Builder Pattern Tests ==========

    @Test
    @DisplayName("Builder Pattern: BoardBuilder creates standard setup")
    void testBoardBuilderStandardSetup() {
        Board board = new BoardBuilder().withStandardSetup().build();

        assertEquals(PieceType.ROOK, board.getPieceAt(Position.fromAlgebraic("a1")).getType());
        assertEquals(PieceType.KNIGHT, board.getPieceAt(Position.fromAlgebraic("b1")).getType());
        assertEquals(PieceType.BISHOP, board.getPieceAt(Position.fromAlgebraic("c1")).getType());
        assertEquals(PieceType.QUEEN, board.getPieceAt(Position.fromAlgebraic("d1")).getType());
        assertEquals(PieceType.KING, board.getPieceAt(Position.fromAlgebraic("e1")).getType());

        for (int file = 0; file < 8; file++) {
            assertEquals(PieceType.PAWN, board.getPieceAt(new Position(file, 1)).getType());
            assertEquals(PieceType.PAWN, board.getPieceAt(new Position(file, 6)).getType());
        }
    }

    @Test
    @DisplayName("Builder Pattern: BoardBuilder from FEN")
    void testBoardBuilderFromFEN() {
        String fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1";
        Board board = new BoardBuilder().fromFEN(fen).build();

        Piece e4Pawn = board.getPieceAt(Position.fromAlgebraic("e4"));
        assertNotNull(e4Pawn);
        assertEquals(PieceType.PAWN, e4Pawn.getType());
        assertEquals(Color.WHITE, e4Pawn.getColor());

        assertNull(board.getPieceAt(Position.fromAlgebraic("e2")));
    }

    @Test
    @DisplayName("Builder Pattern: GameBuilder configures game")
    void testGameBuilder() {
        Game game = new GameBuilder()
                .withStandardSetup()
                .withStartingPlayer(Color.BLACK)
                .withUndoEnabled(false)
                .build();

        assertEquals(Color.BLACK, game.getCurrentPlayer());
        assertFalse(game.canUndo());
    }

    // ========== Command Pattern Tests ==========

    @Test
    @DisplayName("Command Pattern: MoveCommand executes and undoes moves")
    void testMoveCommand() {
        Position from = Position.fromAlgebraic("e2");
        Position to = Position.fromAlgebraic("e4");
        Piece pawn = board.getPieceAt(from);

        Move move = new Move(from, to, pawn, null, Move.MoveType.DOUBLE_PAWN_PUSH, null);
        MoveCommand command = new MoveCommand(board, move);

        command.execute();
        assertNull(board.getPieceAt(from));
        assertNotNull(board.getPieceAt(to));

        command.undo();
        assertNotNull(board.getPieceAt(from));
        assertNull(board.getPieceAt(to));
    }

    @Test
    @DisplayName("Command Pattern: CommandHistory supports undo/redo")
    void testCommandHistory() {
        CommandHistory history = new CommandHistory();

        assertFalse(history.canUndo());
        assertFalse(history.canRedo());

        Position from = Position.fromAlgebraic("e2");
        Position to = Position.fromAlgebraic("e4");
        Piece pawn = board.getPieceAt(from);
        Move move = new Move(from, to, pawn, null, Move.MoveType.DOUBLE_PAWN_PUSH, null);
        MoveCommand command = new MoveCommand(board, move);

        history.executeCommand(command);
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());

        history.undo();
        assertFalse(history.canUndo());
        assertTrue(history.canRedo());

        history.redo();
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    // ========== Observer Pattern Tests ==========

    @Test
    @DisplayName("Observer Pattern: Game notifies observers on move")
    void testObserverNotification() {
        final boolean[] notified = {false};

        game.addObserver(new GameObserver() {
            @Override
            public void onMoveMade(Move move) {
                notified[0] = true;
            }

            @Override
            public void onBoardChanged(Board board) {}

            @Override
            public void onTurnChanged(Color currentPlayer) {}

            @Override
            public void onGameStateChanged(GameState state, Color affectedPlayer) {}

            @Override
            public void onPieceCaptured(Piece piece) {}

            @Override
            public void onMoveUndone(Move move) {}
        });

        game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));
        assertTrue(notified[0]);
    }

    // ========== Turn Change Tests ==========

    @Test
    @DisplayName("Turn changes from WHITE to BLACK after white moves")
    void testTurnChangesAfterWhiteMove() {
        assertEquals(Color.WHITE, game.getCurrentPlayer());

        game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));

        assertEquals(Color.BLACK, game.getCurrentPlayer());
    }

    @Test
    @DisplayName("Turn changes from BLACK to WHITE after black moves")
    void testTurnChangesAfterBlackMove() {
        game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));
        assertEquals(Color.BLACK, game.getCurrentPlayer());

        game.makeMove(Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5"));

        assertEquals(Color.WHITE, game.getCurrentPlayer());
    }

    @Test
    @DisplayName("Observer receives correct player on turn change")
    void testObserverReceivesCorrectTurn() {
        List<Color> turnChanges = new ArrayList<>();

        game.addObserver(new GameObserver() {
            @Override
            public void onMoveMade(Move move) {}

            @Override
            public void onBoardChanged(Board board) {}

            @Override
            public void onTurnChanged(Color currentPlayer) {
                turnChanges.add(currentPlayer);
            }

            @Override
            public void onGameStateChanged(GameState state, Color affectedPlayer) {}

            @Override
            public void onPieceCaptured(Piece piece) {}

            @Override
            public void onMoveUndone(Move move) {}
        });

        game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));
        game.makeMove(Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5"));

        assertEquals(2, turnChanges.size());
        assertEquals(Color.BLACK, turnChanges.get(0));
        assertEquals(Color.WHITE, turnChanges.get(1));
    }

    // ========== Captured Pieces Tests ==========

    @Test
    @DisplayName("Captured piece is tracked correctly")
    void testCapturedPieceTracking() {
        // 1. e4 d5 2. exd5
        game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));
        game.makeMove(Position.fromAlgebraic("d7"), Position.fromAlgebraic("d5"));
        game.makeMove(Position.fromAlgebraic("e4"), Position.fromAlgebraic("d5"));

        List<Piece> capturedBlackPieces = game.getCapturedPieces(Color.BLACK);
        assertEquals(1, capturedBlackPieces.size());
        assertEquals(PieceType.PAWN, capturedBlackPieces.get(0).getType());
        assertEquals(Color.BLACK, capturedBlackPieces.get(0).getColor());

        List<Piece> capturedWhitePieces = game.getCapturedPieces(Color.WHITE);
        assertTrue(capturedWhitePieces.isEmpty());
    }

    @Test
    @DisplayName("Observer notified when piece is captured")
    void testObserverNotifiedOnCapture() {
        List<Piece> capturedPieces = new ArrayList<>();

        game.addObserver(new GameObserver() {
            @Override
            public void onMoveMade(Move move) {}

            @Override
            public void onBoardChanged(Board board) {}

            @Override
            public void onTurnChanged(Color currentPlayer) {}

            @Override
            public void onGameStateChanged(GameState state, Color affectedPlayer) {}

            @Override
            public void onPieceCaptured(Piece piece) {
                capturedPieces.add(piece);
            }

            @Override
            public void onMoveUndone(Move move) {}
        });

        // 1. e4 d5 2. exd5
        game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));
        game.makeMove(Position.fromAlgebraic("d7"), Position.fromAlgebraic("d5"));
        game.makeMove(Position.fromAlgebraic("e4"), Position.fromAlgebraic("d5"));

        assertEquals(1, capturedPieces.size());
        assertEquals(PieceType.PAWN, capturedPieces.get(0).getType());
    }

    @Test
    @DisplayName("Multiple captures tracked correctly")
    void testMultipleCapturesTracked() {
        // Set up a position where captures can happen quickly
        Board customBoard = new BoardBuilder()
                .empty()
                .withPiece("e1", new King(Color.WHITE))
                .withPiece("e8", new King(Color.BLACK))
                .withPiece("d4", new Pawn(Color.WHITE))
                .withPiece("e5", new Pawn(Color.BLACK))
                .withPiece("c5", new Pawn(Color.BLACK))
                .build();

        Game customGame = new GameBuilder()
                .withBoard(customBoard)
                .withStartingPlayer(Color.WHITE)
                .build();

        // d4xc5
        customGame.makeMove(Position.fromAlgebraic("d4"), Position.fromAlgebraic("c5"));

        List<Piece> capturedBlack = customGame.getCapturedPieces(Color.BLACK);
        assertEquals(1, capturedBlack.size());

        // e5-e4 (no capture)
        customGame.makeMove(Position.fromAlgebraic("e5"), Position.fromAlgebraic("e4"));

        // Still only 1 captured
        assertEquals(1, customGame.getCapturedPieces(Color.BLACK).size());
    }

    @Test
    @DisplayName("Captured pieces restored on undo")
    void testCapturedPiecesRestoredOnUndo() {
        // 1. e4 d5 2. exd5
        game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));
        game.makeMove(Position.fromAlgebraic("d7"), Position.fromAlgebraic("d5"));
        game.makeMove(Position.fromAlgebraic("e4"), Position.fromAlgebraic("d5"));

        assertEquals(1, game.getCapturedPieces(Color.BLACK).size());

        // Undo the capture
        game.undoMove();

        assertEquals(0, game.getCapturedPieces(Color.BLACK).size());
    }

    @Test
    @DisplayName("Material calculation after captures")
    void testMaterialCalculation() {
        // Set up position for quick captures of different values
        Board customBoard = new BoardBuilder()
                .empty()
                .withPiece("e1", new King(Color.WHITE))
                .withPiece("e8", new King(Color.BLACK))
                .withPiece("d4", new Queen(Color.WHITE))
                .withPiece("d5", new Pawn(Color.BLACK))
                .build();

        Game customGame = new GameBuilder()
                .withBoard(customBoard)
                .withStartingPlayer(Color.WHITE)
                .build();

        // Qxd5 - capture pawn worth 1
        customGame.makeMove(Position.fromAlgebraic("d4"), Position.fromAlgebraic("d5"));

        List<Piece> captured = customGame.getCapturedPieces(Color.BLACK);
        int material = captured.stream().mapToInt(Piece::getValue).sum();
        assertEquals(1, material);
    }

    // ========== Move Validation Tests ==========

    @Test
    @DisplayName("Valid pawn opening moves")
    void testPawnOpeningMoves() {
        List<Move> moves = validator.getLegalMoves(board, Position.fromAlgebraic("e2"), Color.WHITE);

        assertEquals(2, moves.size());
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(Position.fromAlgebraic("e3"))));
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(Position.fromAlgebraic("e4"))));
    }

    @Test
    @DisplayName("Knight can jump over pieces")
    void testKnightJumps() {
        List<Move> moves = validator.getLegalMoves(board, Position.fromAlgebraic("b1"), Color.WHITE);

        assertEquals(2, moves.size());
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(Position.fromAlgebraic("a3"))));
        assertTrue(moves.stream().anyMatch(m -> m.getTo().equals(Position.fromAlgebraic("c3"))));
    }

    @Test
    @DisplayName("Cannot move opponent's piece")
    void testCannotMoveOpponentPiece() {
        List<Move> moves = validator.getLegalMoves(board, Position.fromAlgebraic("e7"), Color.WHITE);
        assertTrue(moves.isEmpty());
    }

    @Test
    @DisplayName("Game detects check")
    void testCheckDetection() {
        Board testBoard = new BoardBuilder()
                .empty()
                .withPiece("e1", new King(Color.WHITE))
                .withPiece("e8", new King(Color.BLACK))
                .withPiece("e7", new Rook(Color.WHITE))
                .build();

        assertTrue(validator.isInCheck(testBoard, Color.BLACK));
        assertFalse(validator.isInCheck(testBoard, Color.WHITE));
    }

    @Test
    @DisplayName("Game detects checkmate - Fool's Mate")
    void testCheckmateDetection() {
        // Fool's mate final position:
        // 1. f3 e5 2. g4 Qh4#
        Board mateBoard = new BoardBuilder()
                .withStandardSetup()
                .build();

        // Move f2 pawn to f3
        Piece fPawn = mateBoard.removePiece(Position.fromAlgebraic("f2"));
        mateBoard.setPiece(Position.fromAlgebraic("f3"), fPawn);

        // Move e7 pawn to e5
        Piece ePawn = mateBoard.removePiece(Position.fromAlgebraic("e7"));
        mateBoard.setPiece(Position.fromAlgebraic("e5"), ePawn);

        // Move g2 pawn to g4
        Piece gPawn = mateBoard.removePiece(Position.fromAlgebraic("g2"));
        mateBoard.setPiece(Position.fromAlgebraic("g4"), gPawn);

        // Move queen from d8 to h4 (checkmate)
        Piece queen = mateBoard.removePiece(Position.fromAlgebraic("d8"));
        mateBoard.setPiece(Position.fromAlgebraic("h4"), queen);

        assertEquals(GameState.CHECKMATE, validator.getGameState(mateBoard, Color.WHITE));
    }

    @Test
    @DisplayName("Game integration: e4 e5 opening")
    void testOpeningMoves() {
        assertTrue(game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4")));
        assertEquals(Color.BLACK, game.getCurrentPlayer());

        assertTrue(game.makeMove(Position.fromAlgebraic("e7"), Position.fromAlgebraic("e5")));
        assertEquals(Color.WHITE, game.getCurrentPlayer());

        assertNull(board.getPieceAt(Position.fromAlgebraic("e2")));
        assertNotNull(board.getPieceAt(Position.fromAlgebraic("e4")));
        assertNull(board.getPieceAt(Position.fromAlgebraic("e7")));
        assertNotNull(board.getPieceAt(Position.fromAlgebraic("e5")));
    }

    @Test
    @DisplayName("Undo restores previous state")
    void testUndo() {
        game.makeMove(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"));

        assertTrue(game.canUndo());
        game.undoMove();

        assertNotNull(board.getPieceAt(Position.fromAlgebraic("e2")));
        assertNull(board.getPieceAt(Position.fromAlgebraic("e4")));
        assertEquals(Color.WHITE, game.getCurrentPlayer());
    }
}
