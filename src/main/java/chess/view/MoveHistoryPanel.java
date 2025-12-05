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
 * Panel displaying the move history.
 * Implements GameObserver to update when moves are made.
 */
public class MoveHistoryPanel extends JPanel implements GameObserver {

    private final JTextArea moveList;
    private final JScrollPane scrollPane;
    private int moveNumber;
    private boolean isWhiteMove;

    public MoveHistoryPanel(Game game) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Move History"));
        setPreferredSize(new Dimension(200, 400));

        moveList = new JTextArea();
        moveList.setEditable(false);
        moveList.setFont(new Font("Monospaced", Font.PLAIN, 12));

        scrollPane = new JScrollPane(moveList);
        add(scrollPane, BorderLayout.CENTER);

        game.addObserver(this);

        moveNumber = 1;
        isWhiteMove = true;
    }

    public void clear() {
        moveList.setText("");
        moveNumber = 1;
        isWhiteMove = true;
    }

    @Override
    public void onMoveMade(Move move) {
        StringBuilder sb = new StringBuilder();

        if (isWhiteMove) {
            sb.append(String.format("%3d. ", moveNumber));
        }

        sb.append(String.format("%-8s", move.toAlgebraic()));

        if (!isWhiteMove) {
            sb.append("\n");
            moveNumber++;
        }

        moveList.append(sb.toString());
        isWhiteMove = !isWhiteMove;

        moveList.setCaretPosition(moveList.getDocument().getLength());
    }

    @Override
    public void onBoardChanged(Board board) {
    }

    @Override
    public void onTurnChanged(chess.model.Color currentPlayer) {
    }

    @Override
    public void onGameStateChanged(GameState state, chess.model.Color affectedPlayer) {
        if (state == GameState.CHECK) {
        } else if (state == GameState.CHECKMATE) {
            moveList.append("#\n");
            moveList.append(affectedPlayer.opposite() + " wins!");
        } else if (state == GameState.STALEMATE) {
            moveList.append("\nDraw by stalemate");
        }
    }

    @Override
    public void onPieceCaptured(Piece piece) {
    }

    @Override
    public void onMoveUndone(Move move) {
        moveList.append("\n(Move undone)\n");
        isWhiteMove = !isWhiteMove;
        if (isWhiteMove) {
            moveNumber--;
        }
    }
}
