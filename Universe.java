package life;

import java.util.Random;

class Universe {
    private final int size;

    private boolean[][] currentGeneration;
    private boolean[][] nextGeneration;

    private int generationNumber;

    public Universe(int n) {
        size = n;
        currentGeneration = generateFirstGeneration();
        nextGeneration = generateNextGeneration();
        generationNumber = 1;
    }

    public void moveGenerations() {
        currentGeneration = nextGeneration;
        nextGeneration = generateNextGeneration();
        generationNumber++;
    }

    public boolean[][] getCurrentGeneration() {
        return currentGeneration;
    }

    public int getGenerationNumber() {
        return generationNumber;
    }

    public int getAliveCellsCount() {
        int aliveCellsCount = 0;

        for (boolean[] row : currentGeneration) {
            for (boolean c : row) {
                if (c) {
                    aliveCellsCount++;
                }
            }
        }
        return aliveCellsCount;
    }

    private boolean[][] generateFirstGeneration() {
        Random random = new Random();
        boolean[][] generation = new boolean[size][size];

        for (boolean[] row : generation) {
            for (int i = 0; i < row.length; i++) {
                row[i] = random.nextBoolean();
            }
        }
        return generation;
    }

    private boolean[][] generateNextGeneration() {
        boolean[][] generation = new boolean[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                generation[i][j] = isCellAlive(i, j);
            }
        }
        return generation;
    }

    private boolean isCellAlive(int x, int y) {
        int aliveNeighboursCount = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i != 1 || j != 1) {
                    int nx = (x + size - 1 + i) % size;
                    int ny = (y + size - 1 + j) % size;
                    if (currentGeneration[nx][ny]) {
                        aliveNeighboursCount++;
                    }
                }
            }
        }
        return currentGeneration[x][y] ? aliveNeighboursCount == 2 || aliveNeighboursCount == 3 : aliveNeighboursCount == 3;
    }
}