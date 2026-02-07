package com.todolist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Popup dialog showing the todo list for a single day. Add new items and toggle completion.
 */
public class DayTodoDialog extends JDialog {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy");

    private final LocalDate date;
    private final TodoStore store;
    private final Runnable onUpdate;
    private final JPanel listPanel;
    private final JTextField newTodoField;

    public DayTodoDialog(Frame owner, LocalDate date, TodoStore store, Runnable onUpdate) {
        super(owner, "Todos – " + date.format(DATE_FMT), false);
        this.date = date;
        this.store = store;
        this.onUpdate = onUpdate;
        this.listPanel = new JPanel();
        this.listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        this.newTodoField = new JTextField(30);

        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel titleLabel = new JLabel(date.format(DATE_FMT));
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 14f));
        add(titleLabel, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(380, 220));
        add(scroll, BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        addPanel.add(new JLabel("Add:"));
        addPanel.add(newTodoField);
        JButton addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addNewTodo());
        newTodoField.addActionListener(e -> addNewTodo());
        addPanel.add(addBtn);
        add(addPanel, BorderLayout.SOUTH);

        refreshList();
        pack();
        setLocationRelativeTo(owner);
    }

    private void addNewTodo() {
        String text = newTodoField.getText().trim();
        if (text.isEmpty()) return;
        TodoItem item = new TodoItem(TodoStore.generateId(), date, text, false);
        store.add(item);
        try { store.save(); } catch (Exception ex) { showError(ex); }
        newTodoField.setText("");
        refreshList();
        if (onUpdate != null) onUpdate.run();
    }

    private void refreshList() {
        listPanel.removeAll();
        List<TodoItem> items = store.getItemsFor(date);
        for (TodoItem item : items) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            JCheckBox check = new JCheckBox(item.getTitle(), item.isCompleted());
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
            listPanel.add(new JLabel("No todos for this day. Add one above."));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
