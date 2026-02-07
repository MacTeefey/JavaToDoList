package com.todolist;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * In-memory store for todos with file persistence.
 * File format: one line per item: id|date|title|completed
 */
public class TodoStore {
    private final Path filePath;
    private final List<TodoItem> items = new CopyOnWriteArrayList<>();

    public TodoStore() {
        this.filePath = Paths.get(System.getProperty("user.home"), ".calendar-todolist", "todos.txt");
    }

    public TodoStore(Path filePath) {
        this.filePath = filePath;
    }

    public void load() throws IOException {
        items.clear();
        if (!Files.exists(filePath)) return;
        try (var reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                TodoItem item = parseLine(line);
                if (item != null) items.add(item);
            }
        }
    }

    public void save() throws IOException {
        Path dir = filePath.getParent();
        if (dir != null && !Files.exists(dir)) Files.createDirectories(dir);
        try (var writer = Files.newBufferedWriter(filePath)) {
            for (TodoItem item : items) {
                writer.write(formatLine(item));
                writer.newLine();
            }
        }
    }

    private static String formatLine(TodoItem item) {
        return item.getId() + "|" + item.getDate() + "|" + escape(item.getTitle()) + "|" + item.isCompleted();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("|", "\u2016").replace("\n", " ");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\u2016", "|");
    }

    private static TodoItem parseLine(String line) {
        int a = line.indexOf('|');
        if (a < 0) return null;
        int b = line.indexOf('|', a + 1);
        if (b < 0) return null;
        int c = line.indexOf('|', b + 1);
        if (c < 0) return null;
        String id = line.substring(0, a);
        LocalDate date;
        try {
            date = LocalDate.parse(line.substring(a + 1, b));
        } catch (Exception e) {
            return null;
        }
        String title = unescape(line.substring(b + 1, c));
        boolean completed = "true".equalsIgnoreCase(line.substring(c + 1).trim());
        return new TodoItem(id, date, title, completed);
    }

    public List<TodoItem> getItemsFor(LocalDate date) {
        return items.stream()
                .filter(i -> i.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<TodoItem> getAllItems() {
        return new ArrayList<>(items);
    }

    public void add(TodoItem item) {
        items.add(item);
    }

    public void remove(TodoItem item) {
        items.remove(item);
    }

    public void removeById(String id) {
        items.removeIf(i -> i.getId().equals(id));
    }

    public Optional<TodoItem> getById(String id) {
        return items.stream().filter(i -> i.getId().equals(id)).findFirst();
    }

    /** Completed count for the given date. */
    public long completedCount(LocalDate date) {
        return items.stream()
                .filter(i -> i.getDate().equals(date) && i.isCompleted())
                .count();
    }

    /** Total count for the given date. */
    public long totalCount(LocalDate date) {
        return items.stream().filter(i -> i.getDate().equals(date)).count();
    }

    /** Percent completed for the day (0–100). If no items, returns 0. */
    public int percentCompleted(LocalDate date) {
        long total = totalCount(date);
        if (total == 0) return 0;
        long done = completedCount(date);
        return (int) Math.round(100.0 * done / total);
    }

    public static String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
