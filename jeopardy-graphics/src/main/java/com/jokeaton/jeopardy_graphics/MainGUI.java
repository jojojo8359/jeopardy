package main.java.com.jokeaton.jeopardy_graphics;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.security.Key;
import java.util.*;

public class MainGUI {
    public static Random random = new Random();
    public static int money = 0;
    public static Clue[][] clues = new Clue[6][5];
    public static Category[] categories = new Category[6];

    public static void main(String[] args) throws IOException, InterruptedException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory().setInitialTerminalSize(new TerminalSize(125, 50));
        terminalFactory.setTerminalEmulatorTitle("Jeopardy!");
        Screen screen = null;
        screen = terminalFactory.createScreen();
        screen.startScreen();
        MultiWindowTextGUI textGUI = createTextGUI(screen);
        String theme = "businessmachine";
        textGUI.setTheme(LanternaThemes.getRegisteredTheme(theme));
//        if (theme != null) {
//            textGUI.setTheme(LanternaThemes.getRegisteredTheme(theme));
//        }
        textGUI.setBlockingIO(false);
        textGUI.setEOFWhenNoWindows(true);
        textGUI.isEOFWhenNoWindows();   //No meaning, just to silence IntelliJ:s "is never used" alert

        try {
            mainWindow(textGUI);
            AsynchronousTextGUIThread guiThread = (AsynchronousTextGUIThread) textGUI.getGUIThread();
            guiThread.start();
            afterGUIThreadStarted(textGUI);
            guiThread.waitForStop();
        } finally {
            screen.stopScreen();
        }
    }

    public static void mainWindow(final MultiWindowTextGUI textGUI) {
        final BasicWindow modeSelectionWindow = new BasicWindow("Modes");
        ActionListBox modeSelector = new ActionListBox();
        modeSelector.addItem("Single Jeopardy!", new Runnable() {
            @Override
            public void run() {
                try {
                    boardWindow(textGUI, 0);
                } catch (IOException ignored) {}
            }
        });
        modeSelector.addItem("Double Jeopardy!", new Runnable() {
            @Override
            public void run() {

            }
        });
        modeSelector.addItem("Single and Double Jeopardy!", new Runnable() {
            @Override
            public void run() {

            }
        });
        modeSelector.addItem("Single, Double, and Final Jeopardy!", new Runnable() {
            @Override
            public void run() {

            }
        });
        modeSelector.addItem("Trivia Mode", new Runnable() {
            @Override
            public void run() {

            }
        });
        modeSelector.addItem("Exit", new Runnable() {
            @Override
            public void run() {
                modeSelectionWindow.close();
            }
        });

        modeSelectionWindow.setComponent(modeSelector);
        modeSelectionWindow.setHints(Collections.singletonList(Window.Hint.CENTERED));

        textGUI.addWindow(modeSelectionWindow);
    }

    public static void boardWindow(final MultiWindowTextGUI textGUI, int mode) throws IOException {
        progressWindow(textGUI, mode);

        final BasicWindow _boardWindow = new BasicWindow("Single Jeopardy!");
        _boardWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.EXPANDED));

        Panel boardPanel = new Panel();

        Table<String> table = generateTable();
//        Table<String> table = new Table<String>("Category 1", "Category 2", "Category 3", "Category 4", "Category 5");

