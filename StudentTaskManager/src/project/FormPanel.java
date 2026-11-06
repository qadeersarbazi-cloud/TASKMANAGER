package project;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Content of the add / edit dialog. */
class FormPanel extends JPanel {
    boolean accepted;
    String name, subject;
    LocalDate deadline;
    int priority = 2;

    private final Ui.StyledTextField nameF = new Ui.StyledTextField("e.g. Data Structures Assignment 3", 26);
    private final Ui.StyledTextField subjectF = new Ui.StyledTextField("e.g. Data Structures", 26);
    private final Ui.StyledTextField dateF = new Ui.StyledTextField("yyyy-MM-dd", 26);
    private final JLabel error = Ui.label(" ", Font.PLAIN, 12, Ui.HIGH);
    private final Ui.RoundedButton[] prio = new Ui.RoundedButton[3];

    final Ui.RoundedButton saveButton;
    private final Runnable close;

    FormPanel(Assignment existing, Runnable close) {
        super(new BorderLayout());
        this.close = close;

        if (existing != null) {
            nameF.setText(existing.getName());
            subjectF.setText(existing.getSubject());
            dateF.setText(existing.getDeadline().toString());
            priority = existing.getPriority();
        } else {
            dateF.setText(LocalDate.now().plusDays(3).toString());
        }

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Ui.BG);
        p.setBorder(new EmptyBorder(26, 30, 22, 30));

        JLabel title = Ui.label(existing == null ? "New assignment" : "Edit assignment", Font.BOLD, 22, Ui.TEXT);
        JLabel sub = Ui.label(existing == null ? "Add something to your to-do list."
                : "Assignment #" + existing.getId(), Font.PLAIN, 13, Ui.MUTED);
        add(p, title, 2);
        add(p, sub, 20);

        add(p, Ui.label("Name", Font.BOLD, 12, Ui.MUTED), 6);
        add(p, nameF, 14);
        add(p, Ui.label("Subject", Font.BOLD, 12, Ui.MUTED), 6);
        add(p, subjectF, 14);
        add(p, Ui.label("Deadline", Font.BOLD, 12, Ui.MUTED), 6);
        add(p, dateF, 8);

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.add(chip("Today", 0));
        chips.add(chip("Tomorrow", 1));
        chips.add(chip("In 1 week", 7));
        chips.add(chip("In 1 month", 30));
        add(p, chips, 14);

        add(p, Ui.label("Priority", Font.BOLD, 12, Ui.MUTED), 6);
        JPanel pr = new JPanel(new GridLayout(1, 3, 8, 0));
        pr.setOpaque(false);
        String[] names = {"High", "Medium", "Low"};
        for (int i = 0; i < 3; i++) {
            final int value = i + 1;
            prio[i] = new Ui.RoundedButton(names[i], Ui.GHOST, Ui.GHOST_HI, Ui.MUTED);
            prio[i].setActiveColor(Ui.priorityColor(value));
            prio[i].addActionListener(e -> selectPriority(value));
            pr.add(prio[i]);
        }
        add(p, pr, 10);
        selectPriority(priority);

        add(p, error, 14);

        Ui.RoundedButton cancel = Ui.RoundedButton.ghost("Cancel");
        Ui.RoundedButton save = saveButton = Ui.RoundedButton.primary(existing == null ? "Add assignment" : "Save changes");
        cancel.addActionListener(e -> close.run());
        save.addActionListener(e -> trySave());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);
        row.add(cancel);
        row.add(save);
        add(p, row, 0);

        add(p, BorderLayout.CENTER);
    }

    private static void add(JPanel p, JComponent c, int gapAfter) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        p.add(c);
        if (gapAfter > 0) p.add(Box.createVerticalStrut(gapAfter));
    }

    private Ui.RoundedButton chip(String text, int daysFromToday) {
        Ui.RoundedButton b = Ui.RoundedButton.ghost(text);
        b.setFont(Ui.font(Font.PLAIN, 12));
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        b.addActionListener(e -> dateF.setText(LocalDate.now().plusDays(daysFromToday).toString()));
        return b;
    }

    private void selectPriority(int p) {
        priority = p;
        for (int i = 0; i < 3; i++) prio[i].setActive(i + 1 == p);
    }

    private void trySave() {
        String n = nameF.getText().trim();
        String s = subjectF.getText().trim();
        if (n.isEmpty()) { error.setText("Please enter a name."); nameF.requestFocus(); return; }
        if (s.isEmpty()) { error.setText("Please enter a subject."); subjectF.requestFocus(); return; }
        LocalDate d;
        try {
            d = LocalDate.parse(dateF.getText().trim());
        } catch (DateTimeParseException ex) {
            error.setText("Deadline must look like 2026-12-31.");
            dateF.requestFocus();
            return;
        }
        name = n; subject = s; deadline = d; accepted = true;
        close.run();
    }
}
