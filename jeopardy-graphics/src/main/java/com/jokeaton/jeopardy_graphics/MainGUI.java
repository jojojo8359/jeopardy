package main.java.com.jokeaton.jeopardy_graphics;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainGUI {
    public static Random random = new Random();
    public static int money = 0;
    public static Clue[][] clues = new Clue[6][5];
    public static Category[] categories = new Category[6];
    public static boolean exitStatus = false;
    public static boolean close = false;
    public static int retry = 0;
    public static boolean right = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory().setInitialTerminalSize(new TerminalSize(125, 40));
        terminalFactory.setTerminalEmulatorTitle("Jeopardy!");
        Screen screen;
        screen = terminalFactory.createScreen();
        screen.startScreen();
        MultiWindowTextGUI textGUI = createTextGUI(screen);
        String theme = "businessmachine";
        LanternaThemes.registerTheme("asdf", new PropertyTheme(Objects.requireNonNull(loadPropTheme("asdf-theme.properties")), false));
        textGUI.setTheme(LanternaThemes.getRegisteredTheme("asdf"));
//        textGUI.setTheme(LanternaThemes.getRegisteredTheme(theme));
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
                money = 0;
                try {
                    boardWindow(textGUI, 0);
                    if(!exitStatus) {
                        exitStatus = false;
                        // Next step
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        modeSelector.addItem("Double Jeopardy!", new Runnable() {
            @Override
            public void run() {
                money = 0;
                try {
                    boardWindow(textGUI, 1);
                    if(!exitStatus) {
                        exitStatus = false;
                        // Next step
                    }
                } catch(IOException ignored) {}
            }
        });
        modeSelector.addItem("Single and Double Jeopardy!", new Runnable() {
            @Override
            public void run() {
                money = 0;
                try {
                    boardWindow(textGUI, 0);
                    if(!exitStatus) {
                        exitStatus = false;
                        boardWindow(textGUI, 1);
                        exitStatus = false;
                    }
                } catch(IOException ignored) {}
            }
        });
        modeSelector.addItem("Single, Double, and Final Jeopardy!", new Runnable() {
            @Override
            public void run() {
                money = 0;
            }
        });
        modeSelector.addItem("Trivia Mode", new Runnable() {
            @Override
            public void run() {
                money = 0;
            }
        });
        modeSelector.addItem("Change theme", new Runnable() {
            @Override
            public void run() {
                themeWindow(textGUI);
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

    public static void themeWindow(MultiWindowTextGUI textGUI) {
        final BasicWindow themeGui = new BasicWindow("Change theme");
        themeGui.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel themePanel = new Panel(new GridLayout(2));


    }

    public static void boardWindow(MultiWindowTextGUI textGUI, int mode) throws IOException {
        String title = "";
        switch(mode) {
            case 0:
                title = "Single Jeopardy!";
                break;
            case 1:
                title = "Double Jeopardy!";
                break;
        }

        progressWindow(textGUI, mode);
        final BasicWindow _boardWindow = new BasicWindow(title);
        _boardWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.EXPANDED));
        if(retry == 2) {
            exitStatus = true;
            _boardWindow.close();
        }
        else if(retry == 1) {
            while(retry == 1) {
                progressWindow(textGUI, mode);
            }
        }
        else {
            int[] dd = genDailyDouble();
            clues[dd[0]][dd[1]].setDailyDouble(true);

            Panel boardPanel = new Panel();

            Table<String> table = new Table(categories[0].getTitle().toUpperCase()) {
                @Override
                public Result handleKeyStroke(KeyStroke keyStroke) {
                    if (keyStroke.getKeyType() == KeyType.Escape) {
                        verifyExit(textGUI);
                        if (exitStatus) {
                            _boardWindow.close();
                        }
                        return Result.HANDLED;
                    }
                    return super.handleKeyStroke(keyStroke);
                }
            };
            for (int i = 1; i < categories.length; i++) {
                table.getTableModel().addColumn(categories[i].getTitle().toUpperCase(), new String[1]);
            }

            for (int i = 0; i < clues[0].length; i++) {
                Collection<String> values = new ArrayList<>();
                for (int j = 0; j < clues.length; j++) {
                    values.add("$" + clues[0][i].getValue());
                }
                table.getTableModel().addRow(values);
            }

            table.setCellSelection(true);

            Label moneyLabel = new Label("$" + money).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

            table.setSelectAction(new Runnable() {
                @Override
                public void run() {
                    String guess;
                    if (!clues[table.getSelectedColumn()][table.getSelectedRow()].isAnswered()) {
                        int value = 0;
                        if(clues[table.getSelectedColumn()][table.getSelectedRow()].isDailyDouble()) {
                            value = wagerWindow(textGUI);
                        }
                        else value = Integer.parseInt(table.getTableModel().getCell(table.getSelectedColumn(), table.getSelectedRow()).replace("$", ""));
                        guess = clueWindow(textGUI, categories[table.getSelectedColumn()].getTitle().toUpperCase(), clues[table.getSelectedColumn()][table.getSelectedRow()].getQuestion(), value);
                        if (checkAnswer(guess, clues[table.getSelectedColumn()][table.getSelectedRow()].getAnswer())) {
                            correctWindow(textGUI);
                            money += value;
                        } else {
                            if (close) {
                                closeWindow(textGUI, guess, parseAnswer(clues[table.getSelectedColumn()][table.getSelectedRow()].getAnswer()));
                                if(right) {
                                    money += value;
                                }
                                else money -= value;
                            } else {
                                wrongWindow(textGUI, parseAnswer(clues[table.getSelectedColumn()][table.getSelectedRow()].getAnswer()));
                                money -= value;
                            }
                        }
                        close = false;
                        clues[table.getSelectedColumn()][table.getSelectedRow()].setAnswered(true);
                        table.getTableModel().setCell(table.getSelectedColumn(), table.getSelectedRow(), "");
                        moneyLabel.setText("$" + money);
                        if (boardDone()) {
                            resultWindow(textGUI);
                            _boardWindow.close();
                        }
                    }
                }
            });

            table.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center));

            boardPanel.addComponent(table);
            boardPanel.addComponent(new EmptySpace());
            boardPanel.addComponent(moneyLabel);
            boardPanel.addComponent(new EmptySpace());
            boardPanel.addComponent(new Button("Exit", new Runnable() {
                @Override
                public void run() {
                    verifyExit(textGUI);
                    if (exitStatus) {
                        _boardWindow.close();
                    }
                }
            }).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));
            _boardWindow.setComponent(boardPanel);
            textGUI.addWindowAndWait(_boardWindow);
        }
    }

    public static int wagerWindow(MultiWindowTextGUI textGUI) {
        final BasicWindow wagerGui = new BasicWindow("Daily Double Found!");
        wagerGui.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel wagerPanel = new Panel(new GridLayout(2));

        wagerPanel.addComponent(new Label("You have found a Daily Double!").setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 2, 1)));

        String label;
        if(money > 0) {
            label = "Enter your wager: (0-" + money + ")";
        }
        else {
            label = "Enter your wager: (0-1000)";
        }
        wagerPanel.addComponent(new Label(label).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 2, 1)));

        final int[] wager = new int[1];

        TextBox input = new TextBox() {
            @Override
            public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
                if(keyStroke.getKeyType() == KeyType.Enter) {
                    wager[0] = Integer.parseInt(this.getText());
                    wagerGui.close();
                    return Result.HANDLED;
                }
                return super.handleKeyStroke(keyStroke);
            }
        }.setValidationPattern(Pattern.compile("[0-9]*")).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 2, 1));
        input.setPreferredSize(new TerminalSize(6, 1));
        wagerPanel.addComponent(input);

        Button submit = new Button("Submit", new Runnable() {
            @Override
            public void run() {
                wager[0] = Integer.parseInt(input.getText());
                wagerGui.close();
            }
        }).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 2, 1));
        wagerPanel.addComponent(submit);

        wagerGui.setComponent(wagerPanel);
        textGUI.addWindowAndWait(wagerGui);

        return wager[0];
    }

    public static void resultWindow(MultiWindowTextGUI textGUI) {
        final BasicWindow resultGui = new BasicWindow("Results");
        resultGui.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel resultPanel = new Panel(new GridLayout(2));

        resultPanel.addComponent(new Label("You finished the board with $" + money + "!").setLayoutData(GridLayout.createHorizontallyFilledLayoutData(2)));
        resultPanel.addComponent(new EmptySpace());
        resultPanel.addComponent(new Button("OK", new Runnable() {
            @Override
            public void run() {
                resultGui.close();
            }
        }));

        resultGui.setComponent(resultPanel);

        textGUI.addWindowAndWait(resultGui);
    }

    public static boolean boardDone() {
        for(int i = 0; i < clues.length; i++) {
            for(int j = 0; j < clues[0].length; j++) {
                if(!clues[i][j].isAnswered()) return false;
            }
        }
        return true;
    }

    public static void correctWindow(MultiWindowTextGUI textGUI) {
        final BasicWindow correctGui = new BasicWindow("");
        correctGui.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel correctPanel = new Panel(new GridLayout(2));

        correctPanel.addComponent(new Label("Correct!").setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 2, 1)));
        correctPanel.addComponent(new EmptySpace());
        correctPanel.addComponent(new Button("OK", new Runnable() {
            @Override
            public void run() {
                correctGui.close();
            }
        }));

        correctGui.setComponent(correctPanel);

        textGUI.addWindowAndWait(correctGui);
    }

    public static void closeWindow(MultiWindowTextGUI textGUI, String guess, String answer) {
        right = false;
        final BasicWindow closeGui = new BasicWindow("");
        closeGui.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel closePanel = new Panel(new GridLayout(2));

        closePanel.addComponent(new Label("Close!").setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 2, 1)));
        closePanel.addComponent(new Label("Your answer:").setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1)));
        closePanel.addComponent(new Label("Correct answer:").setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1)));
        closePanel.addComponent(new Label(guess).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1)));
        closePanel.addComponent(new Label(answer).setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1)));
        closePanel.addComponent(new Button("I was right", new Runnable() {
            @Override
            public void run() {
                right = true;
                closeGui.close();
            }
        }).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER)));
        closePanel.addComponent(new Button("I was wrong", new Runnable() {
            @Override
            public void run() {
                right = false;
                closeGui.close();
            }
        }).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER)));

        closeGui.setComponent(closePanel);

        textGUI.addWindowAndWait(closeGui);
    }

    public static void wrongWindow(MultiWindowTextGUI textGUI, String answer) {
        final BasicWindow wrongGui = new BasicWindow("");
        wrongGui.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel wrongPanel = new Panel(new GridLayout(2));

        wrongPanel.addComponent(new Label("Wrong!").setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER, true, false, 2, 1)));
        wrongPanel.addComponent(new Label("The correct answer was: ").setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER)));
        wrongPanel.addComponent(new Label(answer).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.CENTER)));
        wrongPanel.addComponent(new EmptySpace());
        wrongPanel.addComponent(new Button("OK", new Runnable() {
            @Override
            public void run() {
                wrongGui.close();
            }
        }).setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER)));

        wrongGui.setComponent(wrongPanel);

        textGUI.addWindowAndWait(wrongGui);
    }

    public static void verifyExit(MultiWindowTextGUI textGUI) {
        BasicWindow verifyWindow = new BasicWindow();
        verifyWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel verifyPanel = new Panel(new GridLayout(3));

        Label message = new Label("Are you sure you want to exit?");
        message.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(3));
        verifyPanel.addComponent(message);

        verifyPanel.addComponent(new EmptySpace());

        Button no = new Button("No", new Runnable() {
            @Override
            public void run() {
                exitStatus = false;
                verifyWindow.close();
            }
        });
        no.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER));
        verifyPanel.addComponent(no);

        Button yes = new Button("Yes", new Runnable() {
            @Override
            public void run() {
                exitStatus = true;
                verifyWindow.close();
            }
        });
        yes.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER));
        verifyPanel.addComponent(yes);


        verifyWindow.setComponent(verifyPanel);

        textGUI.addWindowAndWait(verifyWindow);
    }

    public static void progressWindow(MultiWindowTextGUI textGUI, int mode) throws IOException {
        retry = 0;
        final BasicWindow progressBarWindow = new BasicWindow();
        progressBarWindow.setHints(Collections.singletonList(Window.Hint.CENTERED));

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

        progressBarWindow.setComponent(barPanel);

        textGUI.addWindow(progressBarWindow);
        textGUI.setActiveWindow(progressBarWindow);
        textGUI.updateScreen();

        int start = 500 * (mode + 1);
        int end = 100 * (mode + 1);

        boolean noInternet = false;

        for(int i = 0; i < clues.length; i++) {
            bar.setValue(i);
            textGUI.updateScreen();
            ArrayList<Space[]> returned = generateRandomColumn(start, end, end, requests, textGUI);
            if(Arrays.equals(returned.get(1), new Clue[5])) {
                noInternet = true;
                break;
            }
            clues[i] = (Clue[]) returned.get(1);
            categories[i] = ((Category[]) returned.get(0))[0];
        }
        if(noInternet) {
            noIntWindow(textGUI);
        }
        else {
            bar.setValue(6);
            textGUI.updateScreen();
        }
        progressBarWindow.close();
    }

    public static void noIntWindow(MultiWindowTextGUI textGUI) {
        final BasicWindow noInternetWindow = new BasicWindow("No Internet");
        noInternetWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel noIntPanel = new Panel(new GridLayout(3));

        noIntPanel.addComponent(new Label("Could not connect to the internet.").setLayoutData(GridLayout.createHorizontallyFilledLayoutData(3)));
        noIntPanel.addComponent(new EmptySpace());
        noIntPanel.addComponent(new Button("Retry", new Runnable() {
            @Override
            public void run() {
                retry = 1;
                noInternetWindow.close();
            }
        }).setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1)));

        noIntPanel.addComponent(new Button("Quit", new Runnable() {
            @Override
            public void run() {
                retry = 2;
                noInternetWindow.close();
            }
        }).setLayoutData(GridLayout.createHorizontallyEndAlignedLayoutData(1)));

        noInternetWindow.setComponent(noIntPanel);

        textGUI.addWindowAndWait(noInternetWindow);
    }

    public static String clueWindow(MultiWindowTextGUI textGUI, String categoryText, String clueText, int value) {
        final BasicWindow clueDisplayWindow = new BasicWindow("Clue");
        clueDisplayWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.MODAL));

        Panel cluePanel = new Panel(new GridLayout(2));

        Label valueText = new Label("For $" + value + ":");
        valueText.setLayoutData(GridLayout.createLayoutData(
                GridLayout.Alignment.CENTER,
                GridLayout.Alignment.CENTER,
                true,
                false,
                2,
                1
        ));
        cluePanel.addComponent(valueText);

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

        new Label("Who/what is ").setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.END, GridLayout.Alignment.CENTER)).addTo(cluePanel);

        final String[] guess = new String[1];

        TextBox input = new TextBox() {
            @Override
            public synchronized Result handleKeyStroke(KeyStroke keyStroke) {
                if(keyStroke.getKeyType() == KeyType.Enter) {
                    guess[0] = this.getText();
                    clueDisplayWindow.close();
                    return Result.HANDLED;
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
        ArrayList<Clue> pool = new ArrayList<>(); // Master clue pool, will add and print these clues to the board
        Category finalcat = new Category(0, 0, ""); // Final category, will be added and printed to the board
//        logger.debug("  start cat search");
        while (pool.size() != 5) { // Looking for five clues in a category, main clue loop per column
            Category category;
            try {
                category = Category.getCategories(1, random.nextInt(18320)).get(0); // Gets a random category to try
            }
            catch(IndexOutOfBoundsException e) {
                break;
            }
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

    /**
     * Compares the user's guess against the correct answer
     * @param guess the user's guess
     * @param answer the correct answer
     * @return if the guess and answer match (true/false)
     */
    public static boolean checkAnswer(String guess, String answer) {
        if(guess.length() == 0) {return false;} // If the guess is empty, it is automatically wrong (false)
        guess = parseAnswer(guess); // Parses down the guess and answer
        answer = parseAnswer(answer);

        if(distance(guess, answer) <= 2) { // Uses Levenshtein distance with a threshold of 2 (translates to 2 typos between guess and answer)
            return true;
        }
        else { // If it doesn't pass the distance test,
            if(distance(guess, answer) <= 5) {
                close = true;
            }
            if(answer.contains(guess)) {
                close = true;
            }
            if(guess.contains(answer)) { // checks if the answer is in the user's guess (ex. Korea in Korean War)
                return true;
            }
            else {
                String var;
                String opposite;
                if(answer.contains("(") || guess.contains(")")) { // Checks if there are parenthesis in the guess or answer to compare the guess/answer with the content inside and outside the parenthesis
                    if (answer.contains("(")) {var = answer; opposite = guess;}
                    else {var = guess; opposite = answer;}
                    String pattern = "(\\(.*?\\))";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(var);
                    if (m.find()) {
                        String inside = m.group(1).replaceAll("[()]", "").trim();
                        String outside = var.replaceAll(m.group(1), "").replaceAll("[()]", "").trim();
                        if(Game.checkAnswer(opposite, inside) || Game.checkAnswer(opposite, outside)) {
                            return true;
                        }
                    }
                }

                if(answer.contains("/")) { // Does the same thing as parenthesis, except with a forward slash
                    String first = answer.substring(0, answer.indexOf("/")).trim();
                    String last = answer.substring(answer.indexOf("/")).replaceAll("/", "").trim();
                    if(Game.checkAnswer(guess, first) || Game.checkAnswer(guess, last)) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /**
     * A helper method to parse down an answer into an understandable format to allow the game to compare guess and answer with a large margin of error
     * @param guess the user's raw guess
     * @return the user's parsed guess
     */
    public static String parseAnswer(String guess) {
        guess = guess.toLowerCase().trim(); // Removes outer whitespace
        guess = Normalizer.normalize(guess, Normalizer.Form.NFD); // Removes all accents from both strings, replaces them with their normal counterparts
        guess = guess.replaceAll("[^\\p{ASCII}]", ""); // https://stackoverflow.com/a/3322174
        guess = guess.replaceAll("\"", ""); // Removes all double quotation marks
        guess = guess.replaceAll("\\?", ""); // Removes all question marks
        guess = guess.replaceAll("<[^>]*>", ""); // https://stackoverflow.com/a/4075756 Removes all HTML formatting tags (ex. <i>, <b>)
        guess = guess.replaceAll(",", ""); // Removes commas
        guess=  guess.replaceAll("\\.", ""); // Removes periods
        guess = guess.replaceAll("&", "and"); // Replaces ampersands with "and"
        guess = guess.replaceAll("'", ""); // Removes all single quotation marks
        guess = guess.replaceAll("\\\\", ""); // Removes all back slashes
        guess = guess.replaceAll("\\*", ""); // Removes all asterisks
        guess = guess.trim(); // Removes all whitespace before removing articles, which allows the articles to be found at the beginning of the string
        guess = removeString(guess, "the "); // Removes the articles the, an, a, and your from both guess and answer (possibly more in the future)
        guess = removeString(guess, "an ");
        guess = removeString(guess, "a ");
        guess = removeString(guess, "your ");
        guess = guess.trim(); // Removes all whitespace for the last time
        return guess; // All done!
    }

    /**
     * Compares the guess and answer by detecting character changes, or the "distance" between the two strings
     * @param s string one
     * @param t string two
     * @return distance between the strings
     */
    public static int distance(String s, String t) { // https://en.wikipedia.org/wiki/Levenshtein_distance#Iterative_with_full_matrix
        int m = s.length() + 1;
        int n = t.length() + 1;
        int[][] d = new int[m][n];

        for(int i = 1; i < m; i++) {d[i][0] = i;}
        for(int j = 1; j < n; j++) {d[0][j] = j;}
        int substitutionCost;
        for(int j = 1; j < n; j++) {
            for(int i = 1; i < m; i++) {
                substitutionCost = s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1;
                d[i][j] = Math.min(Math.min(d[i-1][j] + 1, d[i][j-1] + 1), d[i-1][j-1] + substitutionCost);
            }
        }
        return d[m - 1][n - 1];
    }

    /**
     * Removes a substring (article) from the beginning of a string
     * @param original full string
     * @param sub the substring (article) to remove
     * @return modified string
     */
    public static String removeString(String original, String sub) {
        if(original.length() >= sub.length() && original.substring(0, sub.length()).toLowerCase().equals(sub)) {
            original = original.substring(sub.length());
        }
        return original;
    }

    /**
     * Generates a random spot on the board to place a daily double space.
     */
    public static int[] genDailyDouble() {
        int chance = getRandomNumberInRange(1, 10000); // Generates number from 1-10000 (represents 0.00%-100.00%) http://digg.com/2018/joepardy-daily-double-probability-mapped
        int col = 0;
        int row = 0;
        if(isBetween(chance, 1, 4))             {col = 0; row = 0;} // Maps each percentage to a row and column location (ex. 4 = 0.04%)
        else if(isBetween(chance, 4, 7))        {col = 1; row = 0;}
        else if(isBetween(chance, 7, 11))       {col = 2; row = 0;}
        else if(isBetween(chance, 11, 14))      {col = 3; row = 0;}
        else if(isBetween(chance, 14, 17))      {col = 4; row = 0;}
        else if(isBetween(chance, 17, 20))      {col = 5; row = 0;}
        else if(isBetween(chance, 20, 243))     {col = 0; row = 1;}
        else if(isBetween(chance, 243, 367))    {col = 1; row = 1;}
        else if(isBetween(chance, 367, 547))    {col = 2; row = 1;}
        else if(isBetween(chance, 547, 706))    {col = 3; row = 1;}
        else if(isBetween(chance, 706, 883))    {col = 4; row = 1;}
        else if(isBetween(chance, 883, 1009))   {col = 5; row = 1;}
        else if(isBetween(chance, 1009, 1615))  {col = 0; row = 2;}
        else if(isBetween(chance, 1615, 1992))  {col = 1; row = 2;}
        else if(isBetween(chance, 1992, 2514))  {col = 2; row = 2;}
        else if(isBetween(chance, 2514, 3015))  {col = 3; row = 2;}
        else if(isBetween(chance, 3015, 3504))  {col = 4; row = 2;}
        else if(isBetween(chance, 3504, 3869))  {col = 5; row = 2;}
        else if(isBetween(chance, 3869, 4640))  {col = 0; row = 3;}
        else if(isBetween(chance, 4640, 5149))  {col = 1; row = 3;}
        else if(isBetween(chance, 5149, 5875))  {col = 2; row = 3;}
        else if(isBetween(chance, 5875, 6523))  {col = 3; row = 3;}
        else if(isBetween(chance, 6523, 7218))  {col = 4; row = 3;}
        else if(isBetween(chance, 7218, 7693))  {col = 5; row = 3;}
        else if(isBetween(chance, 7693, 8165))  {col = 0; row = 4;}
        else if(isBetween(chance, 8165, 8434))  {col = 1; row = 4;}
        else if(isBetween(chance, 8434, 8869))  {col = 2; row = 4;}
        else if(isBetween(chance, 8869, 9290))  {col = 3; row = 4;}
        else if(isBetween(chance, 9290, 9683))  {col = 4; row = 4;}
        else if(isBetween(chance, 9683, 10003)) {col = 5; row = 4;}
//        logger.info("DD value " + chance + " loc " + col + ", " + row);
        return new int[]{col, row};
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

    public static void afterGUIThreadStarted(MultiWindowTextGUI textGUI) {
        // By default do nothing
    }

    static protected MultiWindowTextGUI createTextGUI(Screen screen) {
        return new MultiWindowTextGUI(new SeparateTextGUIThread.Factory(), screen);
    }

    public static Properties loadPropTheme(String resourceFileName) {
        Properties properties = new Properties();
        try {
            ClassLoader classLoader = AbstractTextGUI.class.getClassLoader();
            InputStream resourceAsStream = classLoader.getResourceAsStream(resourceFileName);
            if(resourceAsStream == null) {
                resourceAsStream = new FileInputStream("jeopardy-graphics/src/main/resources/" + resourceFileName);
//                resourceAsStream = MainGUI.class.getResourceAsStream("src/main/resources/" + resourceFileName);
            }
            properties.load(resourceAsStream);
            resourceAsStream.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
