package chess.view;

import chess.controller.Game;
import chess.patterns.builder.GameBuilder;
import chess.util.GamePersistence;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Main application window for the chess game.
 */
public class ChessFrame extends JFrame {

    private Game game;
    private ChessBoardPanel boardPanel;
    private MoveHistoryPanel moveHistoryPanel;
    private CapturedPiecesPanel capturedPiecesPanel;
    private StatusPanel statusPanel;
    private final GamePersistence persistence;

    public ChessFrame() {
        super("Java Chess");
        this.persistence = new GamePersistence();

        initializeGame();
        setupUI();
        setupMenuBar();

        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void initializeGame() {
        game = new GameBuilder()
                .withStandardSetup()
                .withUndoEnabled(true)
                .build();
    }

    private void setupUI() {
        setLayout(new BorderLayout(10, 10));

        // Main board panel
        boardPanel = new ChessBoardPanel(game);

        // Right side panel with move history and captured pieces
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        moveHistoryPanel = new MoveHistoryPanel(game);
        capturedPiecesPanel = new CapturedPiecesPanel(game);

        rightPanel.add(capturedPiecesPanel);
        rightPanel.add(moveHistoryPanel);

        // Status bar at bottom
        statusPanel = new StatusPanel(game);

        // Control buttons
        JPanel buttonPanel = createButtonPanel();

        // Layout
        add(boardPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(statusPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.NORTH);

        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> game.undoMove());

        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(e -> game.redoMove());

        JButton flipButton = new JButton("Flip Board");
        flipButton.addActionListener(e -> boardPanel.flipBoard());

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> newGame());

        panel.add(newGameButton);
        panel.add(undoButton);
        panel.add(redoButton);
        panel.add(flipButton);

        return panel;
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setMnemonic(KeyEvent.VK_G);

        JMenuItem newGameItem = new JMenuItem("New Game", KeyEvent.VK_N);
        newGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newGameItem.addActionListener(e -> newGame());

        JMenuItem saveItem = new JMenuItem("Save Game", KeyEvent.VK_S);
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveItem.addActionListener(e -> saveGame());

        JMenuItem loadItem = new JMenuItem("Load Position (FEN)", KeyEvent.VK_L);
        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        loadItem.addActionListener(e -> loadPosition());

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem undoItem = new JMenuItem("Undo Move", KeyEvent.VK_U);
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
        undoItem.addActionListener(e -> game.undoMove());

        JMenuItem redoItem = new JMenuItem("Redo Move", KeyEvent.VK_R);
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
        redoItem.addActionListener(e -> game.redoMove());

        editMenu.add(undoItem);
        editMenu.add(redoItem);

        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem flipItem = new JMenuItem("Flip Board", KeyEvent.VK_F);
        flipItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        flipItem.addActionListener(e -> boardPanel.flipBoard());

        JMenuItem copyFenItem = new JMenuItem("Copy Position (FEN)");
        copyFenItem.addActionListener(e -> copyFEN());

        viewMenu.add(flipItem);
        viewMenu.add(copyFenItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> showAbout());

        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void newGame() {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Start a new game? Current game will be lost.",
                "New Game",
                JOptionPane.YES_NO_OPTION
        );

        if (result == JOptionPane.YES_OPTION) {
            // Remove old observers
            game.removeObserver(boardPanel);
            game.removeObserver(moveHistoryPanel);
            game.removeObserver(capturedPiecesPanel);
            game.removeObserver(statusPanel);

            // Create new game
            game = new GameBuilder()
                    .withStandardSetup()
                    .withUndoEnabled(true)
                    .build();

            // Update existing panels with new game
            boardPanel.setGame(game);

            // Reset and re-register observers
            moveHistoryPanel.clear();
            game.addObserver(moveHistoryPanel);

            capturedPiecesPanel.reset();
            game.addObserver(capturedPiecesPanel);

            statusPanel.reset(game.getCurrentPlayer());
            game.addObserver(statusPanel);

            repaint();
        }
    }

    private void saveGame() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Game");
        chooser.setSelectedFile(new File("chess_game.pgn"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                persistence.saveGame(game, chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Game saved successfully!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error saving game: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadPosition() {
        String fen = JOptionPane.showInputDialog(
                this,
                "Enter FEN string:",
                "Load Position",
                JOptionPane.PLAIN_MESSAGE
        );

        if (fen != null && !fen.trim().isEmpty()) {
            try {
                // Remove old observers
                game.removeObserver(boardPanel);
                game.removeObserver(moveHistoryPanel);
                game.removeObserver(capturedPiecesPanel);
                game.removeObserver(statusPanel);

                game = persistence.loadFromFEN(fen.trim());

                boardPanel.setGame(game);

                moveHistoryPanel.clear();
                game.addObserver(moveHistoryPanel);

                capturedPiecesPanel.reset();
                game.addObserver(capturedPiecesPanel);

                statusPanel.reset(game.getCurrentPlayer());
                game.addObserver(statusPanel);

                repaint();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid FEN string: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void copyFEN() {
        String fen = persistence.toFEN(game.getBoard(), game.getCurrentPlayer(), game.getMoveNumber());
        java.awt.datatransfer.StringSelection selection =
                new java.awt.datatransfer.StringSelection(fen);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        JOptionPane.showMessageDialog(this, "FEN copied to clipboard:\n" + fen);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(
                this,
                """
                Java 2D Chess
                
                A fully playable chess game with:
                - Standard chess rules
                - Click-to-move interface
                - Undo/redo support
                - Save/load games
                
                Design Patterns Used:
                - Strategy (piece movement)
                - Builder (board/game setup)
                - Observer (UI updates)
                - Decorator (piece behavior)
                - Command (undo/redo)
                
                Team: Nick Bolger, Austin Hardy
                """,
                "About Java Chess",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
