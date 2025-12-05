package chess.patterns.strategy;

import chess.model.Color;
import chess.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy for pawn movement.
 * Pawns are special: they move forward, capture diagonally, and can move two squares from start.
 */
public class PawnMoveStrategy implements MoveStrategy {
    
    private final Color color;
    
    public PawnMoveStrategy(Color color) {
        this.color = color;
    }
    
    @Override
    public List<Position> getPotentialMoves(Position from) {
        List<Position> moves = new ArrayList<>();
        int direction = color.getPawnDirection();
        
        // Forward one square
        Position oneForward = from.offset(0, direction);
        if (oneForward.isValid()) {
            moves.add(oneForward);
        }
        
        // Forward two squares from starting position
        if (from.getRank() == color.getPawnStartRank()) {
            Position twoForward = from.offset(0, 2 * direction);
            if (twoForward.isValid()) {
                moves.add(twoForward);
            }
        }
        
        // Diagonal captures (left and right)
        Position captureLeft = from.offset(-1, direction);
        if (captureLeft.isValid()) {
            moves.add(captureLeft);
        }
        
        Position captureRight = from.offset(1, direction);
        if (captureRight.isValid()) {
            moves.add(captureRight);
        }
        
        return moves;
    }
    
    @Override
    public boolean isSliding() {
        return false;
    }
    
    @Override
    public int[][] getDirections() {
        // Pawns don't use standard directions
        return new int[0][];
    }
    
    public Color getColor() {
        return color;
    }
}
