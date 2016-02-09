package windows;

import windows.menu_bar.MainWindowMenuBar;
import windows.messages.MainMessagesHolder;
import windows.text_editor.MainTextHolder;
import windows.tools.MainToolsHolder;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Dixo on 2/2/2016.
 */
public class MainWindow extends JFrame {

    private JPanel mainPanel;
    private MainWindowMenuBar menuBar;
    private MainTextHolder mainTextHolder;
    private MainToolsHolder mainToolsHolder;
    private MainMessagesHolder mainMessagesHolder;

    public MainWindow() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        menuBar = new MainWindowMenuBar();
        setMenuBar(menuBar);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        mainToolsHolder = new MainToolsHolder();
        mainPanel.add(mainToolsHolder, BorderLayout.NORTH);

        mainTextHolder = new MainTextHolder();
        mainPanel.add(mainTextHolder, BorderLayout.CENTER);

        mainMessagesHolder = new MainMessagesHolder();
        mainPanel.add(mainMessagesHolder, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        setSize(400,400);
        //pack();
        setVisible(true);
    }
}
