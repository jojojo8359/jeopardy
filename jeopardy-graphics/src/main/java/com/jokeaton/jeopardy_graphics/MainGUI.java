package main.java.com.jokeaton.jeopardy_graphics;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class MainGUI {
    public static int money = 0;

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
                boardWindow(textGUI);
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

    public static void boardWindow(final MultiWindowTextGUI textGUI) {
        final BasicWindow _boardWindow = new BasicWindow("Single Jeopardy!");
        _boardWindow.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.EXPANDED));

        Panel boardPanel = new Panel();

        Table<String> table = new Table<String>("Category 1", "Category 2", "Category 3", "Category 4", "Category 5");

        table.getTableModel().addRow("$100", "$100", "$100", "$100", "$100");
        table.getTableModel().addRow("$200", "$200", "$200", "$200", "$200");
        table.getTableModel().addRow("$300", "$300", "$300", "$300", "$300");
        table.getTableModel().addRow("$400", "$400", "$400", "$400", "$400");
        table.getTableModel().addRow("$500", "$500", "$500", "$500", "$500");

        table.setCellSelection(true);

        table.setSelectAction(new Runnable() {
            @Override
            public void run() {
                System.out.println(table.getSelectedColumn() + ", " + table.getSelectedRow());
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
                _boardWindow.close();
            }
        }).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center)));
        _boardWindow.setComponent(boardPanel);
        textGUI.addWindow(_boardWindow);
    }

    public static void afterGUIThreadStarted(MultiWindowTextGUI textGUI) {
        // By default do nothing
    }

    static protected MultiWindowTextGUI createTextGUI(Screen screen) {
        return new MultiWindowTextGUI(new SeparateTextGUIThread.Factory(), screen);
    }
}