//        table.getTableModel().addRow("$100", "$100", "$100", "$100", "$100");
//        table.getTableModel().addRow("$200", "$200", "$200", "$200", "$200");
//        table.getTableModel().addRow("$300", "$300", "$300", "$300", "$300");
//        table.getTableModel().addRow("$400", "$400", "$400", "$400", "$400");
//        table.getTableModel().addRow("$500", "$500", "$500", "$500", "$500");

        table.setCellSelection(true);

        table.setSelectAction(new Runnable() {
            @Override
            public void run() {
//                System.out.println(table.getSelectedColumn() + ", " + table.getSelectedRow());
                String guess = clueWindow(textGUI, "Clue goes here", table.getTableModel().getCell(table.getSelectedColumn(), table.getSelectedRow()));
                System.out.println(guess);
            }
        });

        table.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

        boardPanel.addComponent(table);
        boardPanel.addComponent(new EmptySpace());
        boardPanel.addComponent(new Label(Integer.toString(money)).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));
        boardPanel.addComponent(new EmptySpace());
        boardPanel.addComponent(new Button("Exit", new Runnable() {
            @Override
            public void run() {
                verifyExit(textGUI);
                _boardWindow.close();
            }
        }).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));
        _boardWindow.setComponent(boardPanel);
        textGUI.addWindow(_boardWindow);
    }

    public static boolean verifyExit(MultiWindowTextGUI textGUI) {
        BasicWindow verifyWindow = new BasicWindow();
        verifyWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel verifyPanel = new Panel(new GridLayout(3));

        Label message = new Label("Are you sure you want to exit?");
        message.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(3));
        verifyPanel.addComponent(message);

        final boolean[] result = {false};

        verifyPanel.addComponent(new EmptySpace());

        Button yes = new Button("Yes", new Runnable() {
            @Override
            public void run() {
                result[0] = true;
            }
        });
        yes.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
        verifyPanel.addComponent(yes);

        Button no = new Button("No", new Runnable() {
            @Override
            public void run() {
                result[0] = false;
            }
        });
        no.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
        verifyPanel.addComponent(no);

        verifyWindow.setComponent(verifyPanel);

        textGUI.addWindowAndWait(verifyWindow);
        return result[0];
    }

    public static Table<String> generateTable() {
        Table<String> table = new Table(categories[0].getTitle().toUpperCase());
        for(int i = 1; i < categories.length; i++) {
            table.getTableModel().addColumn(categories[i].getTitle().toUpperCase(), new String[1]);
        }

        for(int i = 0; i < clues[0].length; i++) {
            Collection<String> values = new ArrayList<>();
            for(int j = 0; j < clues.length; j++) {
                values.add("$" + clues[0][i].getValue());
            }
            table.getTableModel().addRow(values);
        }
        return table;
    }

    public static void progressWindow(MultiWindowTextGUI textGUI, int mode) throws IOException {
        final BasicWindow progressBarWindow = new BasicWindow();
        progressBarWindow.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel barPanel = new Panel(new GridLayout(2));

        ProgressBar bar = new ProgressBar(0, 6);
        bar.setValue(0);
        bar.setPreferredSize(new TerminalSize(50, 3));
        bar.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2));
        barPanel.addComponent(bar);

        barPanel.addComponent(new EmptySpace().setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));

        final Label requests = new Label("0");
        requests.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 2, 1));
        barPanel.addComponent(requests);

