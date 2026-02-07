package com.todolist;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Calendar grid for a month. Each day cell shows the day number and percent completed.
 * Cell background uses a dull gradient by completion: 0% red → 25% orange → 50% yellow → 75% green → 100% blue; no tasks = grey.
 */
public class CalendarPanel extends JPanel {
    private static final String[] WEEK_HEADERS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

    /** Grey for days with no tasks. */
    private static final Color NO_TASKS_COLOR = new Color(55, 55, 70);
    /** Dull gradient stops: 0% red, 25% orange, 50% yellow, 75% green, 100% blue. */
    private static final Color[] GRADIENT_STOPS = {
            new Color(140, 65, 60),   // 0% red
            new Color(165, 110, 65),  // 25% orange
            new Color(165, 145, 75),  // 50% yellow
            new Color(70, 125, 85),   // 75% green
            new Color(60, 85, 130),   // 100% blue
    };

    /** Interpolate between gradient stops by completion percent (0–100). */
    private static Color colorForPercent(int percent) {
        if (percent <= 0) return GRADIENT_STOPS[0];
        if (percent >= 100) return GRADIENT_STOPS[4];
        double p = percent / 100.0;           // 0.0 .. 1.0
        double segment = p * 4.0;              // 0..4 over the 5 stops
        int i = (int) segment;
        if (i >= 4) return GRADIENT_STOPS[4];
        float t = (float) (segment - i);
        return lerp(GRADIENT_STOPS[i], GRADIENT_STOPS[i + 1], t);
    }

    private static Color lerp(Color a, Color b, float t) {
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, bl)));
    }

    private static Color brighten(Color c, float factor) {
        int r = Math.min(255, (int) (c.getRed() * factor + 20));
        int g = Math.min(255, (int) (c.getGreen() * factor + 20));
        int b = Math.min(255, (int) (c.getBlue() * factor + 20));
        return new Color(r, g, b);
    }

    private YearMonth currentMonth;
    private final TodoStore store;
    private final List<DayCell> dayCells = new ArrayList<>();
    private Runnable onDaySelected;
    /** Anchor for range selection (first click). */
    private LocalDate anchorDate;
    /** End of range when shift+click (inclusive). */
    private LocalDate rangeEndDate;

    public CalendarPanel(TodoStore store) {
        this.store = store;
        this.currentMonth = YearMonth.now();
        setLayout(new BorderLayout(5, 5));
        buildUI();
    }

    public void setOnDaySelected(Runnable onDaySelected) {
        this.onDaySelected = onDaySelected;
    }

    private void buildUI() {
        removeAll();
        dayCells.clear();

        JPanel header = new JPanel(new GridLayout(1, 7));
        header.setBackground(new Color(60, 60, 80));
        for (String w : WEEK_HEADERS) {
            JLabel l = new JLabel(w, SwingConstants.CENTER);
            l.setForeground(Color.WHITE);
            l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
            header.add(l);
        }
        add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        grid.setBackground(new Color(45, 45, 55));

        LocalDate first = currentMonth.atDay(1);
        int startOffset = first.getDayOfWeek().getValue() - 1; // Monday = 0
        int daysInMonth = currentMonth.lengthOfMonth();

        // Empty cells before first day
        for (int i = 0; i < startOffset; i++) {
            grid.add(new JLabel());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            DayCell cell = new DayCell(date);
            dayCells.add(cell);
            grid.add(cell);
        }

        add(grid, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void setMonth(YearMonth month) {
        this.currentMonth = month;
        buildUI();
    }

    public YearMonth getCurrentMonth() {
        return currentMonth;
    }

    public void refreshPercentages() {
        for (DayCell cell : dayCells) {
            LocalDate d = cell.getDate();
            int total = (int) store.totalCount(d);
            int percent = store.percentCompleted(d);
            cell.updatePercent(total, percent);
        }
    }

    public void setSelectedDate(LocalDate date) {
        this.anchorDate = date;
        this.rangeEndDate = null;
        updateSelectionHighlight();
    }

    /** Updates which cells appear selected (single date or range). */
    private void updateSelectionHighlight() {
        LocalDate from = anchorDate;
        LocalDate to = rangeEndDate != null ? rangeEndDate : anchorDate;
        if (from != null && to != null) {
            LocalDate start = from.isBefore(to) ? from : to;
            LocalDate end = from.isBefore(to) ? to : from;
            for (DayCell cell : dayCells) {
                LocalDate d = cell.getDate();
                cell.setSelected(!d.isBefore(start) && !d.isAfter(end));
            }
        } else if (anchorDate != null) {
            for (DayCell cell : dayCells) {
                cell.setSelected(cell.getDate().equals(anchorDate));
            }
        }
    }

    private class DayCell extends JPanel {
        private final LocalDate date;
        private final JLabel dayLabel;
        private final JLabel percentLabel;
        private boolean selected;
        /** Background for normal state: grey when no tasks, gradient color by completion when has tasks. */
        private Color baseBackground = NO_TASKS_COLOR;

        DayCell(LocalDate date) {
            this.date = date;
            setLayout(new BorderLayout(2, 2));
            setBorder(new LineBorder(new Color(70, 70, 90), 1));
            setBackground(NO_TASKS_COLOR);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 14f));
            dayLabel.setForeground(Color.WHITE);
            add(dayLabel, BorderLayout.NORTH);

            percentLabel = new JLabel("", SwingConstants.CENTER);
            percentLabel.setFont(percentLabel.getFont().deriveFont(10f));
            percentLabel.setForeground(new Color(180, 220, 180));
            add(percentLabel, BorderLayout.CENTER);

            updatePercent((int) store.totalCount(date), store.percentCompleted(date));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    boolean shift = (e.getModifiersEx() & java.awt.event.InputEvent.SHIFT_DOWN_MASK) != 0;
                    if (shift && anchorDate != null) {
                        rangeEndDate = date;
                        updateSelectionHighlight();
                        if (onDaySelected != null) onDaySelected.run();
                        LocalDate start = anchorDate.isBefore(date) ? anchorDate : date;
                        LocalDate end = anchorDate.isBefore(date) ? date : anchorDate;
                        DayTodoDialog dialog = new DayTodoDialog(
                                (Frame) SwingUtilities.getWindowAncestor(CalendarPanel.this),
                                start, end, store, CalendarPanel.this::refreshPercentages);
                        dialog.setVisible(true);
                    } else {
                        anchorDate = date;
                        rangeEndDate = null;
                        updateSelectionHighlight();
                        if (onDaySelected != null) onDaySelected.run();
                        DayTodoDialog dialog = new DayTodoDialog(
                                (Frame) SwingUtilities.getWindowAncestor(CalendarPanel.this),
                                date, null, store, CalendarPanel.this::refreshPercentages);
                        dialog.setVisible(true);
                    }
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!selected) setBackground(brighten(baseBackground, 1.15f));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!selected) setBackground(baseBackground);
                }
            });
        }

        LocalDate getDate() {
            return date;
        }

        void updatePercent(int totalCount, int percent) {
            if (totalCount == 0) {
                baseBackground = NO_TASKS_COLOR;
                percentLabel.setText("");
            } else {
                baseBackground = colorForPercent(percent);
                percentLabel.setText(percent + "%");
            }
            if (!selected) setBackground(baseBackground);
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            setBackground(selected ? new Color(80, 100, 140) : baseBackground);
        }
    }
}
