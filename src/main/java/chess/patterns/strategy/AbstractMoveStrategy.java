package chess.patterns.strategy;

import chess.model.Position;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for move strategies providing common functionality.
 */
public abstract class AbstractMoveStrategy implements MoveStrategy {
    
    @Override
    public List<Position> getPotentialMoves(Position from) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = getDirections();
        
        if (isSliding()) {
            // Sliding pieces can move multiple squares in each direction
            for (int[] dir : directions) {
                for (int dist = 1; dist < 8; dist++) {
                    Position target = from.offset(dir[0] * dist, dir[1] * dist);
                    if (target.isValid()) {
                        moves.add(target);
                    } else {
                        break; // Stop when we go off the board
                    }
                }
            }
        } else {
            // Non-sliding pieces move exactly one step in each direction
            for (int[] dir : directions) {
                Position target = from.offset(dir[0], dir[1]);
                if (target.isValid()) {
                    moves.add(target);
                }
            }
        }
        
        return moves;
    }
}