//        Button increase = new Button("+", new Runnable() {
//            @Override
//            public void run() {
//                if(bar.getValue() == 100) {
//                    progressBarWindow.close();
//                }
//                else if(bar.getValue() >= bar.getMin() && bar.getValue() < bar.getMax()) {
//                    bar.setValue(bar.getValue() + 5);
//                    requests.setText(Integer.toString(Integer.parseInt(requests.getText()) + 1));
//                }
//            }
//        });
//
//        Button decrease = new Button("-", new Runnable() {
//            @Override
//            public void run() {
//                if(bar.getValue() == 100) {
//                    progressBarWindow.close();
//                }
//                else if(bar.getValue() > bar.getMin() && bar.getValue() < bar.getMax()) {
//                    bar.setValue(bar.getValue() - 5);
//                    requests.setText(Integer.toString(Integer.parseInt(requests.getText()) + 1));
//                }
//            }
//        });
//
//        barPanel.addComponent(decrease);
//        barPanel.addComponent(increase);

        progressBarWindow.setComponent(barPanel);

        textGUI.addWindow(progressBarWindow);
        textGUI.updateScreen();

        int start = 500 * (mode + 1);
        int end = 100 * (mode + 1);

        for(int i = 0; i < clues.length; i++) {
            bar.setValue(i);
            textGUI.updateScreen();
            ArrayList<Space[]> returned = generateRandomColumn(start, end, end, requests, textGUI);
            clues[i] = (Clue[]) returned.get(1);
            categories[i] = ((Category[]) returned.get(0))[0];
        }
        bar.setValue(6);
        textGUI.updateScreen();
        System.out.println(Arrays.deepToString(clues));
        progressBarWindow.close();
    }

    public static String clueWindow(MultiWindowTextGUI textGUI, String clueText, String valueText) {
        final BasicWindow clueDisplayWindow = new BasicWindow("Clue");
        clueDisplayWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel cluePanel = new Panel(new GridLayout(2));

        Label value = new Label("For " + valueText + ":");
        value.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.CENTER,
                GridLayout.Alignment.CENTER,
                true,
                false,
                2,
                1
        ));
        cluePanel.addComponent(value);

        Label clue = new Label(clueText);
        clue.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.CENTER,
                GridLayout.Alignment.CENTER,
                true,
                false,
                2,
                1
        ));
        cluePanel.addComponent(clue);

        new Label("Who/what is ").addTo(cluePanel);

        final String[] guess = new String[1];

        TextBox input = new TextBox() {
            @Override
            public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
                if(keyStroke.getKeyType() == KeyType.Enter) {
                    guess[0] = this.getText();
                    clueDisplayWindow.close();
                }
                return super.handleKeyStroke(keyStroke);
            }
        };
        input.setPreferredSize(new TerminalSize(24, 1));

        cluePanel.addComponent(input);

        cluePanel.addComponent(new EmptySpace());

        Button submit = new Button("Submit", new Runnable() {
            @Override
            public void run() {
                guess[0] = input.getText();
                clueDisplayWindow.close();
            }
        });
        cluePanel.addComponent(submit);

        clueDisplayWindow.setComponent(cluePanel);
        textGUI.addWindowAndWait(clueDisplayWindow);

        return guess[0];
    }

    public static ArrayList<Space[]> generateRandomColumn(int start, int end, int increment, Label requests, MultiWindowTextGUI textGUI) throws IOException {
        ArrayList<Clue> pool = new ArrayList<Clue>(); // Master clue pool, will add and print these clues to the board
        Category finalcat = new Category(0, 0, ""); // Final category, will be added and printed to the board
//        logger.debug("  start cat search");
        while (pool.size() != 5) { // Looking for five clues in a category, main clue loop per column
            Category category = Category.getCategories(1, random.nextInt(18320)).get(0); // Gets a random category to try
            requests.setText(Integer.toString(Integer.parseInt(requests.getText()) + 1));
            textGUI.updateScreen();
//            logger.debug("   got cat#" + category.getId());
            finalcat = category;
            for (int i = start; i >= end; i -= increment) { // Searches for clues of values 100-500
                ArrayList<Clue> valuePool = Clue.getClues(i, category.getId(), 0); // Has to make a pool to choose from multiple clues of same value (i.e. 2 $200 clues)
//                this.requests++;
                if (valuePool.size() == 0) { // If the search comes up empty (no clue with value in category), then throw the category out and try again
//                    logger.debug("*  cat#" + category.getId() + " no clue pool $" + i + ", retrying");
                    pool.clear();
                    break;
                }
                boolean invalid = false;
                for (int j = 0; j < valuePool.size(); j++) { // Checks each clue in the pool for invalidity. If one clue is invalid, then throw out the whole pool and try a different category.
                    if (valuePool.get(j).getInvalidCount() > 0) {
//                        logger.info("Clue #" + valuePool.get(j).getId() + " has invalid x" + valuePool.get(j).getInvalidCount());
                        valuePool.clear();
                        invalid = true;
                        break;
                    }
                }
                if(invalid) {break;}
                // If the program reaches here, then the selected clue is valid, and it is assumed that the category's clue pool is as well. However, not all the other clues may be valid.
                pool.add(valuePool.get(random.nextInt(valuePool.size()))); // Adds a random clue from the value pool into the master pool
            }
        }

        Category[] cat = new Category[1];
        cat[0] = finalcat;

        Clue[] batch = new Clue[5];

        if(pool.size() == 5) {
            for(int i = pool.size() - 1; i >= 0; i--) {
                pool.get(i).setValue(pool.get(i).getValue() * 2);
                batch[4 - i] = pool.get(i);
            }
        }

        ArrayList<Space[]> result = new ArrayList<>();
        result.add(cat);
        result.add(batch);

        return result;
    }

    public static void afterGUIThreadStarted(MultiWindowTextGUI textGUI) {
        // By default do nothing
    }

    static protected MultiWindowTextGUI createTextGUI(Screen screen) {
        return new MultiWindowTextGUI(new SeparateTextGUIThread.Factory(), screen);
    }
}

