package chess.controller;

import chess.model.Color;
import chess.model.PieceType;
import chess.model.Position;

/**
 * Abstraction for how a player supplies moves (human UI, AI, etc.).
 */
public interface PlayerController {

    /**
     * Attempts to submit a move for this player.
     * @return true if the move was accepted and executed
     */
    boolean submitMove(Game game, Position from, Position to, PieceType promotionType);

    /**
     * Color this controller is responsible for.
     */
    Color getColor();

    /**
     * Display name for the controller (for future UI use).
     */
    default String getName() {
        return getColor().name();
    }
}
