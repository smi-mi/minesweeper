package minesweeper;

import java.util.Scanner;

public class Game {
    private final Field field;
    private final Scanner scanner;

    public Game() {
        this.scanner = new Scanner(System.in);
        this.field = new Field(askForMinesNum());
    }

    public void run() {
        while (!field.isSolved()) {
            printField();
            int[] coords = askForCoords();
            if (coords[2] == 1) {
                field.mark(coords[0], coords[1]);
            } else {
                if (!field.tap(coords[0], coords[1])) {
                    gameOver();
                    return;
                }
            }
        }
        congratulate();
    }

    private int askForMinesNum() {
        System.out.println("How many mines do you want on the field?");
        return scanner.nextInt();
    }

    /**
     * @return last number is 1 if type is mine else 0
     */
    private int[] askForCoords() {
        System.out.println("Set/unset mines marks or claim a cell as free:");
        int x = scanner.nextInt();
        int y = scanner.nextInt();
        String type = scanner.next();
        return new int[]{ y, x, "mine".equals(type) ? 1 : 0 };
    }

    private void congratulate() {
        printField();
        System.out.println("Congratulations! You found all the mines!");
    }

    private void gameOver() {
        printField();
        System.out.println("You stepped on a mine and failed!");
    }

    private void printField() {
        field.print();
    }
}
