package project;

import javax.swing.*;
import java.awt.*;

/** Modal window around {@link BenchmarkPanel}. */
class BenchmarkDialog extends JDialog {
    BenchmarkDialog(Window owner) {
        super(owner, "Big-O benchmark", ModalityType.APPLICATION_MODAL);
        setContentPane(new BenchmarkPanel());
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(owner);
    }
}
