package com.todolist;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A single todo item with a date (and optional end date for multi-day tasks),
 * title, and completion status. For multi-day tasks, completing the item marks
 * the whole range as completed.
 */
public class TodoItem {
    private final String id;
    private final LocalDate date;
    /** End date (inclusive). If null or equals date, single-day task. */
    private final LocalDate endDate;
    private String title;
    private boolean completed;

    /** Single-day task. */
    public TodoItem(String id, LocalDate date, String title, boolean completed) {
        this(id, date, date, title, completed);
    }

    /** Multi-day task from date (inclusive) to endDate (inclusive). */
    public TodoItem(String id, LocalDate date, LocalDate endDate, String title, boolean completed) {
        this.id = Objects.requireNonNull(id);
        this.date = Objects.requireNonNull(date);
        this.endDate = endDate != null ? endDate : date;
        if (this.endDate.isBefore(this.date)) throw new IllegalArgumentException("endDate must be >= date");
        this.title = title != null ? title : "";
        this.completed = completed;
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    /** End date (inclusive). Same as getDate() for single-day tasks. */
    public LocalDate getEndDate() { return endDate; }
    /** True if this task spans more than one day. */
    public boolean isMultiDay() { return !endDate.equals(date); }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title != null ? title : ""; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
