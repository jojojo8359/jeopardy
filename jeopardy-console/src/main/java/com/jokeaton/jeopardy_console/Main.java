package main.java.com.jokeaton.jeopardy_console;

import java.io.IOException;
import java.util.Scanner;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Main game class for mode selection through a menu
 * @author Joel Keaton
 * @version 1.0.1
 */

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Main game loop
     * @param args command line arguments, not used
     * @throws IOException caused by get method
     */
    public static void main(String[] args) throws IOException {
        System.out.println("_________ _______  _______  _______  _______  _______  ______            _ ");
        System.out.println("\\__    _/(  ____ \\(  ___  )(  ____ )(  ___  )(  ____ )(  __  \\ |\\     /|( )");
        System.out.println("   )  (  | (    \\/| (   ) || (    )|| (   ) || (    )|| (  \\  )( \\   / )| |");
        System.out.println("   |  |  | (__    | |   | || (____)|| (___) || (____)|| |   ) | \\ (_) / | |");
        System.out.println("   |  |  |  __)   | |   | ||  _____)|  ___  ||     __)| |   | |  \\   /  | |");
        System.out.println("   |  |  | (      | |   | || (      | (   ) || (\\ (   | |   ) |   ) (   (_)");
        System.out.println("|\\_)  )  | (____/\\| (___) || )      | )   ( || ) \\ \\__| (__/  )   | |    _ ");
        System.out.println("(____/   (_______/(_______)|/       |/     \\||/   \\__/(______/    \\_/   (_)");
        System.out.println();

        while(true) { // Main loop for mode selection
            System.out.println("Choose a mode: ");
            System.out.println("1. Singleplayer"); // Planning to eventually add multiplayer...
            System.out.println("2. Exit\n");
            System.out.print("> ");
            String mode = scanner.nextLine().trim();
            if(mode.length() == 1 && (int) mode.charAt(0) == 49) {
                System.out.println();
                while(true) {
                    System.out.println("Choose a singleplayer mode: \n" +
                            "1. Single Jeopardy! board (200-1000)\n" +
                            "2. Double Jeopardy! board (400-2000)\n" +
                            "3. Single and Double Jeopardy! boards\n" +
                            "4. Single, Double, and Final Jeopardy! boards (full game)\n" +
                            "5. Trivia Mode (nonstop questions)\n" +
                            "6. Back");
                    System.out.print("> ");
                    String singlePlayerMode = scanner.nextLine();
                    if(singlePlayerMode.length() == 1) {
                        int code = singlePlayerMode.charAt(0);
                        int balance = 0;
                        if(code == 49) {
                            balance = Game.singlePlayer(0, balance); // Mode 0 is for single jeopardy, while 1 is for double jeopardy; this is to tell the board to generate normal or double values
                            System.out.println("You cleared the Single Jeopardy! board!");
                            System.out.println("Your final score was: $" + balance + "\n");
                            break;
                        }
                        else if(code == 50) {
                            balance = Game.singlePlayer(1, balance);
                            System.out.println("You cleared the Double Jeopardy! board!");
                            System.out.println("Your final score was: $" + balance + "\n");
                            break;
                        }
                        else if(code == 51) {
                            balance = Game.singlePlayer(0, balance);
                            System.out.println("You cleared the Single Jeopardy! board!");
                            System.out.println("Your score is: $" + balance + "\n");
                            balance = Game.singlePlayer(1, balance);
                            System.out.println("You cleared the Double Jeopardy! board!");
                            System.out.println("Your final score was: $" + balance + "\n");
                            break;
                        }
                        else if(code == 52) {
                            balance = Game.singlePlayer(0, balance);
                            System.out.println("You cleared the Single Jeopardy! board!");
                            System.out.println("Your score is: $" + balance + "\n");
                            balance = Game.singlePlayer(1, balance);
                            System.out.println("You cleared the Double Jeopardy! board!");
                            System.out.println("Your score is: $" + balance + "\n");
                            balance = Game.finalJeopardy(balance);
                            System.out.println("You completed the Final Jeopardy! clue!");
                            System.out.println("Your final score was: $" + balance + "\n");
                            break;
                        }
                        else if(code == 53) {
                            balance = Game.triviaMode();
                            System.out.println("Your final score was: $" + balance + "\n");
                            break;
                        }
                        else if(code == 54) {
                            break;
                        }
                    }
                    else {
                        System.out.println("Please try again.\n");
                    }
                }
            }
            else if(mode.length() == 1 && (int) mode.charAt(0) == 50) {
                break;
            }
            else {
                System.out.println("Please try again.\n");
            }
        }
    }
}
