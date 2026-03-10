public class Minesweeper {
    private final int[][] minesweeperMatrix;
    private final int rows;
    private final int cols;
    private final int mines;

    public Minesweeper(int rows, int cols, int mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.minesweeperMatrix = new int[rows][cols];
    }

    public int[][] getMinesweeperMatrix() {
        return minesweeperMatrix;
    }

    public int getMines() {
        return mines;
    }

    public void generateMines() {
        int counterMines = this.mines;

        while (counterMines > 0) {
            int row = (int) (Math.random() * this.rows);
            int col = (int) (Math.random() * this.cols);

            if (minesweeperMatrix[row][col] == 0) {
                minesweeperMatrix[row][col] = -1;
                counterMines--;
            }
        }
    }

    public void scanAndAssignNumbers() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                int counterMines = 0;

                for (int k = i - 1; k < i + 2; k++) {
                    for (int l = j - 1; l < j + 2; l++) {
                        if (k >= 0 && k < this.rows && l >= 0 && l < this.cols) {
                            if (k != i || l != j) {
                                if (minesweeperMatrix[k][l] == -1) {
                                    counterMines++;
                                }
                            }
                        }
                    }
                }

                if (minesweeperMatrix[i][j] == 0) {
                    minesweeperMatrix[i][j] = counterMines;
                }
            }
        }
    }

    public void printMatrix() {
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                System.out.print(minesweeperMatrix[i][j] + "\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Minesweeper ms = new Minesweeper(30, 16, 99);
        ms.generateMines();
        ms.scanAndAssignNumbers();
        ms.printMatrix();
    }
}