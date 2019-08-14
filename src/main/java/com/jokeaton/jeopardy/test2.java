package main.java.com.jokeaton.jeopardy;

import com.inamik.text.tables.*;
import com.inamik.text.tables.grid.Border;
import com.inamik.text.tables.grid.Util;

import static com.inamik.text.tables.Cell.Functions.*;

public class test2 {
    public static void main(String[] args) {
        System.out.println("\n\n");
        int catheight = 3;
        int dollarheight = 1;
        int width = 10;
        GridTable g = GridTable.of(6, 6)
                .put(0, 0, Cell.of("CATEGORY"))
                .put(0, 1, Cell.of("CATEGORY"))
                .put(0, 2, Cell.of("CATEGORY"))
                .put(0, 3, Cell.of("CATEGORY"))
                .put(0, 4, Cell.of("CATEGORY"))
                .put(0, 5, Cell.of("CATEGORY"))

                .put(1, 0, Cell.of("$100"))
                .put(1, 1, Cell.of("$100"))
                .put(1, 2, Cell.of("$100"))
                .put(1, 3, Cell.of("$100"))
                .put(1, 4, Cell.of("$100"))
                .put(1, 5, Cell.of("$100"))

                .put(2, 0, Cell.of("$200"))
                .put(2, 1, Cell.of("$200"))
                .put(2, 2, Cell.of("$200"))
                .put(2, 3, Cell.of("$200"))
                .put(2, 4, Cell.of("$200"))
                .put(2, 5, Cell.of("$200"))

                .put(3, 0, Cell.of("$300"))
                .put(3, 1, Cell.of("$300"))
                .put(3, 2, Cell.of("$300"))
                .put(3, 3, Cell.of("$300"))
                .put(3, 4, Cell.of("$300"))
                .put(3, 5, Cell.of("$300"))

                .put(4, 0, Cell.of("$400"))
                .put(4, 1, Cell.of("$400"))
                .put(4, 2, Cell.of("$400"))
                .put(4, 3, Cell.of("$400"))
                .put(4, 4, Cell.of("$400"))
                .put(4, 5, Cell.of("$400"))

                .put(5, 0, Cell.of("$500"))
                .put(5, 1, Cell.of("$500"))
                .put(5, 2, Cell.of("$500"))
                .put(5, 3, Cell.of("$500"))
                .put(5, 4, Cell.of("$500"))
                .put(5, 5, Cell.of("$500"))

                .applyToRow(0, VERTICAL_CENTER.withHeight(catheight))
                .applyToRow(1, VERTICAL_CENTER.withHeight(dollarheight))
                .applyToRow(2, VERTICAL_CENTER.withHeight(dollarheight))
                .applyToRow(3, VERTICAL_CENTER.withHeight(dollarheight))
                .applyToRow(4, VERTICAL_CENTER.withHeight(dollarheight))
                .applyToRow(5, VERTICAL_CENTER.withHeight(dollarheight))

                .apply(HORIZONTAL_CENTER)
                ;
        g = Border.DOUBLE_LINE.apply(g);
        Util.print(g);
    }
}
