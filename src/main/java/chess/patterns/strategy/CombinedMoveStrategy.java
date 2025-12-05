package chess.patterns.strategy;

/**
 * Strategy combining diagonal and straight movement (used by Queens).
 */
public class CombinedMoveStrategy extends AbstractMoveStrategy {
    
    private static final int[][] DIRECTIONS = {
        // Straight directions
        {0, 1},   // North
        {0, -1},  // South
        {1, 0},   // East
        {-1, 0},  // West
        // Diagonal directions
        {1, 1},   // Northeast
        {1, -1},  // Southeast
        {-1, 1},  // Northwest
        {-1, -1}  // Southwest
    };
    
    @Override
    public boolean isSliding() {
        return true;
    }
    
    @Override
    public int[][] getDirections() {
        return DIRECTIONS;
    }
}
