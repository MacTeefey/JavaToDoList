package com.todolist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Main application window: calendar with month navigation and percent completed per day.
 * Click a day to open the todo popup for that day.
 */
public class CalendarTodoApp extends JFrame {
    private static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("MMMM yyyy");

    private final TodoStore store;
    private final CalendarPanel calendarPanel;
    private final JLabel monthLabel;

    public CalendarTodoApp() {
        setTitle("Calendar Todo List");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        store = new TodoStore();
        try {
            store.load();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load todos: " + e.getMessage());
        }

        calendarPanel = new CalendarPanel(store);
        calendarPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        calendarPanel.refreshPercentages();

        TaskCreateSidebar sidebar = new TaskCreateSidebar(store, calendarPanel::refreshPercentages);

        monthLabel = new JLabel(calendarPanel.getCurrentMonth().format(MONTH_YEAR));
        monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 18f));
        monthLabel.setBorder(new EmptyBorder(0, 0, 8, 0));

        JButton prev = new JButton("< Prev");
        JButton next = new JButton("Next >");
        prev.addActionListener(e -> moveMonth(-1));
        next.addActionListener(e -> moveMonth(1));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(8, 8, 4, 8));
        top.add(monthLabel, BorderLayout.CENTER);
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        nav.add(prev);
        nav.add(next);
        top.add(nav, BorderLayout.EAST);

        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(45, 45, 55));
        add(top, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(calendarPanel, BorderLayout.CENTER);

        JLabel hint = new JLabel("Left: add tasks (single or multi-day). Click a day to edit that day's todos. Shift+click a range for bulk options.");
        hint.setForeground(Color.GRAY);
        hint.setBorder(new EmptyBorder(4, 12, 8, 12));
        add(hint, BorderLayout.SOUTH);

        setSize(780, 420);
        setLocationRelativeTo(null);
    }

    private void moveMonth(int delta) {
        YearMonth next = calendarPanel.getCurrentMonth().plusMonths(delta);
        calendarPanel.setMonth(next);
        monthLabel.setText(next.format(MONTH_YEAR));
        calendarPanel.refreshPercentages();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> {
            CalendarTodoApp app = new CalendarTodoApp();
            app.setVisible(true);
        });
    }
}
