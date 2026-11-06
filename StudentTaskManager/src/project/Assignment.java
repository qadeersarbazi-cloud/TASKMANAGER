package project;

import java.time.LocalDate;

/** One assignment: ID, name, subject, deadline and priority (1 = High, 2 = Medium, 3 = Low). */
public class Assignment {
    private final int id;
    private String name;
    private String subject;
    private LocalDate deadline;
    private int priority;

    public Assignment(int id, String name, String subject, LocalDate deadline, int priority) {
        this.id = id;
        this.name = name;
        this.subject = subject;
        this.deadline = deadline;
        this.priority = priority;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSubject() { return subject; }
    public LocalDate getDeadline() { return deadline; }
    public int getPriority() { return priority; }

    public void setName(String name) { this.name = name; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public void setPriority(int priority) { this.priority = priority; }

    public String priorityName() {
        return priority == 1 ? "High" : priority == 2 ? "Medium" : "Low";
    }

    @Override
    public String toString() {
        return String.format("%-5d %-25s %-18s %-12s %s", id, name, subject, deadline, priorityName());
    }

    public static String header() {
        return String.format("%-5s %-25s %-18s %-12s %s", "ID", "Name", "Subject", "Deadline", "Priority");
    }
}
