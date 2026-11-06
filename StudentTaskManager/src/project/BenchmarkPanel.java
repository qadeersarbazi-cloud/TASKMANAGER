package project;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Runs the Big-O benchmark and draws the results as bar charts (log scale). */
class BenchmarkPanel extends JPanel {
    private static final int[] SIZES = {1000, 5000, 10000, 20000};

    Benchmark.Result result;
    private final JTextArea summary = new JTextArea("Pick a size and press Run.");
    private final Ui.StyledCombo sizeCombo =
            new Ui.StyledCombo("n = 1,000", "n = 5,000", "n = 10,000", "n = 20,000");
    private final Ui.RoundedButton run = Ui.RoundedButton.primary("Run benchmark");
    private final Chart chart = new Chart();

    BenchmarkPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(Ui.BG);
        p.setBorder(new EmptyBorder(24, 28, 22, 28));

        JPanel top = new JPanel(new BorderLayout(14, 0));
        top.setOpaque(false);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(Ui.label("Big-O benchmark", Font.BOLD, 22, Ui.TEXT));
        titles.add(Ui.label("Same data, different data structures. Bars use a log scale.", Font.PLAIN, 13, Ui.MUTED));
        top.add(titles, BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);
        sizeCombo.setSelectedIndex(1);
        controls.add(sizeCombo);
        controls.add(run);
        top.add(controls, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        Ui.RoundedPanel chartCard = new Ui.RoundedPanel(Ui.CARD, Ui.BORDER, 18, new BorderLayout());
        chartCard.setBorder(new EmptyBorder(14, 18, 14, 18));
        chartCard.add(chart, BorderLayout.CENTER);
        p.add(chartCard, BorderLayout.CENTER);

        summary.setEditable(false);
        summary.setFocusable(false);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setOpaque(false);
        summary.setFont(Ui.font(Font.PLAIN, 13));
        summary.setForeground(Ui.MUTED);
        summary.setRows(2);
        summary.setBorder(new EmptyBorder(2, 4, 0, 4));
        p.add(summary, BorderLayout.SOUTH);

        run.addActionListener(e -> runBenchmark());

        add(p, BorderLayout.CENTER);
        setPreferredSize(new Dimension(820, 640));
    }

    void runBenchmark() {
        final int n = SIZES[sizeCombo.getSelectedIndex()];
        run.setEnabled(false);
        summary.setForeground(Ui.MUTED);
        summary.setText("Measuring " + String.format("%,d", n) + " assignments... Bubble Sort can take a few seconds.");
        new SwingWorker<Benchmark.Result, Void>() {
            @Override protected Benchmark.Result doInBackground() { return Benchmark.measure(n); }
            @Override protected void done() {
                try {
                    result = get();
                    summary.setForeground(Ui.TEXT);
                    summary.setText(buildSummary(result));
                } catch (Exception ex) {
                    summary.setForeground(Ui.HIGH);
                    summary.setText("Benchmark failed: " + ex.getMessage());
                }
                run.setEnabled(true);
                chart.repaint();
            }
        }.execute();
    }

    private static String buildSummary(Benchmark.Result r) {
        return String.format("At n = %,d:  HashMap is about %,.0fx faster than linear search  \u00b7  "
                        + "Merge Sort is about %,.0fx faster than Bubble Sort  \u00b7  "
                        + "heap peek is about %,.0fx faster than sorting everything.",
                r.n, r.linear / Math.max(r.hash, 1e-9), r.bubble / Math.max(r.merge, 1e-9),
                r.sortFirst / Math.max(r.heap, 1e-9));
    }

    private class Chart extends JComponent {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Ui.aa(g2);
            if (result == null) {
                g2.setColor(Ui.MUTED);
                g2.setFont(Ui.font(Font.PLAIN, 14));
                String s = "Press \"Run benchmark\" to measure.";
                g2.drawString(s, (getWidth() - g2.getFontMetrics().stringWidth(s)) / 2, getHeight() / 2);
                g2.dispose();
                return;
            }
            Benchmark.Result r = result;
            String[][] labels = {
                    {"Search by ID", "Linear search  O(n)", "HashMap  O(1)"},
                    {"Sort by deadline", "Bubble Sort  O(n\u00b2)", "Insertion Sort  O(n\u00b2)", "Merge Sort  O(n log n)"},
                    {"Get most urgent assignment", "Sort all, take first", "Heap peek  O(1)"}};
            double[][] values = {{r.linear, r.hash}, {r.bubble, r.insertion, r.merge}, {r.sortFirst, r.heap}};
            Color[][] colors = {{Ui.MED, Ui.LOW}, {Ui.HIGH, Ui.MED, Ui.LOW}, {Ui.MED, Ui.LOW}};

            double min = Double.MAX_VALUE, max = 0;
            for (double[] grp : values) for (double v : grp) {
                min = Math.min(min, Math.max(v, 1e-6));
                max = Math.max(max, v);
            }
            double logMin = Math.log10(min) - 0.4, logMax = Math.log10(max);

            int labelW = 170, valueW = 90, barH = 20, rowGap = 12;
            int trackW = getWidth() - labelW - valueW - 10;
            int y = 4;
            for (int gi = 0; gi < values.length; gi++) {
                g2.setColor(Ui.TEXT);
                g2.setFont(Ui.font(Font.BOLD, 14));
                y += 16;
                g2.drawString(labels[gi][0], 0, y);
                y += 12;
                for (int i = 0; i < values[gi].length; i++) {
                    double v = values[gi][i];
                    double frac = (Math.log10(Math.max(v, 1e-6)) - logMin) / (logMax - logMin);
                    frac = Math.max(0.02, Math.min(1, frac));
                    g2.setFont(Ui.font(Font.PLAIN, 12));
                    g2.setColor(Ui.MUTED);
                    g2.drawString(labels[gi][i + 1], 0, y + 15);
                    g2.setColor(Ui.FIELD);
                    g2.fill(new RoundRectangle2D.Float(labelW, y, trackW, barH, 10, 10));
                    g2.setColor(colors[gi][i]);
                    g2.fill(new RoundRectangle2D.Float(labelW, y, (float) (trackW * frac), barH, 10, 10));
                    g2.setColor(Ui.TEXT);
                    g2.setFont(Ui.font(Font.BOLD, 12));
                    g2.drawString(formatMs(v), labelW + trackW + 10, y + 15);
                    y += barH + rowGap;
                }
                y += 14;
            }
            g2.dispose();
        }

        private String formatMs(double ms) {
            if (ms < 0.001) return String.format("%.0f ns", ms * 1e6);
            if (ms < 1) return String.format("%.1f \u00b5s", ms * 1000);
            return String.format("%,.1f ms", ms);
        }
    }
}
