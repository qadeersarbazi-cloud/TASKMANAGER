package project;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** The whole window content: sidebar, search/sort toolbar, stat cards and the assignment table. */
public class AppPanel extends JPanel {
    private static final String TABLE_CARD = "table", EMPTY_CARD = "empty";

    private final TaskManager manager = new TaskManager();
    private final FileStorage storage = new FileStorage("assignments.txt");
    private List<Assignment> shown = new ArrayList<>();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"#", "ASSIGNMENT", "SUBJECT", "DEADLINE", "DUE", "PRIORITY"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    private final Ui.StyledTextField searchField = new Ui.StyledTextField("Search assignments...", 24);
    private final Ui.StyledCombo searchMode = new Ui.StyledCombo("Name \u00b7 Linear", "ID \u00b7 Linear", "ID \u00b7 HashMap");
    private final Ui.StyledCombo sortCombo = new Ui.StyledCombo("Order: As added", "Sort: Bubble Sort",
            "Sort: Insertion Sort", "Sort: Merge Sort");

    private final JLabel totalValue = Ui.label("0", Font.BOLD, 26, Ui.TEXT);
    private final JLabel totalSub = Ui.label(" ", Font.PLAIN, 12, Ui.MUTED);
    private final JLabel urgentValue = Ui.label("-", Font.BOLD, 17, Ui.TEXT);
    private final JLabel urgentSub = Ui.label(" ", Font.PLAIN, 12, Ui.MUTED);
    private final JLabel prioValue = Ui.label("-", Font.BOLD, 17, Ui.TEXT);
    private final JLabel prioSub = Ui.label(" ", Font.PLAIN, 12, Ui.MUTED);
    private final JLabel overdueValue = Ui.label("0", Font.BOLD, 26, Ui.TEXT);
    private final JLabel overdueSub = Ui.label(" ", Font.PLAIN, 12, Ui.MUTED);

    private final JLabel hoodList = Ui.label("0", Font.BOLD, 12, Ui.TEXT);
    private final JLabel hoodMap = Ui.label("0", Font.BOLD, 12, Ui.TEXT);
    private final JLabel hoodHeap = Ui.label("0", Font.BOLD, 12, Ui.TEXT);

    private final JLabel timingLabel = Ui.label(" ", Font.PLAIN, 12, Ui.MUTED);
    private final JLabel toast = Ui.label(" ", Font.BOLD, 13, Ui.LOW);
    private final Timer toastTimer = new Timer(3500, e -> toast.setText(" "));

    private final CardLayout centerCards = new CardLayout();
    private final JPanel centerPanel = new JPanel(centerCards);
    private final JLabel emptyTitle = Ui.label("", Font.BOLD, 20, Ui.TEXT);
    private final JLabel emptySub = Ui.label("", Font.PLAIN, 13, Ui.MUTED);
    private final Ui.RoundedButton editBtn = Ui.RoundedButton.ghost("Edit");
    private final Ui.RoundedButton deleteBtn = Ui.RoundedButton.ghost("Delete");

    public AppPanel(boolean loadFile) {
        super(new BorderLayout());
        setBackground(Ui.BG);
        toastTimer.setRepeats(false);

        if (loadFile) {
            try { storage.load(manager); }
            catch (IOException e) { showToast("Could not read file: " + e.getMessage(), Ui.HIGH); }
        }

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        searchMode.addActionListener(e -> refresh());
        sortCombo.addActionListener(e -> refresh());
        refresh();
    }

    /** Test hook: choose a sort option (0 = as added, 1 = bubble, 2 = insertion, 3 = merge). */
    void sortBy(int index) { sortCombo.setSelectedIndex(index); }

    // ================================================================ building the UI

    private JComponent buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(Ui.SIDEBAR);
        sb.setBorder(new EmptyBorder(28, 20, 22, 20));
        sb.setPreferredSize(new Dimension(250, 0));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.add(new Ui.Logo(), BorderLayout.WEST);
        JPanel names = new JPanel(new GridLayout(2, 1));
        names.setOpaque(false);
        names.add(Ui.label("TaskFlow", Font.BOLD, 19, Ui.TEXT));
        names.add(Ui.label("Student Task Manager", Font.PLAIN, 12, Ui.MUTED));
        brand.add(names, BorderLayout.CENTER);
        sb.add(left(brand, 46));
        sb.add(Box.createVerticalStrut(34));

