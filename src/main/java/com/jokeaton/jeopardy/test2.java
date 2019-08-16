package main.java.com.jokeaton.jeopardy;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class test2 {
    public static void main(String[] args) throws IOException {
//        String guess = "SAT";
//        String answer = "the SAT /Scholastic Aptitude Test";
//        System.out.println(Game.checkAnswer(guess, answer));
        int balance = 12000;
        balance = Game.finalJeopardy(balance);
        System.out.println("You completed the Final Jeopardy! clue!");
        System.out.println("Your final score was: $" + balance + "\n");
    }
}
