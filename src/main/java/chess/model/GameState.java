package chess.model;

/**
 * Represents the current state of the game.
 */
public enum GameState {
    IN_PROGRESS,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_BY_REPETITION,
    DRAW_BY_FIFTY_MOVES,
    DRAW_BY_INSUFFICIENT_MATERIAL,
    DRAW_BY_AGREEMENT,
    RESIGNED
}
