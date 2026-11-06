package project;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/** Theme colours and small custom-painted Swing components used by the GUI. */
final class Ui {
    private Ui() {}

    static final Color BG        = new Color(0x0E1018);
    static final Color SIDEBAR   = new Color(0x131622);
    static final Color CARD      = new Color(0x1A1E2E);
    static final Color CARD_ALT  = new Color(0x1E2335);
    static final Color FIELD     = new Color(0x141829);
    static final Color BORDER    = new Color(0x2A3047);
    static final Color TEXT      = new Color(0xECEEF6);
    static final Color MUTED     = new Color(0x8D94AD);
    static final Color ACCENT    = new Color(0x6C63FF);
    static final Color ACCENT_HI = new Color(0x8279FF);
    static final Color PINK      = new Color(0xFF6AC1);
    static final Color HIGH      = new Color(0xFF5C7A);
    static final Color MED       = new Color(0xFFB84D);
    static final Color LOW       = new Color(0x3DD6A0);
    static final Color SELECT    = new Color(0x2B2E5A);
    static final Color GHOST     = new Color(0x232842);
    static final Color GHOST_HI  = new Color(0x2E3558);

    static Font font(int style, float size) {
        return new Font("Segoe UI", style, 12).deriveFont(style, size);
    }

    static void aa(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), a); }

    static Color priorityColor(int p) { return p == 1 ? HIGH : p == 2 ? MED : LOW; }

    static JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    static String esc(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ------------------------------------------------------------------ panels

    static class RoundedPanel extends JPanel {
        private final Color fill, border;
        private final int arc;

        RoundedPanel(Color fill, Color border, int arc, LayoutManager lm) {
            super(lm);
            this.fill = fill; this.border = border; this.arc = arc;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
            if (border != null) {
                g2.setColor(border);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arc, arc));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Gradient app logo with a check mark. */
    static class Logo extends JComponent {
        Logo() { setPreferredSize(new Dimension(42, 42)); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setPaint(new GradientPaint(0, 0, ACCENT, getWidth(), getHeight(), PINK));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int w = getWidth(), h = getHeight();
            g2.drawPolyline(new int[]{w * 28 / 100, w * 45 / 100, w * 74 / 100},
                            new int[]{h * 52 / 100, h * 68 / 100, h * 34 / 100}, 3);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ buttons

    static class RoundedButton extends JButton {
        private final Color base, hover, fg;
        private Color activeColor;
        private boolean active;

        RoundedButton(String text, Color base, Color hover, Color fg) {
            super(text);
            this.base = base; this.hover = hover; this.fg = fg;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFont(font(Font.BOLD, 13));
            setForeground(fg);
            setBorder(new EmptyBorder(10, 18, 10, 18));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        static RoundedButton primary(String t) { return new RoundedButton(t, ACCENT, ACCENT_HI, Color.WHITE); }
        static RoundedButton ghost(String t)   { return new RoundedButton(t, GHOST, GHOST_HI, TEXT); }
        static RoundedButton danger(String t)  { return new RoundedButton(t, HIGH, HIGH.brighter(), Color.WHITE); }

        void setActiveColor(Color c) { activeColor = c; }

        void setActive(boolean a) {
            active = a;
            setForeground(a ? new Color(0x10121A) : fg);
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            Color c = base;
            if (active && activeColor != null) c = activeColor;
            else if (getModel().isPressed()) c = base.darker();
            else if (getModel().isRollover()) c = hover;
            if (!isEnabled()) c = alpha(base, 90);
            g2.setColor(c);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
            if (!isEnabled()) setForeground(alpha(fg, 110)); else if (!active) setForeground(fg);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ inputs

    static class StyledTextField extends JTextField {
        private final String placeholder;

        StyledTextField(String placeholder, int cols) {
            super(cols);
            this.placeholder = placeholder;
            setOpaque(false);
            setForeground(TEXT);
            setCaretColor(ACCENT_HI);
            setSelectionColor(ACCENT);
            setSelectedTextColor(Color.WHITE);
            setFont(font(Font.PLAIN, 14));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setPreferredSize(new Dimension(220, 42));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(FIELD);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            g2.setColor(hasFocus() ? ACCENT : BORDER);
            g2.setStroke(new BasicStroke(hasFocus() ? 1.6f : 1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 14, 14));
            super.paintComponent(g2);
            if (getText().isEmpty() && placeholder != null) {
                g2.setColor(alpha(MUTED, 170));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(placeholder, getInsets().left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    static class StyledCombo extends JComboBox<String> {
        StyledCombo(String... items) {
            super(items);
            setOpaque(false);
            setFont(font(Font.PLAIN, 13));
            setForeground(TEXT);
            setPreferredSize(new Dimension(180, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setUI(new BasicComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    JButton b = new JButton() {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            aa(g2);
                            g2.setColor(MUTED);
                            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                            int cx = getWidth() / 2, cy = getHeight() / 2;
                            g2.drawPolyline(new int[]{cx - 4, cx, cx + 4}, new int[]{cy - 2, cy + 2, cy - 2}, 3);
                            g2.dispose();
                        }
                    };
                    b.setBorder(null);
                    b.setContentAreaFilled(false);
                    b.setFocusPainted(false);
                    b.setPreferredSize(new Dimension(30, 10));
                    return b;
                }

                @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) { }

                @Override protected ComboPopup createPopup() {
                    BasicComboPopup p = new BasicComboPopup(comboBox);
                    p.setBorder(new LineBorder(BORDER));
                    p.getList().setBackground(CARD);
                    p.getList().setForeground(TEXT);
                    p.getList().setSelectionBackground(SELECT);
                    p.getList().setSelectionForeground(TEXT);
                    return p;
                }
            });
            setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                        boolean sel, boolean focus) {
                    JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, false);
                    l.setFont(font(Font.PLAIN, 13));
                    l.setForeground(TEXT);
                    l.setBorder(new EmptyBorder(8, 14, 8, 10));
                    if (index == -1) { l.setOpaque(false); }
                    else { l.setOpaque(true); l.setBackground(sel ? SELECT : CARD); }
                    return l;
                }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(FIELD);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 14, 14));
            g2.setColor(BORDER);
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 2, getHeight() - 2, 14, 14));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    /** Small coloured pill (used for priority). */
    static class Pill extends JLabel {
        private Color color;

        Pill(String text, Color color) {
            super(text);
            this.color = color;
            setFont(font(Font.BOLD, 11));
            setOpaque(false);
        }

        void set(String text, Color c) { setText(text); color = c; }

        @Override public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(getText()) + 28, 24);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(alpha(color, 45));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()));
            g2.setColor(color);
            g2.fillOval(9, getHeight() / 2 - 3, 6, 6);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), 21, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------ scrolling

    static class ScrollUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { thumbColor = BORDER; trackColor = CARD; }
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b; }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            aa(g2);
            g2.setColor(isThumbRollover() ? MUTED : BORDER);
            g2.fill(new RoundRectangle2D.Float(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8));
            g2.dispose();
        }
        @Override public Dimension getPreferredSize(JComponent c) { return new Dimension(11, 11); }
    }

    static void styleScroll(JScrollPane sp, Color bg) {
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setViewportBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setBackground(bg);
        sp.getVerticalScrollBar().setUI(new ScrollUI());
        sp.getVerticalScrollBar().setOpaque(false);
    }

    // ------------------------------------------------------------------ dialogs

    static boolean confirm(Component parent, String title, String message, String okText) {
        Window w = SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(w, title, Dialog.ModalityType.APPLICATION_MODAL);
        final boolean[] result = {false};

        JPanel p = new JPanel(new BorderLayout(0, 20));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(24, 26, 18, 26));
        JLabel l = new JLabel("<html><body style='width:300px'>" + esc(message) + "</body></html>");
        l.setFont(font(Font.PLAIN, 14));
        l.setForeground(TEXT);
        p.add(l, BorderLayout.CENTER);

        RoundedButton cancel = RoundedButton.ghost("Cancel");
        RoundedButton ok = RoundedButton.danger(okText);
        cancel.addActionListener(e -> d.dispose());
        ok.addActionListener(e -> { result[0] = true; d.dispose(); });
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);
        row.add(cancel); row.add(ok);
        p.add(row, BorderLayout.SOUTH);

        d.setContentPane(p);
        d.getRootPane().setDefaultButton(ok);
        d.pack();
        d.setResizable(false);
        d.setLocationRelativeTo(w);
        d.setVisible(true);
        return result[0];
    }
}
