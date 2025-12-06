package chess.controller;

import chess.model.Color;
import chess.model.PieceType;
import chess.model.Position;

/**
 * Simple player controller for human input routed through the UI.
 */
public class HumanPlayerController implements PlayerController {

    private final Color color;

    public HumanPlayerController(Color color) {
        this.color = color;
    }

    @Override
    public boolean submitMove(Game game, Position from, Position to, PieceType promotionType) {
        return game.makeMove(from, to, promotionType, this);
    }

    @Override
    public Color getColor() {
        return color;
    }
}
