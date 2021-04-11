package minesweeper;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class Field {
    private static final int DEFAULT_HEIGHT = 9;
    private static final int DEFAULT_WIDTH = 9;

    private final int height;
    private final int width;
    private final int minesNum;
    private char[][] field;
    private char[][] fieldForUser;
    private int[][] minesCoords;
    private boolean wasTapped = false;

    Field(int height, int width, int minesNum) {
        this.height = height;
        this.width = width;
        this.minesNum = minesNum;
        this.minesCoords = new int[minesNum][2];

        this.field = new char[height][width];
        this.fieldForUser = new char[height][width];
        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; ++y) {
                fieldForUser[x][y] = '.';
            }
        }
    }

    Field(int minesNum) {
        this(DEFAULT_HEIGHT, DEFAULT_WIDTH, minesNum);
    }

    void print() {
        System.out.print(" |");
        for (int x = 1; x <= width; ++x) {
            System.out.print(x);
        }
        System.out.println("|");
        System.out.print("-|");
        for (int x = 1; x <= width; ++x) {
            System.out.print("-");
        }
        System.out.println("|");

        for (int x = 0; x < height; ++x) {
            System.out.print(x + 1 + "|");
            for (int y = 0; y < width; ++y) {
                System.out.print(fieldForUser[x][y]);
            }
            System.out.println("|");
        }

        System.out.print("-|");
        for (int x = 1; x <= width; ++x) {
            System.out.print("-");
        }
        System.out.println("|");
    }

    /**
     * @brief claim as free
     * @return if it was safe
     */
    boolean tap(int x, int y) {
        int[] tapped = new int[]{ x - 1, y - 1 };
        if (!wasTapped) {
            generate(tapped);
            wasTapped = true;
        }
        if (field[tapped[0]][tapped[1]] == 'X') {
            for (int[] mine : minesCoords) {
                fieldForUser[mine[0]][mine[1]] = 'X';
            }
            return false;
        }
        Queue<int[]> sellsToExplore = new ArrayDeque<>();
        sellsToExplore.offer(tapped);
        while (!sellsToExplore.isEmpty()) {
            int[] sell = sellsToExplore.poll();
            int curX = sell[0];
            int curY = sell[1];
            if (field[curX][curY] == '/' && fieldForUser[curX][curY] != '/') {
                if (curX - 1 >= 0) sellsToExplore.offer(new int[]{ curX - 1, curY });
                if (curX + 1 < height) sellsToExplore.offer(new int[]{ curX + 1, curY });
                if (curY - 1 >= 0) sellsToExplore.offer(new int[]{ curX, curY - 1 });
                if (curY + 1 < width) sellsToExplore.offer(new int[]{ curX, curY + 1 });
                if (curX - 1 >= 0 && curY - 1 >= 0) sellsToExplore.offer(new int[]{ curX - 1, curY - 1 });
                if (curX + 1 < height && curY - 1 >= 0) sellsToExplore.offer(new int[]{ curX + 1, curY - 1 });
                if (curX - 1 >= 0 && curY + 1 < width) sellsToExplore.offer(new int[]{ curX - 1, curY + 1 });
                if (curX + 1 < height && curY + 1 < width) sellsToExplore.offer(new int[]{ curX + 1, curY + 1 });
            }
            fieldForUser[curX][curY] = field[curX][curY];
        }
        return true;
    }

    /**
     * @breif set/unset mine mark
     */
    void mark(int x, int y) {
        if (fieldForUser[x - 1][y - 1] == '.') {
            fieldForUser[x - 1][y - 1] = '*';
        } else if (fieldForUser[x - 1][y - 1] == '*') {
            fieldForUser[x - 1][y - 1] = '.';
        }
    }

    boolean isSolved() {
        return wasTapped ? isSolvedByMarkAllMines() || isSolvedByOpenAllSafe() : false;
    }

    private boolean isSolvedByMarkAllMines() {
        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; ++y) {
                if ((fieldForUser[x][y] == '*') ^ (field[x][y] == 'X')) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSolvedByOpenAllSafe() {
        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; ++y) {
                if (field[x][y] != 'X' && (fieldForUser[x][y] == '.' || fieldForUser[x][y] == '*')) {
                    return false;
                }
            }
        }
        return true;
    }

    private void generate(int[] exclude) {
        initWithMines(exclude);
        countMinesAround();
    }

    private void initWithMines(int[] exclude) {
        var indexes = ThreadLocalRandom.current()
                .ints(0, height * width - 1)
                .distinct()
                .limit(minesNum)
                .toArray();
        int excludeIndex = exclude[0] * width + exclude[1];
        for (int i = 0; i < indexes.length; ++i) {
            int mineIndex = indexes[i];
            if (mineIndex >= excludeIndex) {
                ++mineIndex;
            }
            int x = mineIndex / width;
            int y = mineIndex % width;
            minesCoords[i] = new int[]{x, y};
            field[x][y] = 'X';
        }
        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; ++y) {
                if (field[x][y] != 'X') {
                    field[x][y] = '/';
                }
            }
        }
    }

    private void countMinesAround() {
        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; ++y) {
                if (field[x][y] == '/') {
                    int minesAround = 0;
                    minesAround += x != 0 && y != 0 && field[x - 1][y - 1] == 'X' ? 1 : 0;
                    minesAround += x != 0 && field[x - 1][y] == 'X' ? 1 : 0;
                    minesAround += x != 0 && y != width - 1 && field[x - 1][y + 1] == 'X' ? 1 : 0;
                    minesAround += y != 0 && field[x][y - 1] == 'X' ? 1 : 0;
                    minesAround += y != width - 1 && field[x][y + 1] == 'X' ? 1 : 0;
                    minesAround += x != height - 1 && y != 0 && field[x + 1][y - 1] == 'X' ? 1 : 0;
                    minesAround += x != height - 1 && field[x + 1][y] == 'X' ? 1 : 0;
                    minesAround += x != height - 1 && y != width - 1 && field[x + 1][y + 1] == 'X' ? 1 : 0;
                    if (minesAround > 0) {
                        field[x][y] = (char) (minesAround + 48);
                    }
                }
            }
        }
    }
}
