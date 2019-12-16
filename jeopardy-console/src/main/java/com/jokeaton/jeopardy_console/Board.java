package main.java.com.jokeaton.jeopardy_console;

import com.inamik.text.tables.Cell;
import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.VERTICAL_CENTER;

/**
 * Class for the game's board
 * @author Joel Keaton
 * @version 1.0.1
 */
public class Board {
    private static Logger logger = LogManager.getLogger(Board.class);

    private ArrayList<ArrayList<Space>> board = new ArrayList<ArrayList<Space>>(6); // The board arraylist has a nested arraylist with Space objects, since I wanted both the Clue and Category classes (which inherit Space) to be printed to the board for simplified code and access
    private Random random = new Random();
    private String mode;
    private int requests;

    /**
     * Constructor for a board. Initializes the boards rows.
     */
    public Board() {
        for(int i = 0; i < 6; i++) {
            this.board.add(new ArrayList<>()); // Adds the six rows to the board
        }
        logger.debug("Board init");
    }

    /**
     * Setter method for a Space object on the board.
     * @param col the column to change the Space of
     * @param row the row to change the Space of
     * @param space the Space to replace the old one
     */
    public void setSpace(int col, int row, Space space) {
        this.board.get(col).set(row, space);
    }

    /**
     * Adds a Space object to the bottom of a column
     * @param col the column to add the Space to
     * @param space the Space object to add to the column
     */
    public void addSpace(int col, Space space) {
        this.board.get(col).add(space);
    }

    /**
     * Gets a Space object from the board at a given row and column
     * @param col the column to get the Space at
     * @param row the row to get the Space at
     * @return the Space object
     */
    public Space getSpace(int col, int row) {
        return this.board.get(col).get(row);
    }

    /**
     * Gets a Category object from the board at a given column (since it will always be the top one)
     * @param col the column to get the Category object at
     * @return the Category object
     */
    public Category getCategory(int col) {
        return (Category) this.board.get(col).get(0);
    }

    /**
     * Gets a Clue object from the board at a given column and row
     * @param col the column to get the Clue object at
     * @param row the row to get the Clue object at
     * @return the Clue object
     */
    public Clue getClue(int col, int row) {
        return (Clue) this.board.get(col).get(row);
    }

    /**
     * Gets all the Spaces in a specific column
     * @param col the column to retrieve
     * @return the column as an ArrayList of Space Objects
     */
    public ArrayList<Space> getCol(int col) {
        return this.board.get(col);
    }

    /**
     * Gets the board's mode
     * @return the board's mode
     */
    public String getMode() {
        return this.mode;
    }

    /**
     * Returns if a Clue is answered
     * @param col the column of the Clue to query
     * @param row the row of the Clue to query (MUST NOT BE 0)
     * @return the answered status of the Clue
     */
    public boolean isAnswered(int col, int row) {
        return this.getClue(col, row).isAnswered();
    }

