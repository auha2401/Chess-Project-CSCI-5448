package chess.controller;

import chess.model.*;
import chess.patterns.command.CommandHistory;
import chess.patterns.command.MoveCommand;
import chess.patterns.observer.GameObserver;
import chess.patterns.observer.GameSubject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Main game controller that orchestrates the chess game.
 * 
 * Implements the Observer pattern (GameSubject) for notifying UI components.
 * Uses the Command pattern (CommandHistory) for undo/redo functionality.
 * Uses dependency injection for the board, rule set, and player controllers.
 */
public class Game implements GameSubject {
    
    private final Board board;
    private final RuleSet ruleSet;
    private final CommandHistory commandHistory;
    private final List<GameObserver> observers;
    private final List<Piece> capturedWhitePieces;
    private final List<Piece> capturedBlackPieces;
    private final boolean undoEnabled;
    private final Deque<Integer> halfMoveHistory;
    private final List<String> repetitionHistory;
    private PlayerController whiteController;
    private PlayerController blackController;
    
    private Color currentPlayer;
    private GameState gameState;
    private int moveNumber;
    private int halfMoveClock;
    
    /**
     * Creates a new game with dependency injection.
     */
    public Game(Board board, RuleSet ruleSet, Color startingPlayer, boolean undoEnabled) {
        this.board = board;
        this.ruleSet = ruleSet;
        this.currentPlayer = startingPlayer;
        this.undoEnabled = undoEnabled;
        this.commandHistory = new CommandHistory();
        this.observers = new ArrayList<>();
        this.capturedWhitePieces = new ArrayList<>();
        this.capturedBlackPieces = new ArrayList<>();
        this.gameState = GameState.IN_PROGRESS;
        this.moveNumber = 1;
        this.halfMoveClock = 0;
        this.halfMoveHistory = new ArrayDeque<>();
        this.repetitionHistory = new ArrayList<>();
        addCurrentStateToRepetition();
    }

    /**
     * Injects player controllers for each side.
     */
    public void setPlayerControllers(PlayerController whiteController, PlayerController blackController) {
        this.whiteController = whiteController != null ? whiteController : new HumanPlayerController(Color.WHITE);
        this.blackController = blackController != null ? blackController : new HumanPlayerController(Color.BLACK);
    }

    public PlayerController getPlayerController(Color color) {
        ensureControllers();
        return color == Color.WHITE ? whiteController : blackController;
    }

    private PlayerController getCurrentPlayerController() {
        return getPlayerController(currentPlayer);
    }

    private void ensureControllers() {
        if (whiteController == null) {
            whiteController = new HumanPlayerController(Color.WHITE);
        }
        if (blackController == null) {
            blackController = new HumanPlayerController(Color.BLACK);
        }
    }
    
    /**
     * Attempts to make a move from one position to another.
     * @return true if the move was made successfully
     */
    public boolean makeMove(Position from, Position to) {
        return makeMove(from, to, null, getCurrentPlayerController());
    }
    
    /**
     * Attempts to make a move with optional promotion type.
     * @return true if the move was made successfully
     */
    public boolean makeMove(Position from, Position to, PieceType promotionType) {
        return makeMove(from, to, promotionType, getCurrentPlayerController());
    }
    
