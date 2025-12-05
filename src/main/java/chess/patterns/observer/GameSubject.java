package chess.patterns.observer;

/**
 * Observer Pattern: Interface for the subject (observable) in the observer pattern.
 * 
 * The Game class implements this to manage its list of observers.
 */
public interface GameSubject {
    
    /**
     * Registers an observer to receive game events.
     */
    void addObserver(GameObserver observer);
    
    /**
     * Removes an observer from receiving game events.
     */
    void removeObserver(GameObserver observer);
    
    /**
     * Notifies all registered observers of an event.
     */
    void notifyObservers();
}
