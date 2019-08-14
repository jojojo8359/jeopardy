package main.java.com.jokeaton.jeopardy;

import com.inamik.text.tables.Cell;
import com.inamik.text.tables.GridTable;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static com.inamik.text.tables.Cell.Functions.HORIZONTAL_CENTER;
import static com.inamik.text.tables.Cell.Functions.VERTICAL_CENTER;

// TODO Documentation
public class Board {
    private static Logger logger = LogManager.getLogger(Board.class);

    private ArrayList<ArrayList<Space>> board = new ArrayList<ArrayList<Space>>(6);
    private Random random = new Random();
    private String mode;
    private int requests;

    public Board() {
        for(int i = 0; i < 6; i++) {
            this.board.add(new ArrayList<>());
        }
        logger.debug("Board init");
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

    public Category getCategory(int col) {
        return (Category) this.board.get(col).get(0);
    }

    public Clue getClue(int col, int row) {
        return (Clue) this.board.get(col).get(row);
    }

    public ArrayList<Space> getCol(int col) {
        return this.board.get(col);
    }

    public String getMode() {
        return this.mode;
    }

    public boolean isAnswered(int col, int row) {
        return this.getClue(col, row).isAnswered();
    }

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

    public boolean dailyDouble(int col, int row) {
        return this.getClue(col, row).isDailyDouble();
    }

    public void setDailyDouble(int col, int row, boolean state) {
        this.getClue(col, row).setDailyDouble(state);
    }

    public void genRandColValues(int col, int start, int end, int increment) throws IOException {
        logger.debug("Start gen col " + col);
        ArrayList<Clue> pool = new ArrayList<Clue>();
        Category finalcat = new Category(0, 0, "");
        logger.debug("  start cat search");
        while (pool.size() != 5) {
            Category category = Category.getCategories(1, this.random.nextInt(18320)).get(0); // Gets a random category to try
            this.requests++;
            logger.debug("   got cat#" + category.getId());
            finalcat = category;
//            System.out.print(category);
            for (int i = start; i <= end; i += increment) { // Searches for clues of values 100-500
                ArrayList<Clue> valuePool = Clue.getClues(i, category.getId(), 0); // Has to make a pool to choose from multiple clues of same value (i.e. 2 $200 clues)
                this.requests++;
                if (valuePool.size() == 0) { // If the search comes up empty (no clue with value in category), then throw the category out and try again
//                    System.out.println(" #" + category.getId() + " has no pool");
                    logger.debug("*  cat#" + category.getId() + " no clue pool $" + i + ", retrying");
                    pool.clear();
                    break;
                }
                boolean invalid = false;
                for (int j = 0; j < valuePool.size(); j++) { // Checks each clue in the pool for invalidity. If one clue is invalid, then throw out the whole pool and try a different category.
                    if (valuePool.get(j).getInvalidCount() > 0) {
//                        System.out.println(" #" + category.getId() + " is invalid");
                        logger.info("Clue #" + valuePool.get(j).getId() + " has invalid x" + valuePool.get(j).getInvalidCount());
                        valuePool.clear();
                        invalid = true;
                        break;
                    }
                }
                if(invalid) {break;}
                // If the program reaches here, then the selected clue is valid, and it is assumed that the category's clue pool is as well. However, not all the other clues may be valid.
//                if(i == 100) {
//                    System.out.println(" #" + category.getId() + " has a valid pool");
//                }
                pool.add(valuePool.get(this.random.nextInt(valuePool.size()))); // Adds a random clue from the value pool into the master pool
            }
        }
        logger.debug("! finished cat search col " + col + " #" + finalcat.getId());
        this.addSpace(col, finalcat); // Adds the category name to the board for display purposes
        StringBuilder ids = new StringBuilder();
        for(int i = 0; i < pool.size(); i++) {
            this.addSpace(col, pool.get(i)); // Adds each clue to the board, in value order (least to greatest)
            ids.append(" #").append(pool.get(i).getId());
        }
        logger.debug("! clues:" + ids);
    }

    public void genRandomBoard() throws IOException {
        this.mode = "normal";
        this.requests = 0;
        logger.debug("Generating board \"" + this.mode + "\"");
        long startTime = System.currentTimeMillis();
        progressPercentage(0, 6);
        for(int i = 0; i < 6; i++) {
            progressPercentage(i, 6);
            this.genRandColValues(i, 100, 500, 100);
        }
        progressPercentage(6, 6);
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        DecimalFormat df = new DecimalFormat("##.00");
        double time = elapsedTime / 1000.00;
        System.out.println(this.requests + " / " + df.format(time) + " sec.");
        logger.debug("Took " + df.format(time) + " sec.");
        this.genDailyDouble();
    }

    public void genDailyDouble() {
        int chance = getRandomNumberInRange(1, 10000);
        int col = 0;
        int row = 0;
        if(isBetween(chance, 0, 4))             {col = 0; row = 1;}
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
//        System.out.println("Daily double: " + col + ", " + row);
        logger.info("DD value " + chance + " loc " + col + ", " + row);
        this.setDailyDouble(col, row, true);
    }

    public static int getRandomNumberInRange(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower < x && x <= upper;
    }

    public String toString() {
        GridTable g = GridTable.of(6, 6);
        int catheight = 3;
        int dollarheight = 1;
        int globalWidth = 14;
        for(int i = 0; i < 6; i++) { // column
            for(int j = 0; j < 6; j++) { // row
                if(this.getSpace(i, j).getClass() == Category.class) {
                    String title = this.getCategory(i).getTitle().toUpperCase();
                    if(title.length() > globalWidth && title.contains(" ")) {
                        String[] sep = new String[2];
                        sep[0] = title.substring(0, title.lastIndexOf(" "));
                        sep[1] = title.substring(title.lastIndexOf(" "));
                        g.put(j, i, Cell.of(sep));
                    }
                    else {
                        g.put(j, i, Cell.of(title));
                    }
                }
                else {
                    if(this.getClue(i, j).isAnswered()) {
                        g.put(j, i, Cell.of(" "));
                    }
                    else {
                        g.put(j, i, Cell.of("$" + this.getClue(i, j).getValue()));
                    }
                }
            }
        }
        g.applyToRow(0, VERTICAL_CENTER.withHeight(catheight))
            .applyToRow(1, VERTICAL_CENTER.withHeight(dollarheight))
            .applyToRow(2, VERTICAL_CENTER.withHeight(dollarheight))
            .applyToRow(3, VERTICAL_CENTER.withHeight(dollarheight))
            .applyToRow(4, VERTICAL_CENTER.withHeight(dollarheight))
            .applyToRow(5, VERTICAL_CENTER.withHeight(dollarheight))
            .apply(HORIZONTAL_CENTER.withWidth(globalWidth));
        g = Border.DOUBLE_LINE.apply(g);
        Util.print(g);
        return "";
    }

    public static void progressPercentage(int remain, int total) { // https://stackoverflow.com/a/43381186
        if (remain > total) {
            throw new IllegalArgumentException();
        }
        int maxBareSize = 20; // 10unit for 100%
        int remainPercent = ((400 * remain) / total) / maxBareSize;
        char defaultChar = '-';
        String icon = "*";
        String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
        StringBuilder bareDone = new StringBuilder();
        bareDone.append("[");
        for (int i = 0; i < remainPercent; i++) {
            bareDone.append(icon);
        }
        String bareRemain = bare.substring(remainPercent, bare.length());
        System.out.print("\r" + bareDone + bareRemain + " " + remainPercent * 5 + "%");
        if (remain == total) {
            System.out.print(" Done! ");
        }
    }
}
