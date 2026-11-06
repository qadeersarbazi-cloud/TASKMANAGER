package project;

import javax.swing.*;
import java.awt.*;

/** Modal window around {@link FormPanel}. After setVisible(true) read {@link #result()}. */
class FormDialog extends JDialog {
    private final FormPanel panel;

    FormDialog(Window owner, Assignment existing) {
        super(owner, existing == null ? "New assignment" : "Edit assignment", ModalityType.APPLICATION_MODAL);
        panel = new FormPanel(existing, this::dispose);
        setContentPane(panel);
        getRootPane().setDefaultButton(panel.saveButton);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    FormPanel result() { return panel; }
}
