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
 * Panel displaying captured pieces.
 * Implements GameObserver to update when pieces are captured.
 */
public class CapturedPiecesPanel extends JPanel implements GameObserver {

    private final JLabel whiteCapturedLabel;
    private final JLabel blackCapturedLabel;
    private final JLabel materialAdvantageLabel;
    private final Game game;

    public CapturedPiecesPanel(Game game) {
        this.game = game;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Captured Pieces"));
        setPreferredSize(new Dimension(200, 150));

        whiteCapturedLabel = new JLabel("White lost: ");
        whiteCapturedLabel.setFont(new Font("Serif", Font.PLAIN, 16));

        blackCapturedLabel = new JLabel("Black lost: ");
        blackCapturedLabel.setFont(new Font("Serif", Font.PLAIN, 16));

        materialAdvantageLabel = new JLabel("Material: Even");
        materialAdvantageLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        add(whiteCapturedLabel);
        add(Box.createVerticalStrut(10));
        add(blackCapturedLabel);
        add(Box.createVerticalStrut(10));
        add(materialAdvantageLabel);

        game.addObserver(this);

        // Initialize display
        updateDisplay();
    }

    private void updateDisplay() {
        List<Piece> capturedFromWhite = game.getCapturedPieces(chess.model.Color.WHITE);
        List<Piece> capturedFromBlack = game.getCapturedPieces(chess.model.Color.BLACK);

        whiteCapturedLabel.setText("White lost: " + piecesToSymbols(capturedFromWhite));
        blackCapturedLabel.setText("Black lost: " + piecesToSymbols(capturedFromBlack));

        // Material captured BY white (from black) vs BY black (from white)
        int whiteCapturedMaterial = calculateMaterial(capturedFromBlack);
        int blackCapturedMaterial = calculateMaterial(capturedFromWhite);
        int advantage = whiteCapturedMaterial - blackCapturedMaterial;

        if (advantage > 0) {
            materialAdvantageLabel.setText("Material: White +" + advantage);
        } else if (advantage < 0) {
            materialAdvantageLabel.setText("Material: Black +" + (-advantage));
        } else {
            materialAdvantageLabel.setText("Material: Even");
        }
    }

    private String piecesToSymbols(List<Piece> pieces) {
        if (pieces.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        pieces.stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(p -> sb.append(p.getSymbol()));
        return sb.toString();
    }

    private int calculateMaterial(List<Piece> capturedPieces) {
        return capturedPieces.stream()
                .mapToInt(Piece::getValue)
                .sum();
    }

    @Override
    public void onMoveMade(Move move) {
    }

    @Override
    public void onBoardChanged(Board board) {
    }

    @Override
    public void onTurnChanged(chess.model.Color currentPlayer) {
    }

    @Override
    public void onGameStateChanged(GameState state, chess.model.Color affectedPlayer) {
    }

    @Override
    public void onPieceCaptured(Piece piece) {
        updateDisplay();
    }

    @Override
    public void onMoveUndone(Move move) {
        updateDisplay();
    }

    /**
     * Resets the panel for a new game.
     */
    public void reset() {
        whiteCapturedLabel.setText("White lost: ");
        blackCapturedLabel.setText("Black lost: ");
        materialAdvantageLabel.setText("Material: Even");
    }
}
