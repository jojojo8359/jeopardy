package main.java.com.jokeaton.jeopardy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Board {
    private ArrayList<ArrayList<Space>> board = new ArrayList<ArrayList<Space>>(6);
    private Random random = new Random();

    public Board() {
        for(int i = 0; i < 5; i++) {
            this.board.add(new ArrayList<>());
        }
    }

    public void setSpace(int col, int row, Space space) {
        this.board.get(col).set(row, space);
    }

    public void addSpace(int col, Space space) {
        this.board.get(col).add(space);
    }

    public Space getSpace(int col, int row) {
        return this.board.get(col).get(row);
    }

    public ArrayList<Space> getCol(int col) {
        return this.board.get(col);
    }

    public void genRandColValues(int col) throws IOException {
        ArrayList<Clue> pool = new ArrayList<Clue>();
        Category finalcat = new Category(0, 0, "");
        while (pool.size() != 5) {
            Category category = Category.getCategories(1, this.random.nextInt(18320)).get(0); // Gets a random category to try
            finalcat = category;
            System.out.print(category);
            for (int i = 100; i <= 500; i += 100) { // Searches for clues of values 100-500
                ArrayList<Clue> valuePool = Clue.getClues(i, category.getId(), 0); // Has to make a pool to choose from multiple clues of same value (i.e. 2 $200 clues)
                if (valuePool.size() == 0) { // If the search comes up empty (no clue with value in category), then throw the category out and try again
                    System.out.println(" #" + category.getId() + " has no pool");
                    pool.clear();
                    break;
                }
                boolean invalid = false;
                for (int j = 0; j < valuePool.size(); j++) { // Checks each clue in the pool for invalidity. If one clue is invalid, then throw out the whole pool and try a different category.
                    if (valuePool.get(j).getInvalidCount() > 0) {
                        System.out.println(" #" + category.getId() + " is invalid");
                        valuePool.clear();
                        invalid = true;
                        break;
                    }
                }
                if(invalid) {break;}
                // If the program reaches here, then the selected clue is valid, and it is assumed that the category's clue pool is as well. However, not all the other clues may be valid.
                if(i == 100) {
                    System.out.println(" #" + category.getId() + " has a valid pool");
                }
                System.out.println(valuePool);
//                System.out.println(valuePool.size());
                pool.add(valuePool.get(this.random.nextInt(valuePool.size()))); // Adds a random clue from the value pool into the master pool
            }
        }
        this.addSpace(col, finalcat); // Adds the category name to the board for display purposes
        for(int i = 0; i < pool.size(); i++) {
            this.addSpace(col, pool.get(i)); // Adds each clue to the board, in value order (least to greatest)
        }
        System.out.println(this.board);
    }

//    public String toString() {
//        String result = "";
////        result += "----------";
//        for(int i = 0; i < this.board.size(); i++) { // columns
//            for(int j = 0; j < 5; j++) { // rows
//                if(this.getSpace(i, j).getClass() == Category.class) {
//
//                }
//            }
//        }
//    }

    public String printCat(Category category) {
        String title = category.getTitle().toUpperCase();
        if(title.length() > 14) {
            for(int i = 13; i >= 0; i--) {
                if(title.charAt(i) == ' ') {
                    char[] chars = title.toCharArray();
                    chars[i] = '\n';
                    title = Arrays.toString(chars);
                }
            }
        }
        if(title.length() > 28) {
            for(int i = 27; i >= 13; i--) {
                if(title.charAt(i) == ' ') {
                    char[] chars = title.toCharArray();
                    chars[i] = '\n';
                    title = Arrays.toString(chars);
                }
            }
        }
        return title;
    }
}
