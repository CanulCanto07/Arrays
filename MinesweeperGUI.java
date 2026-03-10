import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MinesweeperGUI extends JFrame {

    private Minesweeper game;
    private JButton[][] cellButtons;
    private boolean[][] revealed;
    private JLabel minesLabel;
    private JLabel timerLabel;
    private JButton resetButton;
    private Timer timer;
    private JPanel gamePanel;

    private int rows = 9;
    private int cols = 9;
    private int mines = 10;
    private int flagsPlaced;
    private boolean gameFinished;

    public MinesweeperGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("No se pudo aplicar el Look and Feel del sistema.");
        }

        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGameItem = new JMenuItem("New");
        JMenu difficultyMenu = new JMenu("Difficulty");
        JMenuItem easyItem = new JMenuItem("Easy");
        JMenuItem mediumItem = new JMenuItem("Medium");
        JMenuItem hardItem = new JMenuItem("Hard");
        JMenuItem customItem = new JMenuItem("Custom...");

        newGameItem.addActionListener(e -> resetGame());
        easyItem.addActionListener(e -> setDifficulty(9, 9, 10));
        mediumItem.addActionListener(e -> setDifficulty(16, 16, 40));
        hardItem.addActionListener(e -> setDifficulty(16, 30, 99));
        customItem.addActionListener(e -> showCustomDialog());

        difficultyMenu.add(easyItem);
        difficultyMenu.add(mediumItem);
        difficultyMenu.add(hardItem);
        difficultyMenu.add(customItem);

        gameMenu.add(newGameItem);
        gameMenu.addSeparator();
        gameMenu.add(difficultyMenu);

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createLoweredBevelBorder()));

        minesLabel = new JLabel(String.format("%03d", mines));
        minesLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        minesLabel.setForeground(Color.RED);
        minesLabel.setOpaque(true);
        minesLabel.setBackground(Color.BLACK);
        minesLabel.setHorizontalAlignment(SwingConstants.CENTER);

        timerLabel = new JLabel("000");
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        timerLabel.setForeground(Color.RED);
        timerLabel.setOpaque(true);
        timerLabel.setBackground(Color.BLACK);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        timer = new Timer(1000, e -> updateTimer());

        resetButton = new JButton("🙂");
        resetButton.setFont(new Font("Arial Unicode MS", Font.PLAIN, 20));
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetGame());

        topPanel.add(minesLabel, BorderLayout.WEST);
        topPanel.add(resetButton, BorderLayout.CENTER);
        topPanel.add(timerLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        startGame();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startGame() {
        if (gamePanel != null) {
            remove(gamePanel);
        }

        gamePanel = new JPanel(new GridLayout(rows, cols));
        gamePanel.setBorder(BorderFactory.createLoweredBevelBorder());

        game = new Minesweeper(rows, cols, mines);
        game.generateMines();
        game.scanAndAssignNumbers();

        cellButtons = new JButton[rows][cols];
        revealed = new boolean[rows][cols];
        flagsPlaced = 0;
        gameFinished = false;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(25, 25));
                button.setFocusable(false);
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setText("");

                final int r = i;
                final int c = j;

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameFinished) {
                            return;
                        }

                        if (SwingUtilities.isLeftMouseButton(e)) {
                            revealCell(r, c);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            flagCell(r, c);
                        }
                    }
                });

                cellButtons[i][j] = button;
                gamePanel.add(button);
            }
        }

        add(gamePanel, BorderLayout.CENTER);

        minesLabel.setText(String.format("%03d", mines - flagsPlaced));
        timer.stop();
        timerLabel.setText("000");
        resetButton.setText("🙂");

        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
    }

    private void revealCell(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) {
            return;
        }

        if (revealed[r][c]) {
            return;
        }

        JButton cell = cellButtons[r][c];

        if ("F".equals(cell.getText())) {
            return;
        }

        if (!timer.isRunning()) {
            timer.start();
        }

        revealed[r][c] = true;

        cell.setOpaque(true);
        cell.setBackground(new Color(220, 220, 220));
        cell.setBorder(BorderFactory.createLoweredBevelBorder());

        int value = game.getMinesweeperMatrix()[r][c];

        if (value == -1) {
            cell.setBackground(Color.RED);
            cell.setText("M");
            gameOver(false);
            return;
        }

        cell.setFont(new Font("Monospaced", Font.BOLD, 14));
        cell.setForeground(getColorForNumber(value));
        cell.setText(value > 0 ? String.valueOf(value) : "");

        if (value == 0) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (i == 0 && j == 0) {
                        continue;
                    }
                    revealCell(r + i, c + j);
                }
            }
        }

        checkWinCondition();
    }

    private void flagCell(int r, int c) {
        if (revealed[r][c]) {
            return;
        }

        JButton cell = cellButtons[r][c];

        if ("F".equals(cell.getText())) {
            cell.setText("");
            flagsPlaced--;
        } else {
            cell.setText("F");
            flagsPlaced++;
        }

        minesLabel.setText(String.format("%03d", mines - flagsPlaced));
    }

    private Color getColorForNumber(int number) {
        switch (number) {
            case 1:
                return Color.BLUE;
            case 2:
                return new Color(0, 128, 0);
            case 3:
                return Color.RED;
            case 4:
                return new Color(0, 0, 128);
            case 5:
                return new Color(128, 0, 0);
            case 6:
                return new Color(0, 128, 128);
            case 7:
                return Color.BLACK;
            case 8:
                return Color.GRAY;
            default:
                return Color.BLACK;
        }
    }

    private void gameOver(boolean win) {
        gameFinished = true;
        timer.stop();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (game.getMinesweeperMatrix()[i][j] == -1) {
                    cellButtons[i][j].setText("M");
                }
            }
        }

        if (win) {
            resetButton.setText("😎");
            JOptionPane.showMessageDialog(this, "You win!", "Congratulations", JOptionPane.INFORMATION_MESSAGE);
        } else {
            resetButton.setText("😵");
            JOptionPane.showMessageDialog(this, "Game Over!", "Boom!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkWinCondition() {
        int revealedCount = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (revealed[i][j]) {
                    revealedCount++;
                }
            }
        }

        if (revealedCount == (rows * cols) - game.getMines()) {
            gameOver(true);
        }
    }

    private void resetGame() {
        setDifficulty(this.rows, this.cols, this.mines);
    }

    private void setDifficulty(int r, int c, int m) {
        this.rows = r;
        this.cols = c;
        this.mines = m;
        startGame();
    }

    private void showCustomDialog() {
        JTextField rowsField = new JTextField(Integer.toString(rows));
        JTextField colsField = new JTextField(Integer.toString(cols));
        JTextField minesField = new JTextField(Integer.toString(mines));

        Object[] message = {
                "Rows:", rowsField,
                "Columns:", colsField,
                "Mines:", minesField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Custom Difficulty", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            try {
                int r = Integer.parseInt(rowsField.getText());
                int c = Integer.parseInt(colsField.getText());
                int m = Integer.parseInt(minesField.getText());

                if (r <= 0 || c <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "Rows and columns must be greater than zero.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (m <= 0 || m >= (r * c)) {
                    JOptionPane.showMessageDialog(this,
                            "The number of mines must be greater than 0 and less than the number of cells.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                setDifficulty(r, c, m);

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Invalid input. Please enter numbers only.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTimer() {
        int currentTime = Integer.parseInt(timerLabel.getText());
        if (currentTime < 999) {
            currentTime++;
        }
        timerLabel.setText(String.format("%03d", currentTime));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MinesweeperGUI::new);
    }
}