        Ui.RoundedButton add = Ui.RoundedButton.primary("+  New assignment");
        add.addActionListener(e -> openForm(null));
        Ui.RoundedButton bench = Ui.RoundedButton.ghost("Run Big-O benchmark");
        bench.addActionListener(e -> new BenchmarkDialog(SwingUtilities.getWindowAncestor(this)).setVisible(true));
        sb.add(left(add, 44));
        sb.add(Box.createVerticalStrut(10));
        sb.add(left(bench, 44));

        sb.add(Box.createVerticalGlue());

        sb.add(left(Ui.label("UNDER THE HOOD", Font.BOLD, 11, Ui.MUTED), 18));
        sb.add(Box.createVerticalStrut(8));
        Ui.RoundedPanel hood = new Ui.RoundedPanel(Ui.CARD, Ui.BORDER, 16, new GridLayout(3, 1, 0, 10));
        hood.setBorder(new EmptyBorder(14, 16, 14, 16));
        hood.add(hoodRow("ArrayList", "list storage", hoodList));
        hood.add(hoodRow("HashMap", "ID lookup", hoodMap));
        hood.add(hoodRow("PriorityQueue", "2 heaps", hoodHeap));
        sb.add(left(hood, 128));
        sb.add(Box.createVerticalStrut(12));
        sb.add(left(Ui.label("Auto-saved to assignments.txt", Font.PLAIN, 11, Ui.MUTED), 16));
        return sb;
    }

    private JComponent hoodRow(String name, String what, JLabel value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JPanel l = new JPanel(new GridLayout(2, 1));
        l.setOpaque(false);
        l.add(Ui.label(name, Font.BOLD, 12, Ui.TEXT));
        l.add(Ui.label(what, Font.PLAIN, 10, Ui.MUTED));
        row.add(l, BorderLayout.CENTER);
        row.add(value, BorderLayout.EAST);
        return row;
    }

    private static JComponent left(JComponent c, int height) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setPreferredSize(new Dimension(c.getPreferredSize().width, height));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return c;
    }

    private JComponent buildMain() {
        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(26, 30, 18, 30));

        // ---- north: title row, toolbar, stat cards
        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(Ui.label("Assignments", Font.BOLD, 28, Ui.TEXT));
        titles.add(Ui.label("Stay on top of deadlines. Smart queue shows what matters first.", Font.PLAIN, 13, Ui.MUTED));
        titleRow.add(titles, BorderLayout.WEST);
        toast.setHorizontalAlignment(SwingConstants.RIGHT);
        titleRow.add(toast, BorderLayout.EAST);
        north.add(left(titleRow, 58));
        north.add(Box.createVerticalStrut(18));

        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 0;
        c.gridx = 0; c.weightx = 1; c.insets = new Insets(0, 0, 0, 10);
        toolbar.add(searchField, c);
        c.gridx = 1; c.weightx = 0;
        searchMode.setPreferredSize(new Dimension(170, 42));
        toolbar.add(searchMode, c);
        c.gridx = 2; c.insets = new Insets(0, 0, 0, 0);
        sortCombo.setPreferredSize(new Dimension(190, 42));
        toolbar.add(sortCombo, c);
        north.add(left(toolbar, 44));
        north.add(Box.createVerticalStrut(16));

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        stats.add(statCard("TOTAL", Ui.ACCENT_HI, totalValue, totalSub));
        stats.add(statCard("MOST URGENT  \u00b7  heap", Ui.MED, urgentValue, urgentSub));
        stats.add(statCard("TOP PRIORITY  \u00b7  heap", Ui.HIGH, prioValue, prioSub));
        stats.add(statCard("OVERDUE", Ui.LOW, overdueValue, overdueSub));
        north.add(left(stats, 104));
        main.add(north, BorderLayout.NORTH);

        // ---- center: table or empty state
        centerPanel.setOpaque(false);
        centerPanel.add(buildTable(), TABLE_CARD);
        centerPanel.add(buildEmpty(), EMPTY_CARD);
        main.add(centerPanel, BorderLayout.CENTER);

        // ---- south: timing + actions
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(timingLabel, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);
        actions.add(editBtn);
        actions.add(deleteBtn);
        south.add(actions, BorderLayout.EAST);
        main.add(south, BorderLayout.SOUTH);
        return main;
    }

    private JComponent statCard(String title, Color accent, JLabel value, JLabel sub) {
        Ui.RoundedPanel card = new Ui.RoundedPanel(Ui.CARD, Ui.BORDER, 18, new GridLayout(3, 1, 0, 2));
        card.setBorder(new EmptyBorder(14, 18, 12, 14));
        card.add(Ui.label(title, Font.BOLD, 11, accent));
        card.add(value);
        card.add(sub);
        return card;
    }

    private JComponent buildTable() {
        table.setBackground(Ui.CARD);
        table.setForeground(Ui.TEXT);
        table.setSelectionBackground(Ui.SELECT);
        table.setSelectionForeground(Ui.TEXT);
        table.setRowHeight(48);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new CellRenderer());
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        int[] widths = {55, 330, 190, 120, 150, 120};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        table.getSelectionModel().addListSelectionListener(e -> {
            boolean has = table.getSelectedRow() >= 0;
            editBtn.setEnabled(has);
            deleteBtn.setEnabled(has);
        });
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) editSelected();
            }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DELETE"), "del");
        table.getActionMap().put("del", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { deleteSelected(); }
        });

        JScrollPane sp = new JScrollPane(table);
        Ui.styleScroll(sp, Ui.CARD);
        Ui.RoundedPanel card = new Ui.RoundedPanel(Ui.CARD, Ui.BORDER, 18, new BorderLayout());
        card.setBorder(new EmptyBorder(6, 6, 6, 6));
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildEmpty() {
        Ui.RoundedPanel card = new Ui.RoundedPanel(Ui.CARD, Ui.BORDER, 18, new GridBagLayout());
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        emptyTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptySub.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(emptyTitle);
        box.add(Box.createVerticalStrut(6));
        box.add(emptySub);
        card.add(box);
        return card;
    }

    // ================================================================ table rendering

    private class CellRenderer extends DefaultTableCellRenderer {
        private final Ui.Pill pill = new Ui.Pill("", Ui.MED);
        private final JPanel pillHolder = new JPanel(new GridBagLayout());

        CellRenderer() {
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST;
            gc.insets = new Insets(0, 14, 0, 0);
            pillHolder.add(pill, gc);
            GridBagConstraints fill = new GridBagConstraints();
            fill.weightx = 1;
            pillHolder.add(Box.createGlue(), fill);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            Color bg = sel ? Ui.SELECT : (row % 2 == 0 ? Ui.CARD : Ui.CARD_ALT);
            Assignment a = row < shown.size() ? shown.get(row) : null;
            if (col == 5 && a != null) {
                pill.set(a.priorityName(), Ui.priorityColor(a.getPriority()));
                pillHolder.setBackground(bg);
                return pillHolder;
            }
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, false, row, col);
            l.setBackground(bg);
            l.setForeground(Ui.TEXT);
            l.setFont(Ui.font(Font.PLAIN, 14));
            l.setBorder(new EmptyBorder(0, 14, 0, 8));
            if (col == 0) { l.setForeground(Ui.MUTED); l.setFont(Ui.font(Font.PLAIN, 13)); }
            if (col == 1) l.setFont(Ui.font(Font.BOLD, 14));
            if (col == 2) l.setForeground(Ui.MUTED);
            if (col == 4 && a != null) {
                long days = daysLeft(a);
                l.setForeground(days < 0 ? Ui.HIGH : days <= 2 ? Ui.MED : Ui.LOW);
                l.setFont(Ui.font(Font.BOLD, 13));
            }
            return l;
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, false, false, row, col);
            l.setOpaque(true);
            l.setBackground(Ui.CARD);
            l.setForeground(Ui.MUTED);
            l.setFont(Ui.font(Font.BOLD, 11));
            l.setBorder(new EmptyBorder(0, 14, 0, 8));
            return l;
        }
    }

    // ================================================================ refresh

    private static long daysLeft(Assignment a) {
        return ChronoUnit.DAYS.between(LocalDate.now(), a.getDeadline());
    }

    private static String dueText(Assignment a) {
        long d = daysLeft(a);
        if (d < 0) return "Overdue by " + (-d) + (d == -1 ? " day" : " days");
        if (d == 0) return "Due today";
        if (d == 1) return "Due tomorrow";
        return "Due in " + d + " days";
    }

    private static String ms(double v) {
        return v < 0.01 ? "<0.01 ms" : String.format("%.2f ms", v);
    }

    void refresh() {
        // 1) optional search (linear search or HashMap, as selected)
        String q = searchField.getText().trim();
        List<String> info = new ArrayList<>();
        List<Assignment> base;
        if (q.isEmpty()) {
            base = new ArrayList<>(manager.getAll());
        } else {
            int mode = searchMode.getSelectedIndex();
            long t0 = System.nanoTime();
            if (mode == 0) {
                base = manager.searchByNameLinear(q);
            } else {
                base = new ArrayList<>();
                try {
                    int id = Integer.parseInt(q);
                    Assignment a = mode == 1 ? manager.searchByIdLinear(id) : manager.searchByIdHash(id);
                    if (a != null) base.add(a);
                } catch (NumberFormatException ignored) { }
            }
            String what = mode == 2 ? "HashMap lookup" : "Linear search";
            info.add(what + " " + ms((System.nanoTime() - t0) / 1e6));
        }

        // 2) optional sort by deadline (hand-written algorithms)
        int s = sortCombo.getSelectedIndex();
        if (s > 0) {
            long t0 = System.nanoTime();
            String name;
            if (s == 1) { base = SortAlgorithms.bubbleSort(base, SortAlgorithms.BY_DEADLINE); name = "Bubble Sort"; }
            else if (s == 2) { base = SortAlgorithms.insertionSort(base, SortAlgorithms.BY_DEADLINE); name = "Insertion Sort"; }
            else { base = SortAlgorithms.mergeSort(base, SortAlgorithms.BY_DEADLINE); name = "Merge Sort"; }
            info.add(name + " " + ms((System.nanoTime() - t0) / 1e6));
        }
        timingLabel.setText(info.isEmpty() ? shownCountText(base.size())
                : shownCountText(base.size()) + "   \u00b7   " + String.join("   \u00b7   ", info));

        // 3) table
        shown = base;
        model.setRowCount(0);
        for (Assignment a : shown) {
            model.addRow(new Object[]{a.getId(), a.getName(), a.getSubject(), a.getDeadline().toString(),
                    dueText(a), a.priorityName()});
        }

        updateStats();
        if (shown.isEmpty()) {
            boolean none = manager.size() == 0;
            emptyTitle.setText(none ? "No assignments yet" : "No results");
            emptySub.setText(none ? "Click \"+ New assignment\" to add your first one."
                    : "Nothing matches your search. Try a different word or mode.");
            centerCards.show(centerPanel, EMPTY_CARD);
        } else {
            centerCards.show(centerPanel, TABLE_CARD);
        }
    }

    private String shownCountText(int n) {
        return "Showing " + n + " of " + manager.size();
    }

    private void updateStats() {
        int n = manager.size();
        totalValue.setText(String.valueOf(n));
        totalSub.setText(n == 1 ? "assignment" : "assignments");

        Assignment u = manager.mostUrgent();
        urgentValue.setText(u == null ? "-" : u.getName());
        urgentSub.setText(u == null ? "Nothing to do" : dueText(u));
        Assignment h = manager.highestPriority();
        prioValue.setText(h == null ? "-" : h.getName());
        prioSub.setText(h == null ? "Nothing to do" : h.priorityName() + " priority");

        int overdue = 0;
        for (Assignment a : manager.getAll()) if (daysLeft(a) < 0) overdue++;
        overdueValue.setText(String.valueOf(overdue));
        overdueValue.setForeground(overdue > 0 ? Ui.HIGH : Ui.TEXT);
        overdueSub.setText(overdue == 0 ? "all on track" : "need attention");

        hoodList.setText(n + " items");
        hoodMap.setText(n + " keys");
        hoodHeap.setText(n + " nodes");
    }

    // ================================================================ actions

    private Window window() { return SwingUtilities.getWindowAncestor(this); }

    private void openForm(Assignment existing) {
        FormDialog dlg = new FormDialog(window(), existing);
        dlg.setVisible(true);
        FormPanel r = dlg.result();
        if (!r.accepted) return;
        if (existing == null) {
            manager.add(r.name, r.subject, r.deadline, r.priority);
            showToast("Assignment added", Ui.LOW);
        } else {
            manager.update(existing.getId(), r.name, r.subject, r.deadline, r.priority);
            showToast("Changes saved", Ui.LOW);
        }
        persist();
        refresh();
    }

    private Assignment selected() {
        int row = table.getSelectedRow();
        return row >= 0 && row < shown.size() ? shown.get(row) : null;
    }

    private void editSelected() {
        Assignment a = selected();
        if (a != null) openForm(a);
    }

    private void deleteSelected() {
        Assignment a = selected();
        if (a == null) return;
        if (Ui.confirm(this, "Delete assignment", "Delete \"" + a.getName() + "\"? This cannot be undone.", "Delete")) {
            manager.delete(a.getId());
            persist();
            refresh();
            showToast("Assignment deleted", Ui.MED);
        }
    }

    private void persist() {
        try {
            storage.save(manager.getAll());
        } catch (IOException e) {
            showToast("Could not save: " + e.getMessage(), Ui.HIGH);
        }
    }

    private void showToast(String msg, Color color) {
        toast.setText(msg);
        toast.setForeground(color);
        toastTimer.restart();
    }
}
