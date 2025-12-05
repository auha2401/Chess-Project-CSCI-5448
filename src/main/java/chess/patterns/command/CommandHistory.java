package chess.patterns.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Command Pattern: Maintains history of executed commands for undo/redo.
 */
public class CommandHistory {
    
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;
    private final List<Command> allCommands;
    
    public CommandHistory() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.allCommands = new ArrayList<>();
    }
    
    /**
     * Executes a command and adds it to history.
     */
    public void executeCommand(Command command) {
        command.execute();
        undoStack.push(command);
        allCommands.add(command);
        redoStack.clear(); // Clear redo stack after new move
    }
    
    /**
     * Undoes the last command.
     * @return true if a command was undone
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return true;
    }
    
    /**
     * Redoes the last undone command.
     * @return true if a command was redone
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return true;
    }
    
    /**
     * Returns true if there are commands that can be undone.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Returns true if there are commands that can be redone.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Returns the number of moves made.
     */
    public int getMoveCount() {
        return undoStack.size();
    }
    
    /**
     * Returns all executed commands in order.
     */
    public List<Command> getAllCommands() {
        return new ArrayList<>(allCommands);
    }
    
    /**
     * Returns the last executed command, or null if none.
     */
    public Command getLastCommand() {
        return undoStack.isEmpty() ? null : undoStack.peek();
    }
    
    /**
     * Clears all history.
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        allCommands.clear();
    }
}
