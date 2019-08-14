package main.java.com.jokeaton.jeopardy;

import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Scanner;

import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.VERTICAL_CENTER;

public class Game {
    private static Logger logger = LogManager.getLogger(Game.class);

    public Game() {}

    public static void singlePlayer() throws IOException {
        logger.info("Create new board");
        Board board = new Board();
        logger.info("Generate random board");
        board.genRandomBoard();
        Scanner scanner = new Scanner(System.in);
        int balance = 0;
        logger.info("Starting game");
        while(!board.boardAnswered()) {
            System.out.println(board);
            System.out.println("Balance: $" + balance + "\n");
            while(true) {
                int col;
                int row = 0;
                while (true) {
                    System.out.print("Choose a category [A-F]: ");
                    String column = scanner.nextLine().toLowerCase();
                    if(column.equals("quit") || column.equals("exit")) {
                        System.exit(0);
                    }
                    if (column.length() == 1) {
                        int code = column.charAt(0);
                        col = code - 97;
                        logger.info("Chose column " + col);
                        break;
                    } else {
                        System.out.println("I couldn't recognize that! Please try again!");
                    }
                }
                boolean reported = false;
                while (true) {
                    if (board.getMode().equals("normal")) {
                        System.out.print("Choose a value [100-500]: ");
                        String value = scanner.nextLine();
                        if(value.equals("!r")) {
                            System.out.println("Only use the report link if something is wrong with the category text. This category's ID is " + board.getCategory(col).getId());
                            logger.warn("Reported Category #" + board.getCategory(col).getId());
                            reported = true;
                            break;
                        }
                        else if (value.length() == 1 || value.length() == 3) {
                            if ((int) value.charAt(0) >= 49 && (int) value.charAt(0) <= 53) {
                                row = value.charAt(0) - 48;
                                logger.info("Chose row " + row);
                                break;
                            }
                        }
                        System.out.println("I couldn't recognize that! Please try again!");
                    }
                }
                if(reported) {
                    break;
                }
                if (!board.isAnswered(col, row)) {
                    int value = row * 100;
                    if (board.dailyDouble(col, row)) {
                        System.out.println("Daily double found!");
                        value *= 2;
                    }
                    System.out.println("For $" + value + ":");
                    GridTable g = GridTable.of(1, 1);
                    g.put(0, 0, Collections.singleton(board.getClue(col, row).getQuestion()));
                    g.apply(VERTICAL_CENTER).apply(HORIZONTAL_CENTER);
                    g = Border.SINGLE_LINE.apply(g);
                    Util.print(g);
                    System.out.print("What/who is ");
                    String answer = scanner.nextLine().toLowerCase();
                    if(answer.equals("!r")) {
                        System.out.println("Only use the report link if something is wrong with the clue text. This clue's ID is " + board.getClue(col, row).getId());
                        logger.warn("Reported Clue #" + board.getClue(col, row).getId());
                        break;
                    }
                    if (answer.equals(board.getClue(col, row).getAnswer().toLowerCase())) {
                        System.out.println("Correct!   + $" + value);
                        balance += value;
                    } else {
                        System.out.println("Incorrect! - $" + value);
                        System.out.println("The correct answer was " + board.getClue(col, row).getAnswer());
                        balance -= value;
                    }
                    board.getClue(col, row).setAnswered(true);
                    break;
                } else {
                    System.out.println("This clue has already been guessed! Please try again!");
                    logger.info("Invalid spot");
                }
            }
        }
    }
}
