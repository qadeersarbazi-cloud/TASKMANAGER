package project;

import javax.swing.*;
import java.awt.*;

/** Entry point of the graphical version. Run this class. */
public class TaskManagerGUI extends JFrame {

    public TaskManagerGUI() {
        super("TaskFlow - Student Task Manager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(new AppPanel(true));
        setMinimumSize(new Dimension(1000, 660));
        setSize(1240, 780);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new TaskManagerGUI().setVisible(true));
    }
}