    /**
     * Returns if the board has no unanswered Clues left
     * @return if the board is cleared
     */
    public boolean boardAnswered() {
        for(int i = 0; i < 6; i++) {
            for(int j = 1; j < 6; j++) {
                if(!this.getClue(i, j).isAnswered()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns if a Clue is a Daily Double spot
     * @param col the column to query
     * @param row the row to query
     * @return the Clue's Daily Double status
     */
    public boolean dailyDouble(int col, int row) {
        return this.getClue(col, row).isDailyDouble();
    }

    /**
     * Sets a Clue's Daily Double status
     * @param col the column of the Clue
     * @param row the row of the Clue
     * @param state the status to set the Clue to
     */
    public void setDailyDouble(int col, int row, boolean state) {
        this.getClue(col, row).setDailyDouble(state);
    }

    /**
     * Generates random clues based on a random category in order to fill in a certain column
     * @param col the column to randomly generate
     * @param start the starting value to search from
     * @param end the value to end the search at
     * @param increment the increment to search by (100 would be 100, 200, 300, etc.)
     * @throws IOException caused by get method
     */
    public void genRandColValues(int col, int start, int end, int increment) throws IOException {
        logger.debug("Start gen col " + col);
        ArrayList<Clue> pool = new ArrayList<Clue>(); // Master clue pool, will add and print these clues to the board
        Category finalcat = new Category(0, 0, ""); // Final category, will be added and printed to the board
        logger.debug("  start cat search");
        while (pool.size() != 5) { // Looking for five clues in a category, main clue loop per column
            Category category = Category.getCategories(1, this.random.nextInt(18320)).get(0); // Gets a random category to try
            this.requests++;
            logger.debug("   got cat#" + category.getId());
            finalcat = category;
            for (int i = start; i >= end; i -= increment) { // Searches for clues of values 100-500
                ArrayList<Clue> valuePool = Clue.getClues(i, category.getId(), 0); // Has to make a pool to choose from multiple clues of same value (i.e. 2 $200 clues)
                this.requests++;
                if (valuePool.size() == 0) { // If the search comes up empty (no clue with value in category), then throw the category out and try again
                    logger.debug("*  cat#" + category.getId() + " no clue pool $" + i + ", retrying");
                    pool.clear();
                    break;
                }
                boolean invalid = false;
                for (int j = 0; j < valuePool.size(); j++) { // Checks each clue in the pool for invalidity. If one clue is invalid, then throw out the whole pool and try a different category.
                    if (valuePool.get(j).getInvalidCount() > 0) {
                        logger.info("Clue #" + valuePool.get(j).getId() + " has invalid x" + valuePool.get(j).getInvalidCount());
                        valuePool.clear();
                        invalid = true;
                        break;
                    }
                }
                if(invalid) {break;}
                // If the program reaches here, then the selected clue is valid, and it is assumed that the category's clue pool is as well. However, not all the other clues may be valid.
                pool.add(valuePool.get(this.random.nextInt(valuePool.size()))); // Adds a random clue from the value pool into the master pool
            }
        }
        logger.debug("! finished cat search col " + col + " #" + finalcat.getId());
        this.addSpace(col, finalcat); // Adds the category name to the board for display purposes
        StringBuilder ids = new StringBuilder();
        for (Clue clue : pool) {
            clue.setValue(clue.getValue() * 2);
        }
        for(int i = pool.size() - 1; i >= 0; i--) {
            this.addSpace(col, pool.get(i)); // Adds each clue to the board, in value order (least to greatest)
            ids.append(" #").append(pool.get(i).getId());
        }
        logger.debug("! clues:" + ids);
    }

    /**
     * Generates a full board given a mode
     * @param mode the board's creation mode, 0 is for single jeopardy, 1 is for double jeopardy
     * @throws IOException caused by get method
     */
    public void genRandomBoard(int mode) throws IOException {
        if(mode == 0) {
            this.mode = "normal"; // This means that the board will generate values from 100-500
        }
        else if(mode == 1) {
            this.mode = "double";
        }
        this.requests = 0;
        logger.debug("Generating board \"" + this.mode + "\"");
        System.out.println("Generating a " + this.mode + " board...\n");
        long startTime = System.currentTimeMillis(); // Start timer for progress bar
        progressPercentage(0, 6); // Starts displaying progress bar
        if(mode == 0) {
            for (int i = 0; i < 6; i++) {
                progressPercentage(i, 6);
                this.genRandColValues(i, 500, 100, 100); // Switched to reverse generation for faster loading times (especially for double mode)
            }
        }
        else {
            for (int i = 0; i < 6; i++) {
                progressPercentage(i, 6);
                this.genRandColValues(i, 1000, 200, 200);
            }
        }
        progressPercentage(6, 6); // Finish progress bar
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        DecimalFormat df = new DecimalFormat("##.00");
        double time = elapsedTime / 1000.00;
        System.out.println(this.requests + " / " + df.format(time) + " sec."); // Display requests made / time taken to finish board
        logger.debug("Took " + df.format(time) + " sec.");
        this.genDailyDouble(); // Makes a random clue on the board a Daily Double
    }

    /**
     * Generates a random spot on the board to place a daily double space.
     */
    public void genDailyDouble() {
        int chance = getRandomNumberInRange(1, 10000); // Generates number from 1-10000 (represents 0.00%-100.00%) http://digg.com/2018/joepardy-daily-double-probability-mapped
        int col = 0;
        int row = 0;
        if(isBetween(chance, 1, 4))             {col = 0; row = 1;} // Maps each percentage to a row and column location (ex. 4 = 0.04%)
        else if(isBetween(chance, 4, 7))        {col = 1; row = 1;}
        else if(isBetween(chance, 7, 11))       {col = 2; row = 1;}
        else if(isBetween(chance, 11, 14))      {col = 3; row = 1;}
        else if(isBetween(chance, 14, 17))      {col = 4; row = 1;}
        else if(isBetween(chance, 17, 20))      {col = 5; row = 1;}
        else if(isBetween(chance, 20, 243))     {col = 0; row = 2;}
        else if(isBetween(chance, 243, 367))    {col = 1; row = 2;}
        else if(isBetween(chance, 367, 547))    {col = 2; row = 2;}
        else if(isBetween(chance, 547, 706))    {col = 3; row = 2;}
        else if(isBetween(chance, 706, 883))    {col = 4; row = 2;}
        else if(isBetween(chance, 883, 1009))   {col = 5; row = 2;}
        else if(isBetween(chance, 1009, 1615))  {col = 0; row = 3;}
        else if(isBetween(chance, 1615, 1992))  {col = 1; row = 3;}
        else if(isBetween(chance, 1992, 2514))  {col = 2; row = 3;}
        else if(isBetween(chance, 2514, 3015))  {col = 3; row = 3;}
        else if(isBetween(chance, 3015, 3504))  {col = 4; row = 3;}
        else if(isBetween(chance, 3504, 3869))  {col = 5; row = 3;}
        else if(isBetween(chance, 3869, 4640))  {col = 0; row = 4;}
        else if(isBetween(chance, 4640, 5149))  {col = 1; row = 4;}
        else if(isBetween(chance, 5149, 5875))  {col = 2; row = 4;}
        else if(isBetween(chance, 5875, 6523))  {col = 3; row = 4;}
        else if(isBetween(chance, 6523, 7218))  {col = 4; row = 4;}
        else if(isBetween(chance, 7218, 7693))  {col = 5; row = 4;}
        else if(isBetween(chance, 7693, 8165))  {col = 0; row = 5;}
        else if(isBetween(chance, 8165, 8434))  {col = 1; row = 5;}
        else if(isBetween(chance, 8434, 8869))  {col = 2; row = 5;}
        else if(isBetween(chance, 8869, 9290))  {col = 3; row = 5;}
        else if(isBetween(chance, 9290, 9683))  {col = 4; row = 5;}
        else if(isBetween(chance, 9683, 10003)) {col = 5; row = 5;}
        logger.info("DD value " + chance + " loc " + col + ", " + row);
        this.setDailyDouble(col, row, true); // Sets location as Daily Double spot
    }

    /**
     * Returns a random number between the minimum and maximum values given
     * @param min the minimum value
     * @param max the maximum value
     * @return a random number between the min and max
     */
    public static int getRandomNumberInRange(int min, int max) { // https://www.mkyong.com/java/java-generate-random-integers-in-a-range/
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    /**
     * Helper method to tell if a given is greater than a number and less than or equal to another one
     * @param x the given number
     * @param lower the lower bound
     * @param upper the upper bound
     * @return true if x is between lower and upper, false if not
     */
    public static boolean isBetween(int x, int lower, int upper) {
        return lower < x && x <= upper;
    } // Used in genDailyDouble()

    /**
     * Prints the board table to the screen
     * @return the board as a String
     */
    public String toString() {
        GridTable g = GridTable.of(6, 6); // Initializes table with 6 rows and 6 columns
        int catheight = 3;
        int dollarheight = 1;
        int globalWidth = 14;
        for(int i = 0; i < 6; i++) { // column
            for(int j = 0; j < 6; j++) { // row
                if(this.getSpace(i, j).getClass() == Category.class) { // If the chosen cell is a category space, print its title
                    String title = this.getCategory(i).getTitle().toUpperCase();
                    if(title.length() > globalWidth && title.contains(" ")) { // Wraps category text if necessary
                        String[] sep = new String[2];
                        String split = title.substring(0, globalWidth);
                        int index = split.indexOf(" ");
                        sep[0] = title.substring(0, index).trim();
                        sep[1] = title.substring(index).trim();
                        g.put(j, i, Cell.of(sep));
                    }
                    else {
                        g.put(j, i, Cell.of(title));
                    }
                }
                else { // If the chosen cell is a clue space, print its money value
                    if(this.getClue(i, j).isAnswered()) { // Also account for if it is answered (blank)
                        g.put(j, i, Cell.of(" "));
                    }
                    else {
                        g.put(j, i, Cell.of("$" + this.getClue(i, j).getValue()));
                    }
                }
            }
        }
        g.applyToRow(0, VERTICAL_CENTER.withHeight(catheight)) // Align everything to the right dimensions and center it all
            .applyToRow(1, VERTICAL_CENTER.withHeight(dollarheight))
            .applyToRow(2, VERTICAL_CENTER.withHeight(dollarheight))
            .applyToRow(3, VERTICAL_CENTER.withHeight(dollarheight))
            .applyToRow(4, VERTICAL_CENTER.withHeight(dollarheight))
            .applyToRow(5, VERTICAL_CENTER.withHeight(dollarheight))
            .apply(HORIZONTAL_CENTER.withWidth(globalWidth));
        g = Border.DOUBLE_LINE.apply(g); // Apply fancy double border effect
        Util.print(g); // Print the table
        return ""; // Since this is the built in toString() method, this will always print an extra newline at the end :/
    }

    /**
     * Print and update a progress bar to display the creation progress of the board
     * @param remain part
     * @param total whole
     */
    public static void progressPercentage(int remain, int total) { // https://stackoverflow.com/a/43381186
        if (remain > total) {
            throw new IllegalArgumentException();
        }
        int maxBareSize = 20; // 10 units for 100%, 20 for 200%, etc.
        int remainPercent = ((400 * remain) / total) / maxBareSize; // First number is 100 for 10 unit bar, 400 for 20 unit bar, etc.
        char defaultChar = '-';
        String icon = "*";
        String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
        StringBuilder bareDone = new StringBuilder();
        bareDone.append("[");
        for (int i = 0; i < remainPercent; i++) {
            bareDone.append(icon);
        }
        String bareRemain = bare.substring(remainPercent);
        System.out.print("\r" + bareDone + bareRemain + " " + remainPercent * 5 + "%"); // Number is 10 for 10 unit, 5 for 20 unit, etc.
        if (remain == total) {
            System.out.print(" Done! ");
        }
    }
}
