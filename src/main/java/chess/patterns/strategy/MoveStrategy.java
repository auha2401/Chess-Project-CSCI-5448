package chess.patterns.strategy;

import chess.model.Position;
import java.util.List;

/**
 * Strategy Pattern: Interface for different piece movement strategies.
 * 
 * This eliminates the need for large switch statements when determining
 * how pieces can move. Each piece type has its own strategy implementation.
 */
public interface MoveStrategy {
    
    /**
     * Returns all potential target positions from the given position,
     * without considering other pieces on the board.
     * 
     * @param from The starting position
     * @return List of all positions this strategy allows moving to
     */
    List<Position> getPotentialMoves(Position from);
    
    /**
     * Returns true if this strategy allows sliding moves (can be blocked).
     * Rooks, bishops, and queens slide; knights and kings do not.
     */
    boolean isSliding();
    
    /**
     * Returns the movement directions for this strategy.
     * Each direction is represented as [deltaFile, deltaRank].
     */
    int[][] getDirections();
}
