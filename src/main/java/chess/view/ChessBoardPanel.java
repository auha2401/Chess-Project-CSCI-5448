package chess.view;

import chess.controller.Game;
import chess.model.Board;
import chess.model.GameState;
import chess.model.Move;
import chess.model.Piece;
import chess.model.PieceType;
import chess.model.Position;
import chess.patterns.observer.GameObserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Swing panel that displays the chess board and handles user interaction.
 * Implements GameObserver to react to game state changes.
 */
public class ChessBoardPanel extends JPanel implements GameObserver {

    private static final int SQUARE_SIZE = 70;
    private static final java.awt.Color LIGHT_SQUARE = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK_SQUARE = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color HIGHLIGHT_COLOR = new java.awt.Color(186, 202, 68, 180);
    private static final java.awt.Color SELECTED_COLOR = new java.awt.Color(246, 246, 105, 200);
    private static final java.awt.Color LAST_MOVE_COLOR = new java.awt.Color(205, 210, 106, 150);
    private static final java.awt.Color CHECK_COLOR = new java.awt.Color(255, 0, 0, 100);

    private Game game;
    private Position selectedSquare;
    private List<Position> highlightedMoves;
    private Move lastMove;
    private boolean flipped;

    public ChessBoardPanel(Game game) {
        this.game = game;
        this.highlightedMoves = new ArrayList<>();
        this.flipped = false;

        setPreferredSize(new Dimension(SQUARE_SIZE * 8, SQUARE_SIZE * 8));

        game.addObserver(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    private void handleClick(int x, int y) {
        Position clickedPos = screenToBoard(x, y);
        if (clickedPos == null || !clickedPos.isValid()) {
            return;
        }

        if (selectedSquare == null) {
            Piece piece = game.getBoard().getPieceAt(clickedPos);
            if (piece != null && piece.getColor() == game.getCurrentPlayer()) {
                selectedSquare = clickedPos;
                highlightedMoves = game.getLegalTargets(clickedPos);
                repaint();
            }
        } else {
            if (highlightedMoves.contains(clickedPos)) {
                Piece piece = game.getBoard().getPieceAt(selectedSquare);
                PieceType promotionType = null;

                if (piece != null && piece.getType() == PieceType.PAWN) {
                    int promotionRank = piece.getColor().getPromotionRank();
                    if (clickedPos.getRank() == promotionRank) {
                        promotionType = showPromotionDialog();
                    }
                }

                game.makeMove(selectedSquare, clickedPos, promotionType);
            }

            selectedSquare = null;
            highlightedMoves.clear();
            repaint();
        }
    }

    private PieceType showPromotionDialog() {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose promotion piece:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        return switch (choice) {
            case 1 -> PieceType.ROOK;
            case 2 -> PieceType.BISHOP;
            case 3 -> PieceType.KNIGHT;
            default -> PieceType.QUEEN;
        };
    }

    private Position screenToBoard(int x, int y) {
        int file = x / SQUARE_SIZE;
        int rank = 7 - (y / SQUARE_SIZE);

        if (flipped) {
            file = 7 - file;
            rank = 7 - rank;
        }

        return new Position(file, rank);
    }

    private Point boardToScreen(Position pos) {
        int file = pos.getFile();
        int rank = pos.getRank();

        if (flipped) {
            file = 7 - file;
            rank = 7 - rank;
        }

        return new Point(file * SQUARE_SIZE, (7 - rank) * SQUARE_SIZE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBoard(g2d);
        drawHighlights(g2d);
        drawPieces(g2d);
        drawCoordinates(g2d);
    }

    private void drawBoard(Graphics2D g) {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                boolean isLight = (rank + file) % 2 == 0;
                g.setColor(isLight ? LIGHT_SQUARE : DARK_SQUARE);

                int displayFile = flipped ? 7 - file : file;
                int displayRank = flipped ? rank : 7 - rank;

                g.fillRect(displayFile * SQUARE_SIZE, displayRank * SQUARE_SIZE,
                        SQUARE_SIZE, SQUARE_SIZE);
            }
        }
    }

    private void drawHighlights(Graphics2D g) {
        if (lastMove != null) {
            g.setColor(LAST_MOVE_COLOR);
            highlightSquare(g, lastMove.getFrom());
            highlightSquare(g, lastMove.getTo());
        }

        if (selectedSquare != null) {
            g.setColor(SELECTED_COLOR);
            highlightSquare(g, selectedSquare);
        }

        for (Position pos : highlightedMoves) {
            g.setColor(HIGHLIGHT_COLOR);
            Point p = boardToScreen(pos);

            Piece targetPiece = game.getBoard().getPieceAt(pos);
            if (targetPiece != null) {
                g.setStroke(new BasicStroke(3));
                g.drawOval(p.x + 5, p.y + 5, SQUARE_SIZE - 10, SQUARE_SIZE - 10);
            } else {
                int dotSize = SQUARE_SIZE / 4;
                g.fillOval(p.x + (SQUARE_SIZE - dotSize) / 2,
                        p.y + (SQUARE_SIZE - dotSize) / 2,
                        dotSize, dotSize);
            }
        }

        if (game.getGameState() == GameState.CHECK || game.getGameState() == GameState.CHECKMATE) {
            Position kingPos = game.getBoard().findKing(game.getCurrentPlayer());
            if (kingPos != null) {
                g.setColor(CHECK_COLOR);
                highlightSquare(g, kingPos);
            }
        }
    }

    private void highlightSquare(Graphics2D g, Position pos) {
        Point p = boardToScreen(pos);
        g.fillRect(p.x, p.y, SQUARE_SIZE, SQUARE_SIZE);
    }

    private void drawPieces(Graphics2D g) {
        Board board = game.getBoard();
        g.setFont(new Font("Serif", Font.PLAIN, SQUARE_SIZE - 10));

        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position pos = new Position(file, rank);
                Piece piece = board.getPieceAt(pos);

                if (piece != null) {
                    Point p = boardToScreen(pos);
                    String symbol = piece.getSymbol();

                    FontMetrics fm = g.getFontMetrics();
                    int textX = p.x + (SQUARE_SIZE - fm.stringWidth(symbol)) / 2;
                    int textY = p.y + (SQUARE_SIZE + fm.getAscent() - fm.getDescent()) / 2;

                    g.setColor(java.awt.Color.DARK_GRAY);
                    g.drawString(symbol, textX + 1, textY + 1);

                    g.setColor(piece.getColor() == chess.model.Color.WHITE ? java.awt.Color.WHITE : java.awt.Color.BLACK);
                    g.drawString(symbol, textX, textY);
                }
            }
        }
    }

    private void drawCoordinates(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 12));

        for (int i = 0; i < 8; i++) {
            int file = flipped ? 7 - i : i;
            char fileLetter = (char) ('a' + file);
            boolean isLightSquare = (7 + i) % 2 == 0;
            g.setColor(isLightSquare ? DARK_SQUARE : LIGHT_SQUARE);
            g.drawString(String.valueOf(fileLetter),
                    i * SQUARE_SIZE + SQUARE_SIZE - 12,
                    8 * SQUARE_SIZE - 3);

            int rank = flipped ? i : 7 - i;
            isLightSquare = (i) % 2 == 0;
            g.setColor(isLightSquare ? DARK_SQUARE : LIGHT_SQUARE);
            g.drawString(String.valueOf(rank + 1), 3, i * SQUARE_SIZE + 14);
        }
    }

    public void flipBoard() {
        flipped = !flipped;
        repaint();
    }

    public void setGame(Game game) {
        if (this.game != null) {
            this.game.removeObserver(this);
        }
        this.game = game;
        game.addObserver(this);
        selectedSquare = null;
        highlightedMoves.clear();
        lastMove = null;
        repaint();
    }

    @Override
    public void onMoveMade(Move move) {
        lastMove = move;
        repaint();
    }

    @Override
    public void onBoardChanged(Board board) {
        repaint();
    }

    @Override
    public void onTurnChanged(chess.model.Color currentPlayer) {
        repaint();
    }

    @Override
    public void onGameStateChanged(GameState state, chess.model.Color affectedPlayer) {
        repaint();

        if (state == GameState.CHECKMATE) {
            JOptionPane.showMessageDialog(this,
                    affectedPlayer.opposite() + " wins by checkmate!",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else if (state == GameState.STALEMATE) {
            JOptionPane.showMessageDialog(this,
                    "Game drawn by stalemate!",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else if (state == GameState.DRAW_BY_REPETITION) {
            JOptionPane.showMessageDialog(this,
                    "Game drawn by threefold repetition.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else if (state == GameState.DRAW_BY_FIFTY_MOVES) {
            JOptionPane.showMessageDialog(this,
                    "Game drawn by 50-move rule.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else if (state == GameState.DRAW_BY_INSUFFICIENT_MATERIAL) {
            JOptionPane.showMessageDialog(this,
                    "Game drawn by insufficient material.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void onPieceCaptured(Piece piece) {
    }

    @Override
    public void onMoveUndone(Move move) {
        lastMove = null;
        repaint();
    }

    @Override
    public void onLegalMovesCalculated(Position from, List<Position> legalMoves) {
    }
}
