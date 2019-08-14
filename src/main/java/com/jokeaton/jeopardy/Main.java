package main.java.com.jokeaton.jeopardy;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        System.out.println("_________ _______  _______  _______  _______  _______  ______            _ ");
        System.out.println("\\__    _/(  ____ \\(  ___  )(  ____ )(  ___  )(  ____ )(  __  \\ |\\     /|( )");
        System.out.println("   )  (  | (    \\/| (   ) || (    )|| (   ) || (    )|| (  \\  )( \\   / )| |");
        System.out.println("   |  |  | (__    | |   | || (____)|| (___) || (____)|| |   ) | \\ (_) / | |");
        System.out.println("   |  |  |  __)   | |   | ||  _____)|  ___  ||     __)| |   | |  \\   /  | |");
        System.out.println("   |  |  | (      | |   | || (      | (   ) || (\\ (   | |   ) |   ) (   (_)");
        System.out.println("|\\_)  )  | (____/\\| (___) || )      | )   ( || ) \\ \\__| (__/  )   | |    _ ");
        System.out.println("(____/   (_______/(_______)|/       |/     \\||/   \\__/(______/    \\_/   (_)");
        logger.info("=======================");
        logger.info("=======================");
        logger.info("Start singleplayer game");
        Game.singlePlayer();
    }
}
