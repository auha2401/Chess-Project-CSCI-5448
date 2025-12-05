package chess.patterns.strategy;

/**
 * Strategy for knight movement (L-shaped jumps).
 */
public class KnightMoveStrategy extends AbstractMoveStrategy {
    
    private static final int[][] DIRECTIONS = {
        {2, 1},
        {2, -1},
        {-2, 1},
        {-2, -1},
        {1, 2},
        {1, -2},
        {-1, 2},
        {-1, -2}
    };
    
    @Override
    public boolean isSliding() {
        return false; // Knights jump, they don't slide
    }
    
    @Override
    public int[][] getDirections() {
        return DIRECTIONS;
    }
}
