# Calendar Todo List

A Java Swing desktop application: calendar view with a todo list per day and **percent completed** shown on each day. Click a day to open a popup and add or complete todos.

## Features

- **Calendar view** – Month grid with Mon–Sun headers; navigate with Prev/Next.
- **Percent per day** – Each day cell shows completion (e.g. `50%` when 1 of 2 tasks is done).
- **Day popup** – Click a day to open a dialog: add todos, check/uncheck, delete.
- **Persistence** – Todos are saved to `~/.calendar-todolist/todos.txt` and reloaded on startup.

## Requirements

- Java 17 or later

## Build and run

```bash
mvn compile
mvn exec:java -Dexec.mainClass="com.todolist.CalendarTodoApp"
```

Or create a runnable JAR:

```bash
mvn package
java -jar target/calendar-todolist-1.0.0.jar
```

(Note: The default JAR does not include a classpath for dependencies; this project has no external dependencies, so `mvn package` produces a JAR that can be run with `java -jar` only if the manifest main class is set. The pom already sets the main class; for a fat JAR you could add the Maven Assembly or Shade plugin.)

To run the app from your IDE, run the main method in `com.todolist.CalendarTodoApp`.

## Usage

1. Launch the app. The current month is shown with day numbers and completion percentages.
2. Click any day. A popup shows that day’s todos.
3. In the popup: type in “Add” and press Enter or click Add; check/uncheck items; click × to remove.
4. Close the popup. The calendar updates the percent for that day (e.g. 2/4 = 50%).
