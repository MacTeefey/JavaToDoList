package com.todolist;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A single todo item with a date, title, and completion status.
 */
public class TodoItem {
    private final String id;
    private final LocalDate date;
    private String title;
    private boolean completed;

    public TodoItem(String id, LocalDate date, String title, boolean completed) {
        this.id = Objects.requireNonNull(id);
        this.date = Objects.requireNonNull(date);
        this.title = title != null ? title : "";
        this.completed = completed;
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title != null ? title : ""; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
