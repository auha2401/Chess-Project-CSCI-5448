package chess.patterns.strategy;

/**
 * Strategy for straight (horizontal/vertical) movement (used by Rooks).
 */
public class StraightMoveStrategy extends AbstractMoveStrategy {
    
    private static final int[][] DIRECTIONS = {
        {0, 1},   // North
        {0, -1},  // South
        {1, 0},   // East
        {-1, 0}   // West
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
