package main.java.com.jokeaton.jeopardy;

import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.Normalizer;
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
            System.out.println("        A              B              C              D              E              F       ");
            System.out.println(board);
            System.out.println("Balance: $" + balance);
            while(true) {
                int col;
                int row = 0;
                while (true) {
                    System.out.println();
                    System.out.print("Choose a category [A-F]: ");
                    String column = scanner.nextLine().toLowerCase();
                    if(column.equals("quit") || column.equals("exit")) {
                        System.exit(0);
                    }
                    if (column.length() == 1) {
                        int code = column.charAt(0);
                        if(code >= 97 && code <= 102) {
                            col = code - 97;
                            logger.info("Chose column " + col);
                            break;
                        }
                        else {
                            System.out.println("I couldn't recognize that! Please try again!");
                        }
                    } else {
                        System.out.println("I couldn't recognize that! Please try again!");
                    }
                }
                boolean reported = false;
                while (true) {
                    if (board.getMode().equals("normal")) {
                        System.out.println();
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
                    System.out.println();
                    boolean canWager = true;
                    if (board.dailyDouble(col, row)) {
                        System.out.println();
                        System.out.println("Daily double found!");
                        int wager = wager(balance);
                        value = wager;
                        if (wager == 0) {
                            canWager = false;
                            board.getClue(col, row).setAnswered(true);
                        }
                    }
                    if(!canWager) {
                        break;
                    }
                    System.out.println();
                    System.out.println("For $" + value + ":");
                    GridTable g = GridTable.of(1, 1);
                    g.put(0, 0, Collections.singleton(board.getClue(col, row).getQuestion()));
                    g.apply(VERTICAL_CENTER).apply(HORIZONTAL_CENTER);
                    g = Border.SINGLE_LINE.apply(g);
                    Util.print(g);
                    System.out.print("What/who is ");
                    String answer = scanner.nextLine();
                    logger.info("Guessed " + answer + ", answer " + board.getClue(col, row).getAnswer());
                    if(answer.equals("!r") || board.getClue(col, row).getAnswer().equals("")) {
                        System.out.println("Only use the report link if something is wrong with the clue text. This clue's ID is " + board.getClue(col, row).getId());
                        logger.warn("Reported Clue #" + board.getClue(col, row).getId());
                        break;
                    }
                    if (checkAnswer(answer, board.getClue(col, row).getAnswer())) { // All ASCII Art generated with http://patorjk.com/software/taag/
                        System.out.println(" _______ _________ _______          _________");
                        System.out.println("(  ____ )\\__   __/(  ____ \\|\\     /|\\__   __/");
                        System.out.println("| (    )|   ) (   | (    \\/| )   ( |   ) (   ");
                        System.out.println("| (____)|   | |   | |      | (___) |   | |   ");
                        System.out.println("|     __)   | |   | | ____ |  ___  |   | |   ");
                        System.out.println("| (\\ (      | |   | | \\_  )| (   ) |   | |   ");
                        System.out.println("| ) \\ \\_____) (___| (___) || )   ( |   | |   ");
                        System.out.println("|/   \\__/\\_______/(_______)|/     \\|   )_(   ");
                        System.out.println("+$" + value);
                        System.out.println("The correct answer was " + board.getClue(col, row).getAnswer()); // TODO DELETE
                        balance += value;
                    } else {
                        System.out.println("          _______  _______  _        _______ ");
                        System.out.println("|\\     /|(  ____ )(  ___  )( (    /|(  ____ \\");
                        System.out.println("| )   ( || (    )|| (   ) ||  \\  ( || (    \\/");
                        System.out.println("| | _ | || (____)|| |   | ||   \\ | || |      ");
                        System.out.println("| |( )| ||     __)| |   | || (\\ \\) || | ____ ");
                        System.out.println("| || || || (\\ (   | |   | || | \\   || | \\_  )");
                        System.out.println("| () () || ) \\ \\__| (___) || )  \\  || (___) |");
                        System.out.println("(_______)|/   \\__/(_______)|/    )_)(_______)");
                        System.out.println("-$" + value);
                        System.out.println("The correct answer was " + board.getClue(col, row).getAnswer());
                        balance -= value;
                    }
                    board.getClue(col, row).setAnswered(true);
                    System.out.print("[Enter] ");
                    scanner.nextLine();
                    break;
                } else {
                    System.out.println("This clue has already been guessed! Please try again!");
                    logger.info("Invalid spot");
                }
            }
        }
    }

    public static int wager(int balance) {
        Scanner scanner = new Scanner(System.in);
        while(true) {
            if(balance > 1) {
                System.out.print("Enter a wager from 1-" + balance + ": $");
                String wagerRaw = scanner.nextLine();
                int wager = Integer.parseInt(wagerRaw);
                if (wager >= 1 && wager <= balance) {
                    return wager;
                } else if (wager < 1) {
                    System.out.println("Wager has to be more than $1. Try again.");
                } else {
                    System.out.println("Wager has to be less than your balance.");
                }
            }
            else {
                System.out.println("You can't wager with no money!");
                return 0;
            }
        }
    }

    public static boolean checkAnswer(String guess, String answer) {
        if(guess.length() == 0) {return false;}
        guess = guess.toLowerCase().trim();
        answer = answer.toLowerCase().trim();
        guess = removeString(guess, "the ");
        guess = removeString(guess, "an ");
        guess = removeString(guess, "a ");
        guess = removeString(guess, "your ");

        answer = removeString(answer, "the ");
        answer = removeString(answer, "an ");
        answer = removeString(answer, "a ");
        answer = removeString(answer, "your ");

        guess = Normalizer.normalize(guess, Normalizer.Form.NFD);
        answer = Normalizer.normalize(answer, Normalizer.Form.NFD);

        guess = guess.replaceAll("[^\\p{ASCII}]", "");
        answer = answer.replaceAll("[^\\p{ASCII}]", "");

        guess = guess.replaceAll("\"", "");
        answer = answer.replaceAll("\"", "");

        guess = guess.replaceAll("\\?", "");
        answer = answer.replaceAll("\\?", "");

        guess = guess.replaceAll("<[^>]*>", ""); // https://stackoverflow.com/questions/4075742/regex-to-strip-html-tags
        answer = answer.replaceAll("<[^>]*>", "");

        if(distance(guess, answer) <= 2) {
            return true;
        }
        else {
            if(guess.contains(answer)) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    public static int distance(String s, String t) { // https://en.wikipedia.org/wiki/Levenshtein_distance
        int m = s.length() + 1;
        int n = t.length() + 1;
        int[][] d = new int[m][n];

        for(int i = 1; i < m; i++) {d[i][0] = i;}
        for(int j = 1; j < n; j++) {d[0][j] = j;}
        int substitutionCost;
        for(int j = 1; j < n; j++) {
            for(int i = 1; i < m; i++) {
                substitutionCost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i-1][j] + 1, d[i][j-1] + 1), d[i-1][j-1] + substitutionCost);
            }
        }
        return d[m - 1][n - 1];
    }

    public static String removeString(String original, String sub) {
        if(original.length() >= sub.length() && original.substring(0, sub.length()).toLowerCase().equals(sub)) {
            original = original.substring(sub.length());
        }
        return original;
    }
}
