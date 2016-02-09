package windows.text_editor;

import windows.utils.WindowColors;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Dixo on 2/3/2016.
 */
public class MainTextHolder extends JPanel {

    private JTextPane codeHoder;
    private TextEnhancer textEnhancer;

    public MainTextHolder() {
        super(new BorderLayout());

        textEnhancer = new TextEnhancer();

        codeHoder = new JTextPane(textEnhancer.getEnhancer());
        codeHoder.setBackground(WindowColors.CODE_HOLDER);


        JScrollPane scrollPane = new JScrollPane(codeHoder);
        add(scrollPane, BorderLayout.CENTER);
    }
}