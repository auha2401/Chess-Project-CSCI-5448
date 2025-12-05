package chess.patterns.strategy;

/**
 * Strategy for diagonal movement (used by Bishops).
 */
public class DiagonalMoveStrategy extends AbstractMoveStrategy {
    
    private static final int[][] DIRECTIONS = {
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
