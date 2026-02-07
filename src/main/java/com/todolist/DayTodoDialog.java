package com.todolist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Popup dialog showing the todo list for a single day or a date range.
 * Single day: add items for that day; multi-day tasks show and completing them marks the whole range.
 * Range: add "one task per day" or "one multi-day task" for the range; completing a multi-day task
 * marks all days in its range as completed.
 */
public class DayTodoDialog extends JDialog {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");
    private static final DateTimeFormatter SHORT_FMT = DateTimeFormatter.ofPattern("MMM d");

    private final LocalDate dateFrom;
    private final LocalDate dateTo; // null or same as dateFrom = single day
    private final TodoStore store;
    private final Runnable onUpdate;
    private final JPanel listPanel;
    private final JTextField newTodoField;

    /** Single-day dialog. */
    public DayTodoDialog(Frame owner, LocalDate date, TodoStore store, Runnable onUpdate) {
        this(owner, date, null, store, onUpdate);
    }

    /** Range dialog when dateTo != null and !dateTo.equals(dateFrom). */
    public DayTodoDialog(Frame owner, LocalDate dateFrom, LocalDate dateTo, TodoStore store, Runnable onUpdate) {
        super(owner, "", false);
        this.dateFrom = dateFrom;
        this.dateTo = (dateTo == null || dateTo.equals(dateFrom)) ? null : dateTo;
        this.store = store;
        this.onUpdate = onUpdate;
        this.listPanel = new JPanel();
        this.listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        this.newTodoField = new JTextField(28);

        boolean rangeMode = isRangeMode();
        setTitle(rangeMode ? "Todos – " + dateFrom.format(SHORT_FMT) + " – " + this.dateTo.format(SHORT_FMT)
                : "Todos – " + dateFrom.format(DATE_FMT));

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel(rangeMode
                ? dateFrom.format(DATE_FMT) + "  →  " + this.dateTo.format(DATE_FMT)
                : dateFrom.format(DATE_FMT));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(titleLabel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(420, 240));
        add(scroll, BorderLayout.CENTER);

        JPanel addPanel = buildAddPanel(rangeMode);
        add(addPanel, BorderLayout.SOUTH);

        refreshList();
        pack();
        setLocationRelativeTo(owner);
    }

    private boolean isRangeMode() {
        return dateTo != null && !dateTo.equals(dateFrom);
    }

    private JPanel buildAddPanel(boolean rangeMode) {
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        row1.add(new JLabel("Add:"));
        row1.add(newTodoField);
        if (!rangeMode) {
            JButton addBtn = new JButton("Add");
            addBtn.addActionListener(e -> addNewTodoSingle());
            newTodoField.addActionListener(e -> addNewTodoSingle());
            row1.add(addBtn);
        }
        addPanel.add(row1);

        if (rangeMode) {
            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            JButton addOnePerDay = new JButton("Add same task to each day");
            addOnePerDay.addActionListener(e -> addTaskToEachDay());
            JButton addMultiDay = new JButton("Add one multi-day task");
            addMultiDay.addActionListener(e -> addMultiDayTask());
            newTodoField.addActionListener(e -> addTaskToEachDay());
            row2.add(addOnePerDay);
            row2.add(addMultiDay);
            addPanel.add(row2);
        }

        return addPanel;
    }

    private void addNewTodoSingle() {
        String text = newTodoField.getText().trim();
        if (text.isEmpty()) return;
        TodoItem item = new TodoItem(TodoStore.generateId(), dateFrom, text, false);
        store.add(item);
        try { store.save(); } catch (Exception ex) { showError(ex); }
        newTodoField.setText("");
        refreshList();
        if (onUpdate != null) onUpdate.run();
    }

    /** Create one task per day in the range (same title). */
    private void addTaskToEachDay() {
        String text = newTodoField.getText().trim();
        if (text.isEmpty()) return;
        LocalDate d = dateFrom;
        while (!d.isAfter(dateTo)) {
            TodoItem item = new TodoItem(TodoStore.generateId(), d, text, false);
            store.add(item);
            d = d.plusDays(1);
        }
        try { store.save(); } catch (Exception ex) { showError(ex); }
        newTodoField.setText("");
        refreshList();
        if (onUpdate != null) onUpdate.run();
    }

    /** Create one multi-day task spanning the range. Completing it marks all days completed. */
    private void addMultiDayTask() {
        String text = newTodoField.getText().trim();
        if (text.isEmpty()) return;
        TodoItem item = new TodoItem(TodoStore.generateId(), dateFrom, dateTo, text, false);
        store.add(item);
        try { store.save(); } catch (Exception ex) { showError(ex); }
        newTodoField.setText("");
        refreshList();
        if (onUpdate != null) onUpdate.run();
    }

    private void refreshList() {
        listPanel.removeAll();
        List<TodoItem> items = isRangeMode()
                ? store.getItemsInRange(dateFrom, dateTo)
                : store.getItemsFor(dateFrom);

        for (TodoItem item : items) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            String label = item.getTitle();
            if (item.isMultiDay()) {
                label += " (" + item.getDate().format(SHORT_FMT) + " – " + item.getEndDate().format(SHORT_FMT) + ")";
            }
            JCheckBox check = new JCheckBox(label, item.isCompleted());
            check.addActionListener(e -> {
                item.setCompleted(check.isSelected());
                try { store.save(); } catch (Exception ex) { showError(ex); }
                if (onUpdate != null) onUpdate.run();
            });
            row.add(check);
            JButton del = new JButton("X");
            del.setMargin(new Insets(0, 4, 0, 4));
            del.addActionListener(e -> {
                store.remove(item);
                try { store.save(); } catch (Exception ex) { showError(ex); }
                refreshList();
                if (onUpdate != null) onUpdate.run();
            });
            row.add(del);
            listPanel.add(row);
        }
        if (items.isEmpty()) {
            listPanel.add(new JLabel(isRangeMode()
                    ? "No todos in this range. Add one above (per day or multi-day)."
                    : "No todos for this day. Add one above."));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
