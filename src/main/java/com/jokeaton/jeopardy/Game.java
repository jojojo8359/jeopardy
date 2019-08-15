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

/**
 * Game logic classes for different modes
 * @author Joel Keaton
 */
public class Game {
    private static Logger logger = LogManager.getLogger(Game.class);

    public Game() {}

    /**
     * Starts a singleplayer game
     * @throws IOException caused by get method
     */
    public static void singlePlayer() throws IOException {
        logger.info("Create new board");
        Board board = new Board(); // Creates new board instance
        logger.info("Generate random board");
        board.genRandomBoard(); // Generates random categories and clues for the board
        Scanner scanner = new Scanner(System.in);
        int balance = 0; // Initializes user's balance
        logger.info("Starting game");
        while(!board.boardAnswered()) { // While the board still has clues on it
            System.out.println("        A              B              C              D              E              F       "); // Print column headers
            System.out.println(board);
            System.out.println("Balance: $" + balance);
            while(true) { // Main guess loop
                int col;
                int row = 0;
                while (true) { // Category selection loop
                    System.out.println();
                    System.out.print("Choose a category [A-F]: ");
                    String column = scanner.nextLine().toLowerCase();
                    column = column.trim(); // Remove whitespace
                    if(column.equals("quit") || column.equals("exit")) { // Breakout method
                        System.exit(0);
                    }
                    if (column.length() == 1) { // Expects a single character...
                        int code = column.charAt(0);
                        if(code >= 97 && code <= 102) { // From a-f (decimal 97-102)
                            col = code - 97; // Converts to base 0 (a = 0, b = 1, c = 2, etc.)
                            logger.info("Chose column " + col);
                            break;
                        }
                        else {
                            System.out.println("I couldn't recognize that! Please try again!"); // Make user try again if it isn't from a-f
                        }
                    } else {
                        System.out.println("I couldn't recognize that! Please try again!"); // Make user try again if it isn't one character
                    }
                }
                boolean reported = false; // Adds break variable for if the user reports the selected category
                while (true) {
                    if (board.getMode().equals("normal")) { // If the board has values from 100-500 (for formatting text)
                        System.out.println();
                        System.out.print("Choose a value [100-500]: ");
                        String value = scanner.nextLine();
                        if(value.equals("!r")) { // If the user types "!r", show the category's id for reporting purposes.
                            System.out.println("Only use the report link if something is wrong with the category text. This category's ID is " + board.getCategory(col).getId());
                            logger.warn("Reported Category #" + board.getCategory(col).getId());
                            reported = true; // Allows the user to rechoose a clue after getting the report id
                            break;
                        }
                        else if (value.length() == 1 || value.length() == 3) { // If the user's input is either 1 or 3 characters long (ex. 1 / 100)
                            if ((int) value.charAt(0) >= 49 && (int) value.charAt(0) <= 53) { // Checks if the number is in between 1-5 (decimal 49-53)
                                row = value.charAt(0) - 48; // Converts to base 1 to accommodate for categories in first row
                                logger.info("Chose row " + row);
                                break;
                            }
                        }
                        System.out.println("I couldn't recognize that! Please try again!"); // Make user try again if it isn't a number or 1/3 characters
                    }
                }
                if(reported) { // Report break
                    break;
                }
                if (!board.isAnswered(col, row)) { // Checks if the clue selected is not answered
                    int value = row * 100; // Scales row value up to the amount
                    System.out.println();
                    boolean canWager = true; // If the user's balance is below 1, they cannot wager on a Daily Double
                    if (board.dailyDouble(col, row)) {
                        System.out.println();
                        System.out.println("Daily double found!");
                        int wager = wager(balance); // wager() method takes care of handling the wager input
                        value = wager;
                        if (wager == 0) { // If the user cannot wager, break out of the whole loop (choose another clue) and mark current clue as answered
                            canWager = false;
                            board.getClue(col, row).setAnswered(true);
                        }
                    }
                    if(!canWager) { // Wager break
                        break;
                    }
                    System.out.println();
                    System.out.println("For $" + value + ":");
                    GridTable g = GridTable.of(1, 1); // Displays the question in a simple box
                    g.put(0, 0, Collections.singleton(board.getClue(col, row).getQuestion()));
                    g.apply(VERTICAL_CENTER).apply(HORIZONTAL_CENTER);
                    g = Border.SINGLE_LINE.apply(g);
                    Util.print(g);
                    System.out.print("What/who is "); // Prompt for the user to answer the question
                    String answer = scanner.nextLine();
                    logger.info("Guessed " + answer + ", answer " + board.getClue(col, row).getAnswer());
                    if(answer.equals("!r") || board.getClue(col, row).getAnswer().equals("")) { // Triggers report case if the user types "!r" or the question is somehow empty
                        System.out.println("Only use the report link if something is wrong with the clue text. This clue's ID is " + board.getClue(col, row).getId());
                        logger.warn("Reported Clue #" + board.getClue(col, row).getId());
                        continue; // Sends user to the beginning of the guess loop
                    }
                    if (checkAnswer(answer, board.getClue(col, row).getAnswer())) { // Utilizes checkAnswer() to make sure the answer and guess are similar enough
                        System.out.println(" _______ _________ _______          _________"); // All ASCII Art generated with http://patorjk.com/software/taag/
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
                    board.getClue(col, row).setAnswered(true); // Mark clue as answered
                    System.out.print("[Enter] "); // Prompts the user to press enter
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
                System.out.print("Enter a wager from 1-" + balance + ": $"); // Minimum wager is 1, maximum wager is the user's current balance
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
        if(guess.length() == 0) {return false;} // If the guess is empty, it is automatically wrong (false)
        guess = guess.toLowerCase().trim(); // Removes outer whitespace
        answer = answer.toLowerCase().trim();
        guess = removeString(guess, "the "); // Removes the articles the, an, a, and your from both guess and answer (possibly more in the future)
        guess = removeString(guess, "an ");
        guess = removeString(guess, "a ");
        guess = removeString(guess, "your ");
        answer = removeString(answer, "the ");
        answer = removeString(answer, "an ");
        answer = removeString(answer, "a ");
        answer = removeString(answer, "your ");

        guess = Normalizer.normalize(guess, Normalizer.Form.NFD); // Removes all accents from both strings, replaces them with their normal counterparts
        answer = Normalizer.normalize(answer, Normalizer.Form.NFD);
        guess = guess.replaceAll("[^\\p{ASCII}]", "");
        answer = answer.replaceAll("[^\\p{ASCII}]", "");

        guess = guess.replaceAll("\"", ""); // Removes all quotations
        answer = answer.replaceAll("\"", "");

        guess = guess.replaceAll("\\?", ""); // Removes all question marks
        answer = answer.replaceAll("\\?", "");

        guess = guess.replaceAll("<[^>]*>", ""); // https://stackoverflow.com/a/4075756
        answer = answer.replaceAll("<[^>]*>", ""); // Removes all HTML formatting tags (ex. <i>, <b>)

        if(distance(guess, answer) <= 2) { // Uses Levenshtein distance with a threshold of 2 (translates to 2 typos between guess and answer)
            return true;
        }
        else { // If it doesn't pass the distance test,
            if(guess.contains(answer)) { // checks if the answer is in the user's guess (ex. Korea in Korean War)
                return true;
            }
            else {
                return false;
            }
        }
    }

    public static int distance(String s, String t) { // https://en.wikipedia.org/wiki/Levenshtein_distance#Iterative_with_full_matrix
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

    public static String removeString(String original, String sub) { // Method for removing a substring (articles) from the beginning of a string
        if(original.length() >= sub.length() && original.substring(0, sub.length()).toLowerCase().equals(sub)) {
            original = original.substring(sub.length());
        }
        return original;
    }
}
