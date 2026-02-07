package com.todolist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Sidebar panel for creating tasks without using the calendar.
 * Supports single-day and multi-day tasks via start/end date.
 */
public class TaskCreateSidebar extends JPanel {
    private final TodoStore store;
    private final Runnable onTaskAdded;
    private final JTextField titleField;
    private final JSpinner startSpinner;
    private final JCheckBox multiDayCheck;
    private final JSpinner endSpinner;
    private final JButton addButton;

    public TaskCreateSidebar(TodoStore store, Runnable onTaskAdded) {
        this.store = store;
        this.onTaskAdded = onTaskAdded;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(new Color(50, 50, 62));
        setPreferredSize(new Dimension(240, 0));

        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
                " New task ",
                0, 0, null, Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0, 0, 0, 4), border));

        JLabel titleLabel = new JLabel("Title");
        titleLabel.setForeground(Color.WHITE);
        titleField = new JTextField(16);
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel startLabel = new JLabel("Start date");
        startLabel.setForeground(Color.WHITE);
        startSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd");
        startSpinner.setEditor(startEditor);
        startSpinner.setValue(new Date());

        multiDayCheck = new JCheckBox("Multi-day task", false);
        multiDayCheck.setForeground(Color.WHITE);
        multiDayCheck.setBackground(getBackground());
        multiDayCheck.addActionListener(e -> {
            boolean multi = multiDayCheck.isSelected();
            endSpinner.setEnabled(multi);
            if (multi) endSpinner.setValue(startSpinner.getValue());
        });

        JLabel endLabel = new JLabel("End date");
        endLabel.setForeground(Color.WHITE);
        endSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd");
        endSpinner.setEditor(endEditor);
        endSpinner.setValue(new Date());
        endSpinner.setEnabled(false);

        addButton = new JButton("Add task");
        addButton.addActionListener(e -> addTask());

        titleField.addActionListener(e -> addTask());

        add(Box.createVerticalStrut(4));
        add(titleLabel);
        add(Box.createVerticalStrut(2));
        add(titleField);
        add(Box.createVerticalStrut(10));
        add(startLabel);
        add(Box.createVerticalStrut(2));
        add(startSpinner);
        add(Box.createVerticalStrut(8));
        add(multiDayCheck);
        add(Box.createVerticalStrut(4));
        add(endLabel);
        add(Box.createVerticalStrut(2));
        add(endSpinner);
        add(Box.createVerticalStrut(14));
        add(addButton);
        add(Box.createVerticalGlue());
    }

    private static LocalDate toLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void addTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            titleField.requestFocusInWindow();
            return;
        }
        LocalDate start = toLocalDate((Date) startSpinner.getValue());
        boolean multiDay = multiDayCheck.isSelected();
        LocalDate end = multiDay ? toLocalDate((Date) endSpinner.getValue()) : start;
        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this, "End date must be on or after start date.", "Invalid dates", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TodoItem item = new TodoItem(TodoStore.generateId(), start, end, title, false);
        store.add(item);
        try {
            store.save();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        titleField.setText("");
        titleField.requestFocusInWindow();
        if (onTaskAdded != null) onTaskAdded.run();
    }
}
