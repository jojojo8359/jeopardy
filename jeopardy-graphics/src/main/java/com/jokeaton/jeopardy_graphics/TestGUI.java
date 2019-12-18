package main.java.com.jokeaton.jeopardy_graphics;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public class TestGUI {
    public static boolean animRunning = false;
    public static int num = 0;

    public static void main(String[] args) throws IOException, InterruptedException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory().setInitialTerminalSize(new TerminalSize(130, 45));
        terminalFactory.setTerminalEmulatorTitle("Theme Test");
        Screen screen;
        screen = terminalFactory.createScreen();
        screen.startScreen();
        MultiWindowTextGUI textGUI = createTextGUI(screen);
        loadTheme(textGUI);

        textGUI.setBlockingIO(false);
        textGUI.setEOFWhenNoWindows(true);
        textGUI.isEOFWhenNoWindows();

        try {
            mainWindow(textGUI);
            AsynchronousTextGUIThread guiThread = (AsynchronousTextGUIThread) textGUI.getGUIThread();
            guiThread.start();
            guiThread.waitForStop();
        } finally {
            screen.stopScreen();
        }
    }

    public static void mainWindow(MultiWindowTextGUI textGUI) {
        final BasicWindow testingWindow = new BasicWindow("Testing");
        testingWindow.setHints(Arrays.asList(Window.Hint.FULL_SCREEN));

        Panel testPanel = new Panel(new GridLayout(4));



        ActionListBox alb = new ActionListBox(new TerminalSize(15, 5))
                .addItem(new NullRunnable("Item #1"))
                .addItem(new NullRunnable("Item #2"))
                .addItem(new NullRunnable("Item #3"))
                .addItem(new NullRunnable("Item #4"))
                .addItem(new NullRunnable("Item #5"))
                .addItem(new NullRunnable("Item #6"))
                .addItem(new NullRunnable("Item #7"))
                .addItem(new NullRunnable("Item #8"));
        testPanel.addComponent(alb);



        Panel animPanel = new Panel(new GridLayout(1));
        AnimatedLabel animatedLabel = new AnimatedLabel("First Frame")
                .addFrame("Second Frame")
                .addFrame("Third Frame")
                .addFrame("Last Frame");
        animPanel.addComponent(animatedLabel);
        animPanel.addComponent(new Button("Run anim", new Runnable() {
                    @Override
                    public void run() {
                        if(animRunning) {
                            animatedLabel.stopAnimation();
                            animRunning = false;
                        }
                        else {
                            animatedLabel.startAnimation(500);
                            animRunning = true;
                        }
                    }
                }));
        testPanel.addComponent(animPanel);



        testPanel.addComponent(new Panel(new GridLayout(4))
                .addComponent(new EmptySpace(new TerminalSize(4, 2)).withBorder(Borders.singleLine()))
                .addComponent(new EmptySpace(new TerminalSize(4, 2)).withBorder(Borders.singleLineBevel()))
                .addComponent(new EmptySpace(new TerminalSize(4, 2)).withBorder(Borders.doubleLine()))
                .addComponent(new EmptySpace(new TerminalSize(4, 2)).withBorder(Borders.doubleLineBevel())));



        testPanel.addComponent(new Button("This is a button"));



        testPanel.addComponent(new CheckBox("This is a checkbox"));



        testPanel.addComponent(new CheckBoxList<String>(new TerminalSize(15, 5))
                .addItem("Item #1")
                .addItem("Item #2")
                .addItem("Item #3")
                .addItem("Item #4")
                .addItem("Item #5")
                .addItem("Item #6")
                .addItem("Item #7")
                .addItem("Item #8"));



        testPanel.addComponent(new Panel()
                .addComponent(new ComboBox<String>("Editable", "Item #2", "Item #3", "Item #4", "Item #5", "Item #6", "Item #7")
                        .setReadOnly(false)
                        .setPreferredSize(new TerminalSize(12, 1)))
                .addComponent(new EmptySpace())
                .addComponent(new ComboBox<String>("Read-only", "Item #2", "Item #3", "Item #4", "Item #5", "Item #6", "Item #7")
                        .setReadOnly(true)
                        .setPreferredSize(new TerminalSize(12, 1))));



        testPanel.addComponent(new Label("This is a label"));



        testPanel.addComponent(new RadioBoxList<String>(new TerminalSize(15, 5))
                .addItem("Item #1")
                .addItem("Item #2")
                .addItem("Item #3")
                .addItem("Item #4")
                .addItem("Item #5")
                .addItem("Item #6")
                .addItem("Item #7")
                .addItem("Item #8"));



        testPanel.addComponent(new ProgressBar(0, 100, 24)
                .setLabelFormat("%2.0f%%")
                .setValue(26));



        testPanel.addComponent(new Panel(new GridLayout(2)).setLayoutManager(new GridLayout(2))
                .addComponent(new ScrollBar(Direction.HORIZONTAL).setPreferredSize(new TerminalSize(6, 1)))
                .addComponent(new ScrollBar(Direction.VERTICAL).setPreferredSize(new TerminalSize(1, 6))));



        testPanel.addComponent(new Panel(new GridLayout(2)).addComponent(new Separator(Direction.HORIZONTAL).setPreferredSize(new TerminalSize(6, 1)))
                .addComponent(new Separator(Direction.VERTICAL).setPreferredSize(new TerminalSize(1, 6))));



        testPanel.addComponent(new Table<String>("Column #1", "Column #2", "Column #3")
                .setTableModel(
                        new TableModel<String>("Column #1", "Column #2", "Column #3")
                                .addRow("Row #1", "Row #1", "Row #1")
                                .addRow("Row #2", "Row #2", "Row #2")
                                .addRow("Row #3", "Row #3", "Row #3")
                                .addRow("Row #4", "Row #4", "Row #4")));



        testPanel.addComponent(new Panel().addComponent(
                Panels.horizontal(
                        new TextBox("Single-line text box")
                                .setPreferredSize(new TerminalSize(15, 1)),
                        new TextBox("Single-line read-only")
                                .setPreferredSize(new TerminalSize(15, 1))
                                .setReadOnly(true)))
                .addComponent(new EmptySpace())
                .addComponent(
                        Panels.horizontal(
                                new TextBox(new TerminalSize(15, 5), "Multi\nline\ntext\nbox\nHere is a very long line that doesn't fit")
                                        .setVerticalFocusSwitching(false),
                                new TextBox(new TerminalSize(15, 5), "Multi\nline\nread-only\ntext\nbox\n" +
                                        "Here is a very long line that doesn't fit")
                                        .setReadOnly(true))));

        testPanel.addComponent(new Button("Make new window", new Runnable() {
            @Override
            public void run() {
                testingWindow.setVisible(false);
                MessageDialog.showMessageDialog(textGUI, "New window", "This is a new window", MessageDialogButton.OK);
                testingWindow.setVisible(true);
            }
        }));

        testPanel.addComponent(new Button("Reload theme", new Runnable() {
            @Override
            public void run() {
                loadTheme(textGUI);
            }
        }));



        testingWindow.setComponent(testPanel);
        textGUI.addWindow(testingWindow);
    }

    private static class NullRunnable implements Runnable {
        private final String label;

        public NullRunnable(String label) {
            this.label = label;
        }

        @Override
        public void run() {
        }

        @Override
        public String toString() {
            return label;
        }
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
            }
            properties.load(resourceAsStream);
            resourceAsStream.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void loadTheme(MultiWindowTextGUI textGUI) {
        LanternaThemes.registerTheme("asdf" + num, new PropertyTheme(Objects.requireNonNull(loadPropTheme("asdf-theme.properties")), false));
        textGUI.setTheme(LanternaThemes.getRegisteredTheme("asdf" + num));
        num++;
    }
}
