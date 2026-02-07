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
 */
public class CalendarPanel extends JPanel {
    private static final String[] WEEK_HEADERS = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

    private YearMonth currentMonth;
    private final TodoStore store;
    private final List<DayCell> dayCells = new ArrayList<>();
    private Runnable onDaySelected;

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
            cell.updatePercent(store.percentCompleted(cell.getDate()));
        }
    }

    public void setSelectedDate(LocalDate date) {
        for (DayCell cell : dayCells) {
            cell.setSelected(cell.getDate().equals(date));
        }
    }

    private class DayCell extends JPanel {
        private final LocalDate date;
        private final JLabel dayLabel;
        private final JLabel percentLabel;
        private boolean selected;

        DayCell(LocalDate date) {
            this.date = date;
            setLayout(new BorderLayout(2, 2));
            setBorder(new LineBorder(new Color(70, 70, 90), 1));
            setBackground(new Color(55, 55, 70));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), SwingConstants.CENTER);
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD, 14f));
            dayLabel.setForeground(Color.WHITE);
            add(dayLabel, BorderLayout.NORTH);

            percentLabel = new JLabel("", SwingConstants.CENTER);
            percentLabel.setFont(percentLabel.getFont().deriveFont(10f));
            percentLabel.setForeground(new Color(180, 220, 180));
            add(percentLabel, BorderLayout.CENTER);

            updatePercent(store.percentCompleted(date));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    setSelectedDate(date);
                    if (onDaySelected != null) onDaySelected.run();
                    DayTodoDialog dialog = new DayTodoDialog(
                            (Frame) SwingUtilities.getWindowAncestor(CalendarPanel.this),
                            date, store, CalendarPanel.this::refreshPercentages);
                    dialog.setVisible(true);
                }

                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!selected) setBackground(new Color(65, 65, 85));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!selected) setBackground(new Color(55, 55, 70));
                }
            });
        }

        LocalDate getDate() {
            return date;
        }

        void updatePercent(int percent) {
            if (percent <= 0) {
                percentLabel.setText("");
            } else {
                percentLabel.setText(percent + "%");
            }
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            setBackground(selected ? new Color(80, 100, 140) : new Color(55, 55, 70));
        }
    }
}
