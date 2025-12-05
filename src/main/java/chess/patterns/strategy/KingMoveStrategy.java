package chess.patterns.strategy;

/**
 * Strategy for king movement (one square in any direction).
 */
public class KingMoveStrategy extends AbstractMoveStrategy {
    
    private static final int[][] DIRECTIONS = {
        {0, 1},   // North
        {0, -1},  // South
        {1, 0},   // East
        {-1, 0},  // West
        {1, 1},   // Northeast
        {1, -1},  // Southeast
        {-1, 1},  // Northwest
        {-1, -1}  // Southwest
    };
    
    @Override
    public boolean isSliding() {
        return false; // King only moves one square
    }
    
    @Override
    public int[][] getDirections() {
        return DIRECTIONS;
    }
}
