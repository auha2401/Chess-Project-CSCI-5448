package chess.view;

import chess.controller.Game;
import chess.model.Board;
import chess.model.GameState;
import chess.model.Move;
import chess.model.Piece;
import chess.patterns.observer.GameObserver;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel displaying current game status.
 * Implements GameObserver to update status in real-time.
 */
public class StatusPanel extends JPanel implements GameObserver {

    private final JLabel turnLabel;
    private final JLabel statusLabel;

    public StatusPanel(Game game) {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setBorder(BorderFactory.createEtchedBorder());

        turnLabel = new JLabel();
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        statusLabel.setForeground(java.awt.Color.RED);

        add(turnLabel);
        add(Box.createHorizontalStrut(20));
        add(statusLabel);

        // Set initial state
        updateTurnLabel(game.getCurrentPlayer());

        game.addObserver(this);
    }

    private void updateTurnLabel(chess.model.Color player) {
        turnLabel.setText(player + " to move");
    }

    @Override
    public void onMoveMade(Move move) {
    }

    @Override
    public void onBoardChanged(Board board) {
    }

    @Override
    public void onTurnChanged(chess.model.Color currentPlayer) {
        updateTurnLabel(currentPlayer);
    }

    @Override
    public void onGameStateChanged(GameState state, chess.model.Color affectedPlayer) {
        switch (state) {
            case CHECK -> statusLabel.setText("Check!");
            case CHECKMATE -> {
                statusLabel.setText("Checkmate!");
                turnLabel.setText(affectedPlayer.opposite() + " wins!");
            }
            case STALEMATE -> {
                statusLabel.setText("Stalemate!");
                turnLabel.setText("Game drawn");
            }
            case DRAW_BY_REPETITION -> {
                statusLabel.setText("Draw by repetition");
                turnLabel.setText("Game drawn");
            }
            case DRAW_BY_FIFTY_MOVES -> {
                statusLabel.setText("Draw by 50-move rule");
                turnLabel.setText("Game drawn");
            }
            case DRAW_BY_INSUFFICIENT_MATERIAL -> {
                statusLabel.setText("Draw by insufficient material");
                turnLabel.setText("Game drawn");
            }
            default -> statusLabel.setText("");
        }
    }

    @Override
    public void onPieceCaptured(Piece piece) {
    }

    @Override
    public void onMoveUndone(Move move) {
        statusLabel.setText("");
    }

    /**
     * Resets the panel for a new game.
     */
    public void reset(chess.model.Color startingPlayer) {
        updateTurnLabel(startingPlayer);
        statusLabel.setText("");
    }
}
