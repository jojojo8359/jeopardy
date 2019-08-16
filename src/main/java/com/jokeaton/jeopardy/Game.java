package main.java.com.jokeaton.jeopardy;

import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.VERTICAL_CENTER;

/**
 * Game logic classes for different modes
 * @author Joel Keaton
 */
public class Game {
    private static Logger logger = LogManager.getLogger(Game.class);
    private static Scanner scanner = new Scanner(System.in);

    public Game() {}

    /**
     * Starts a singleplayer game
     * @throws IOException caused by get method
     */
    public static int singlePlayer(int mode, int balance) throws IOException {
        logger.info("===========================");
        logger.info("Create new board");
        Board board = new Board(); // Creates new board instance
        logger.info("Generate random board");
        board.genRandomBoard(mode); // Generates random categories and clues for the board
        logger.info("Starting game");
        while(!board.boardAnswered()) { // While the board still has clues on it
            printNormalBoard(board, balance);
            while(true) { // Main guess loop
                int col;
                int row = 0;
                col = getColumn(); // Gets column input from user
                row = getRow(board, col);
                if(row == -1) {break;} // Report break
                if (!board.isAnswered(col, row)) { // Checks if the clue selected is not answered
                    int value;
                    if(board.getMode().equals("normal")) { // Scales row value up to the amount
                        value = row * 200;
                    }
                    else {
                        value = row * 400;
                    }
                    System.out.println();
                    value = checkDailyDouble(board, col, row, balance, value); // Returns the modified value of the clue after getting wager input
                    if(value == -1) {break;} // Wager break
                    printClue(board, col, row, value); // Prints the clue to the board in its box
                    int balanceBefore = balance;
                    balance = processGuess(board.getClue(col, row), value, balance);
                    if(balance == -balanceBefore) {
                        balance = -balance;
                        break;
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
        return balance;
    }

    public static int processGuess(Clue clue, int value, int balance) {
        System.out.print("What/who is "); // Prompt for the user to answer the question
        String answer = scanner.nextLine();
        logger.info("Guessed " + answer + ", answer " + clue.getAnswer());
        if(answer.equals("!r") || clue.getAnswer().equals("")) { // Triggers report case if the user types "!r" or the question is somehow empty
            System.out.println("Only use the report link if something is wrong with the clue text. This clue's ID is " + clue.getId());
            logger.warn("Reported Clue #" + clue.getId());
            balance = -balance; // Sends user to the beginning of the guess loop
            return balance;
        }
        if (checkAnswer(answer, clue.getAnswer())) { // Utilizes checkAnswer() to make sure the answer and guess are similar enough
            System.out.println(" _______ _________ _______          _________"); // All ASCII Art generated with http://patorjk.com/software/taag/
            System.out.println("(  ____ )\\__   __/(  ____ \\|\\     /|\\__   __/");
            System.out.println("| (    )|   ) (   | (    \\/| )   ( |   ) (   ");
            System.out.println("| (____)|   | |   | |      | (___) |   | |   ");
            System.out.println("|     __)   | |   | | ____ |  ___  |   | |   ");
            System.out.println("| (\\ (      | |   | | \\_  )| (   ) |   | |   ");
            System.out.println("| ) \\ \\_____) (___| (___) || )   ( |   | |   ");
            System.out.println("|/   \\__/\\_______/(_______)|/     \\|   )_(   ");
            System.out.println("+$" + value);
            System.out.println("The correct answer was " + parseAnswer(clue.getAnswer())); // TODO DELETE
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
            System.out.println("The correct answer was " + parseAnswer(clue.getAnswer()));
            balance -= value;
        }
        return balance;
    }

    public static void printClue(Board board, int col, int row, int value) {
        System.out.println();
        System.out.println("For $" + value + ":");
        GridTable g = GridTable.of(1, 1); // Displays the question in a simple box
        g.put(0, 0, Collections.singleton(board.getClue(col, row).getQuestion()));
        g.apply(VERTICAL_CENTER).apply(HORIZONTAL_CENTER);
        g = Border.SINGLE_LINE.apply(g);
        Util.print(g);
    }

    public static int checkDailyDouble(Board board, int col, int row, int balance, int value) {
        if (board.dailyDouble(col, row)) {
            System.out.println();
            System.out.println("Daily double found!");
            int wager = wager(balance); // wager() method takes care of handling the wager input
            value = wager;
            if (wager == 0) { // If the user cannot wager, break out of the whole loop (choose another clue) and mark current clue as answered
                value = -1;
                board.getClue(col, row).setAnswered(true);
            }
        }
        return value; // If the clue is not a Daily Double, just spit the value back out again
    }

    public static int getColumn() {
        int col;
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
        return col;
    }

    public static int getRow(Board board, int col) {
        int row;
        while (true) {
//            if (board.getMode().equals("normal")) { // If the board has values from 100-500 (for formatting text)
            System.out.println();
            if(board.getMode().equals("normal")) {System.out.print("Choose a value [200-1000]: ");}
            else if(board.getMode().equals("double")) {System.out.print("Choose a value [400-2000]: ");}
            String value = scanner.nextLine();
            if(value.equals("!r")) { // If the user types "!r", show the category's id for reporting purposes.
                System.out.println("Only use the report link if something is wrong with the category text. This category's ID is " + board.getCategory(col).getId());
                logger.warn("Reported Category #" + board.getCategory(col).getId());
                return -1; // Allows the user to rechoose a clue after getting the report id
            }

            if(board.getMode().equals("double")) {
                if(value.equals("4") || value.equals("400")) {row = 1; break;}
                else if(value.equals("8") || value.equals("800")) {row = 2; break;}
                else if(value.equals("12") || value.equals("1200")) {row = 3; break;}
                else if(value.equals("16") || value.equals("1600")) {row = 4; break;}
                else if(value.equals("2") || value.equals("20") || value.equals("2000")) {row = 5; break;}
                System.out.println("I couldn't recognize that! Please try again!"); // Make user try again if it isn't a number it recognizes
            }
            else if(board.getMode().equals("normal")) {
                if(value.equals("2") || value.equals("200")) {row = 1; break;}
                else if(value.equals("4") || value.equals("400")) {row = 2; break;}
                else if(value.equals("6") || value.equals("600")) {row = 3; break;}
                else if(value.equals("8") || value.equals("800")) {row = 4; break;}
                else if(value.equals("1") || value.equals("10") || value.equals("1000")) {row = 5; break;}
                System.out.println("I couldn't recognize that! Please try again!"); // Make user try again if it isn't a number it recognizes
            }
        }
        logger.info("Chose row " + row);
        return row;
    }

    public static void printNormalBoard(Board board, int balance) {
        System.out.println("        A              B              C              D              E              F       "); // Print column headers
        System.out.println(board);
        System.out.println("Balance: $" + balance);
    }

    public static int wager(int balance) {
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
                System.out.println("You have $1000 to wager.");
                balance = 1000;
            }
        }
    }

    public static String parseAnswer(String guess) {
        guess = guess.toLowerCase().trim(); // Removes outer whitespace
        guess = Normalizer.normalize(guess, Normalizer.Form.NFD); // Removes all accents from both strings, replaces them with their normal counterparts
        guess = guess.replaceAll("[^\\p{ASCII}]", ""); // https://stackoverflow.com/a/3322174
        guess = guess.replaceAll("\"", ""); // Removes all quotations
        guess = guess.replaceAll("\\?", ""); // Removes all question marks
        guess = guess.replaceAll("<[^>]*>", ""); // https://stackoverflow.com/a/4075756 Removes all HTML formatting tags (ex. <i>, <b>)
        guess = guess.replaceAll(",", "");
        guess=  guess.replaceAll("\\.", "");
        guess = guess.replaceAll("&", "and");
        guess = guess.replaceAll("'", "");
        guess = guess.replaceAll("\\\\", "");
        guess = guess.replaceAll("\\*", "");
        guess = guess.trim();
        guess = removeString(guess, "the "); // Removes the articles the, an, a, and your from both guess and answer (possibly more in the future)
        guess = removeString(guess, "an ");
        guess = removeString(guess, "a ");
        guess = removeString(guess, "your ");
        guess = guess.trim();
        return guess;
    }

    public static boolean checkAnswer(String guess, String answer) {
        if(guess.length() == 0) {return false;} // If the guess is empty, it is automatically wrong (false)
        guess = parseAnswer(guess);
        answer = parseAnswer(answer);

        if(distance(guess, answer) <= 2) { // Uses Levenshtein distance with a threshold of 2 (translates to 2 typos between guess and answer)
            return true;
        }
        else { // If it doesn't pass the distance test,
            if(guess.contains(answer)) { // checks if the answer is in the user's guess (ex. Korea in Korean War)
                return true;
            }
            else {
                String var = "";
                String opposite = "";
                if(answer.contains("(") || guess.contains(")")) {
                    if (answer.contains("(")) {var = answer; opposite = guess;}
                    else {var = guess; opposite = answer;}
                    String pattern = "(\\(.*?\\))";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(var);
                    if (m.find()) {
                        String inside = m.group(1).replaceAll("[()]", "").trim();
                        String outside = var.replaceAll(m.group(1), "").replaceAll("[()]", "").trim();
                        if (Game.checkAnswer(opposite, inside) || Game.checkAnswer(opposite, outside)) {
                            return true;
                        }
                    }
                }

                if(answer.contains("/")) {
                    String first = answer.substring(0, answer.indexOf("/")).trim();
                    String last = answer.substring(answer.indexOf("/")).replaceAll("/", "").trim();
                    if(Game.checkAnswer(guess, first) || Game.checkAnswer(guess, last)) {
                        return true;
                    }
                }

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

    public static int finalJeopardy(int balance) throws IOException {
        int wager = wager(balance);
        System.out.println("For $" + wager + ":");

        ArrayList<Clue> finalClue = new ArrayList<>();
        boolean done = false;
        while(!done) {
            ArrayList<Clue> pool = Clue.getRandom(100);
            for (Clue clue : pool) {
                if (clue.getValue() == 0) {
                    if(!clue.getQuestion().trim().equals("")) {
                        if(!clue.getAnswer().trim().equals("")) {
                            finalClue.add(clue);
                            done = true;
                            break;
                        }
                    }
                }
            }
        }

        GridTable g = GridTable.of(1, 1); // Displays the category in a simple box
        g.put(0, 0, Collections.singleton(finalClue.get(0).getCategory().getTitle().toUpperCase()));
        g.apply(VERTICAL_CENTER).apply(HORIZONTAL_CENTER);
        g = Border.SINGLE_LINE.apply(g);
        Util.print(g);

        GridTable h = GridTable.of(1, 1); // Displays the question in a simple box
        h.put(0, 0, Collections.singleton(finalClue.get(0).getQuestion()));
        h.apply(VERTICAL_CENTER).apply(HORIZONTAL_CENTER);
        h = Border.SINGLE_LINE.apply(h);
        Util.print(h);

        int balanceBefore = balance;
        while (true) {
            balance = processGuess(finalClue.get(0), wager, balance);
            if(balance != -balanceBefore) {
                break;
            }
        }
        System.out.println("[Enter] ");
        scanner.nextLine();
        return balance;
    }
}