    /**
     * Attempts to make a move on behalf of the provided controller.
     */
    public boolean makeMove(Position from, Position to, PieceType promotionType, PlayerController actor) {
        if (gameState == GameState.CHECKMATE || gameState == GameState.STALEMATE) {
            return false;
        }
        
        if (actor != null && actor.getColor() != currentPlayer) {
            return false;
        }
        
        Move move = ruleSet.validateMove(board, from, to, currentPlayer);
        if (move == null) {
            return false;
        }
        
        // Handle promotion type
        if (move.isPromotion() && promotionType != null) {
            move = new Move(from, to, move.getPiece(), move.getCapturedPiece(),
                           move.getMoveType(), promotionType);
        }
        
        // Track half-move clock for undo
        halfMoveHistory.push(halfMoveClock);

        // Execute the move using Command pattern
        MoveCommand command = new MoveCommand(board, move);
        commandHistory.executeCommand(command);
        
        // Track captured pieces
        if (command.getCapturedPiece() != null) {
            Piece captured = command.getCapturedPiece();
            if (captured.getColor() == Color.WHITE) {
                capturedWhitePieces.add(captured);
            } else {
                capturedBlackPieces.add(captured);
            }
            notifyPieceCaptured(captured);
        }

        // Update half-move clock (reset on pawn move or capture)
        if (move.getPiece().getType() == PieceType.PAWN || command.getCapturedPiece() != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        
        // Notify observers of the move
        notifyMoveMade(move);
        notifyBoardChanged();
        
        // Switch players
        currentPlayer = currentPlayer.opposite();
        if (currentPlayer == Color.WHITE) {
            moveNumber++;
        }
        notifyTurnChanged();

        addCurrentStateToRepetition();
        
        // Update game state
        updateGameState();
        
        return true;
    }
    
    /**
     * Undoes the last move.
     * @return true if a move was undone
     */
    public boolean undoMove() {
        if (!undoEnabled || !commandHistory.canUndo()) {
            return false;
        }

        removeCurrentStateFromRepetition();
        
        MoveCommand lastCommand = (MoveCommand) commandHistory.getLastCommand();
        Move lastMove = lastCommand.getMove();
        
        // Restore captured piece tracking
        if (lastCommand.getCapturedPiece() != null) {
            Piece captured = lastCommand.getCapturedPiece();
            if (captured.getColor() == Color.WHITE) {
                capturedWhitePieces.remove(captured);
            } else {
                capturedBlackPieces.remove(captured);
            }
        }
        
        commandHistory.undo();

        // Restore half-move clock
        if (!halfMoveHistory.isEmpty()) {
            halfMoveClock = halfMoveHistory.pop();
        } else {
            halfMoveClock = 0;
        }
        
        // Switch back to previous player
        currentPlayer = currentPlayer.opposite();
        if (currentPlayer == Color.BLACK) {
            moveNumber--;
        }
        
        // Notify observers
        notifyMoveUndone(lastMove);
        notifyBoardChanged();
        notifyTurnChanged();
        updateGameState();
        
        return true;
    }
    
    /**
     * Redoes the last undone move.
     * @return true if a move was redone
     */
    public boolean redoMove() {
        if (!undoEnabled || !commandHistory.canRedo()) {
            return false;
        }
        
        commandHistory.redo();
        
        MoveCommand command = (MoveCommand) commandHistory.getLastCommand();
        
        // Update captured piece tracking
        if (command.getCapturedPiece() != null) {
            Piece captured = command.getCapturedPiece();
            if (captured.getColor() == Color.WHITE) {
                capturedWhitePieces.add(captured);
            } else {
                capturedBlackPieces.add(captured);
            }
        }

        // Update half-move clock (track previous for undo)
        halfMoveHistory.push(halfMoveClock);
        if (command.getMove().getPiece().getType() == PieceType.PAWN || command.getCapturedPiece() != null) {
            halfMoveClock = 0;
        } else {
            halfMoveClock++;
        }
        
        // Switch players
        currentPlayer = currentPlayer.opposite();
        if (currentPlayer == Color.WHITE) {
            moveNumber++;
        }

        addCurrentStateToRepetition();
        
        // Notify observers
        notifyMoveMade(command.getMove());
        notifyBoardChanged();
        notifyTurnChanged();
        updateGameState();
        
        return true;
    }
    
    /**
     * Gets all legal moves for the piece at the given position.
     */
    public List<Move> getLegalMoves(Position position) {
        return ruleSet.getLegalMoves(board, position, currentPlayer);
    }
    
    /**
     * Gets legal target positions for a piece (for UI highlighting).
     */
    public List<Position> getLegalTargets(Position from) {
        List<Move> moves = getLegalMoves(from);
        List<Position> targets = new ArrayList<>();
        for (Move move : moves) {
            targets.add(move.getTo());
        }
        notifyLegalMovesCalculated(from, targets);
        return targets;
    }
    
    private void updateGameState() {
        gameState = ruleSet.getGameState(board, currentPlayer);

        // Apply draw conditions beyond the base rule set
        if (gameState != GameState.CHECKMATE && gameState != GameState.STALEMATE) {
            if (halfMoveClock >= 100) {
                gameState = GameState.DRAW_BY_FIFTY_MOVES;
            } else if (hasThreefoldRepetition()) {
                gameState = GameState.DRAW_BY_REPETITION;
            } else if (hasInsufficientMaterial()) {
                gameState = GameState.DRAW_BY_INSUFFICIENT_MATERIAL;
            }
        }
        notifyGameStateChanged();
    }
    
    // Observer pattern implementation
    
    @Override
    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }
    
