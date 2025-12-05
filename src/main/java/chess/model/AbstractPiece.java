package chess.model;

import chess.patterns.strategy.MoveStrategy;
import java.util.List;

/**
 * Abstract base class for chess pieces.
 */
public abstract class AbstractPiece implements Piece {
    
    protected final PieceType type;
    protected final Color color;
    protected final MoveStrategy moveStrategy;
    protected boolean moved;
    
    protected AbstractPiece(PieceType type, Color color, MoveStrategy moveStrategy) {
        this.type = type;
        this.color = color;
        this.moveStrategy = moveStrategy;
        this.moved = false;
    }
    
    @Override
    public PieceType getType() {
        return type;
    }
    
    @Override
    public Color getColor() {
        return color;
    }
    
    @Override
    public MoveStrategy getMoveStrategy() {
        return moveStrategy;
    }
    
    @Override
    public List<Position> getPotentialMoves(Position from) {
        return moveStrategy.getPotentialMoves(from);
    }
    
    @Override
    public boolean hasMoved() {
        return moved;
    }
    
    @Override
    public void setMoved(boolean moved) {
        this.moved = moved;
    }
    
    @Override
    public String toString() {
        return color.name().charAt(0) + type.getNotation();
    }
}
