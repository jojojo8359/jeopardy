package main.java.com.jokeaton.jeopardy;

import java.io.IOException;
import java.text.ParseException;

public class test {
    public static void main(String[] args) throws IOException, ParseException {
//        System.out.println(Category.getCategory(11508));
//        ArrayList<ArrayList<Space>> board = new ArrayList<ArrayList<Space>>(5);
//        for(int i = 0; i < 5; i++) {
//            board.add(new ArrayList());
//        }
//        ArrayList<Category> rand = Category.getCategories(1, 0);
//        ArrayList<Clue> clues = Clue.getClues(0, rand.get(0).getId(), 0);
//        board.get(0).add(rand.get(0));
//        for(int i = 0; i < clues.size(); i++) {
//            board.get(0).add(clues.get(i));
//        }
//        System.out.println(board);

//        System.out.println(timeParser.convert("2009-07-22T12:00:00.000Z"));

        Board board = new Board();
        board.genRandColValues(0);
        Space cat;
        if(board.getSpace(0, 0).getClass() == Category.class) {
            cat = board.getSpace(0, 0);
            board.printCat((Category) cat);
        }
    }
}
