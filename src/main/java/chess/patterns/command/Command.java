package chess.patterns.command;

/**
 * Command Pattern: Interface for executable commands.
 * 
 * Encapsulates a chess move as an object, enabling:
 * - Undo/redo functionality
 * - Move history tracking
 * - Deferred execution
 */
public interface Command {
    
    /**
     * Executes the command.
     */
    void execute();
    
    /**
     * Undoes the command, restoring the previous state.
     */
    void undo();
    
    /**
     * Returns a description of this command.
     */
    String getDescription();
}
