package chess.controller;

import chess.model.Board;
import chess.model.Color;
import chess.model.GameState;
import chess.model.Move;
import chess.model.Position;

import java.util.List;

/**
 * Abstraction over chess rules so different rule sets can be injected.
 */
public interface RuleSet {

    /**
     * Returns all legal moves for a piece at the given position.
     */
    List<Move> getLegalMoves(Board board, Position from, Color currentPlayer);

    /**
     * Validates a specific move, returning the concrete Move if legal or null if illegal.
     */
    Move validateMove(Board board, Position from, Position to, Color currentPlayer);

    /**
     * Determines the current game state (check, checkmate, etc.) for the active player.
     */
    GameState getGameState(Board board, Color currentPlayer);
}
