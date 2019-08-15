package main.java.com.jokeaton.jeopardy;

public class test2 {
    public static void main(String[] args) {
        String guess = "asdf?";
        String answer = "<i>asdf</i>";
        System.out.println(Game.checkAnswer(guess, answer));
    }
}
