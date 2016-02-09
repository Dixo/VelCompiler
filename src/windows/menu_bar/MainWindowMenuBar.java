package windows.menu_bar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Dixo on 2/2/2016.
 */
public class MainWindowMenuBar extends MenuBar implements ActionListener{

    private Menu fileMenu;

    public MainWindowMenuBar(){
        computeMenus();
        add(fileMenu);
    }

    private void computeMenus(){

        computeFileMenu();
    }

    private void computeFileMenu(){

        fileMenu = new Menu("File");

        MenuItem saveButton = new MenuItem("Save");
        saveButton.setActionCommand("Save");
        saveButton.addActionListener(this);

        fileMenu.add(saveButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        switch (e.getActionCommand()){
            case "Save":
                System.out.println("Helloooo");
                break;
        }
    }
}