    @Override
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        notifyBoardChanged();
        notifyTurnChanged();
        notifyGameStateChanged();
    }
    
    private void notifyMoveMade(Move move) {
        for (GameObserver observer : observers) {
            observer.onMoveMade(move);
        }
    }
    
    private void notifyMoveUndone(Move move) {
        for (GameObserver observer : observers) {
            observer.onMoveUndone(move);
        }
    }
    
    private void notifyBoardChanged() {
        for (GameObserver observer : observers) {
            observer.onBoardChanged(board);
        }
    }
    
    private void notifyTurnChanged() {
        for (GameObserver observer : observers) {
            observer.onTurnChanged(currentPlayer);
        }
    }
    
    private void notifyGameStateChanged() {
        for (GameObserver observer : observers) {
            observer.onGameStateChanged(gameState, currentPlayer);
        }
    }
    
    private void notifyPieceCaptured(Piece piece) {
        for (GameObserver observer : observers) {
            observer.onPieceCaptured(piece);
        }
    }
    
    private void notifyLegalMovesCalculated(Position from, List<Position> moves) {
        for (GameObserver observer : observers) {
            observer.onLegalMovesCalculated(from, moves);
        }
    }
    
    // Getters
    
    public Board getBoard() {
        return board;
    }
    
    public Color getCurrentPlayer() {
        return currentPlayer;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public int getMoveNumber() {
        return moveNumber;
    }
    
    public List<Piece> getCapturedPieces(Color color) {
        return color == Color.WHITE ? 
               new ArrayList<>(capturedWhitePieces) : 
               new ArrayList<>(capturedBlackPieces);
    }

    public int getHalfMoveClock() {
        return halfMoveClock;
    }
    
    public boolean canUndo() {
        return undoEnabled && commandHistory.canUndo();
    }
    
    public boolean canRedo() {
        return undoEnabled && commandHistory.canRedo();
    }
    
    public CommandHistory getCommandHistory() {
        return commandHistory;
    }

    /**
     * Initializes game metadata for a loaded position.
     */
    public void initializeFromPosition(Color currentPlayer, int moveNumber, int halfMoveClock) {
        this.currentPlayer = currentPlayer;
        this.moveNumber = Math.max(moveNumber, 1);
        this.halfMoveClock = Math.max(halfMoveClock, 0);

        repetitionHistory.clear();
        halfMoveHistory.clear();
        addCurrentStateToRepetition();
        updateGameState();
    }

    // === Draw detection helpers ===

    private void addCurrentStateToRepetition() {
        String key = computeStateKey();
        repetitionHistory.add(key);
    }

    private void removeCurrentStateFromRepetition() {
        if (repetitionHistory.isEmpty()) {
            return;
        }
        repetitionHistory.remove(repetitionHistory.size() - 1);
    }

    private boolean hasThreefoldRepetition() {
        if (repetitionHistory.isEmpty()) {
            return false;
        }
        String key = repetitionHistory.get(repetitionHistory.size() - 1);
        int occurrences = 0;
        for (String state : repetitionHistory) {
            if (state.equals(key)) {
                occurrences++;
                if (occurrences >= 3) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasInsufficientMaterial() {
        int bishops = 0;
        int knights = 0;
        boolean hasOtherMaterial = false;
        List<Boolean> bishopSquareColors = new ArrayList<>();

        for (Map.Entry<Position, Piece> entry : board.getAllPieces().entrySet()) {
            Piece piece = entry.getValue();
            switch (piece.getType()) {
                case PAWN, ROOK, QUEEN -> hasOtherMaterial = true;
                case BISHOP -> {
                    bishops++;
                    Position pos = entry.getKey();
                    bishopSquareColors.add((pos.getFile() + pos.getRank()) % 2 == 0);
                }
                case KNIGHT -> knights++;
                default -> { /* kings ignored */ }
            }
            if (hasOtherMaterial) {
                return false;
            }
        }

        int minorCount = bishops + knights;
        if (minorCount == 0) {
            return true; // kings only
        }
        if (minorCount == 1) {
            return true; // single bishop or knight
        }
        if (minorCount == 2 && knights == 2) {
            return true; // two knights vs king
        }
        if (minorCount == 2 && bishops == 2) {
            boolean allSameColor = bishopSquareColors.stream().allMatch(bishopSquareColors.get(0)::equals);
            if (allSameColor) {
                return true;
            }
        }
        return false;
    }

    private String computeStateKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(currentPlayer);

        // Piece placement
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPieceAt(pos);
                if (piece == null) {
                    sb.append('.');
                } else {
                    char c = switch (piece.getType()) {
                        case PAWN -> 'p';
                        case KNIGHT -> 'n';
                        case BISHOP -> 'b';
                        case ROOK -> 'r';
                        case QUEEN -> 'q';
                        case KING -> 'k';
                    };
                    if (piece.getColor() == Color.WHITE) {
                        c = Character.toUpperCase(c);
                    }
                    sb.append(c);
                }
            }
        }

        // Castling rights
        sb.append('|').append(getCastlingRights());

        // En passant target
        sb.append('|');
        Position ep = board.getEnPassantTarget();
        sb.append(ep != null ? ep.toAlgebraic() : "-");

        return sb.toString();
    }

    private String getCastlingRights() {
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
}
