package project;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/** Console menu for the Student Task Manager. */
public class Main {
    private static final Scanner in = new Scanner(System.in);
    private static final TaskManager manager = new TaskManager();
    private static final FileStorage storage = new FileStorage("assignments.txt");

    public static void main(String[] args) {
        try {
            int n = storage.load(manager);
            System.out.println("Loaded " + n + " assignment(s) from " + storage.path());
        } catch (IOException e) {
            System.out.println("Could not read file: " + e.getMessage());
        }

        boolean running = true;
        while (running) {
            printMenu();
            switch (readInt("Choose: ")) {
                case 1:  addAssignment(); break;
                case 2:  updateAssignment(); break;
                case 3:  deleteAssignment(); break;
                case 4:  show(manager.getAll()); break;
                case 5:  searchByName(); break;
                case 6:  searchById(true); break;
                case 7:  searchById(false); break;
                case 8:  sortByDeadline(); break;
                case 9:  showOne("Most urgent (earliest deadline)", manager.mostUrgent()); break;
                case 10: showOne("Highest priority", manager.highestPriority()); break;
                case 11: Benchmark.run(); break;
                case 12: save(); break;
                case 0:  save(); running = false; break;
                default: System.out.println("Invalid choice.");
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n===== Student Task Manager =====");
        System.out.println(" 1. Add assignment");
        System.out.println(" 2. Update assignment");
        System.out.println(" 3. Delete assignment");
        System.out.println(" 4. Display all");
        System.out.println(" 5. Search by name (linear search)");
        System.out.println(" 6. Search by ID (HashMap)");
        System.out.println(" 7. Search by ID (linear search)");
        System.out.println(" 8. Sort by deadline (Bubble / Insertion / Merge)");
        System.out.println(" 9. Most urgent assignment (heap)");
        System.out.println("10. Highest priority assignment (heap)");
        System.out.println("11. Run Big-O benchmark");
        System.out.println("12. Save to file");
        System.out.println(" 0. Save and exit");
    }

    private static void addAssignment() {
        String name = readText("Name: ");
        String subject = readText("Subject: ");
        LocalDate deadline = readDate("Deadline (yyyy-MM-dd): ");
        int priority = readPriority();
        Assignment a = manager.add(name, subject, deadline, priority);
        System.out.println("Added with ID " + a.getId());
    }

    private static void updateAssignment() {
        int id = readInt("ID to update: ");
        Assignment a = manager.searchByIdHash(id);
        if (a == null) { System.out.println("No assignment with ID " + id); return; }
        System.out.println("Current: " + a);
        System.out.println("(press Enter to keep the current value)");
        String name = readOptional("Name [" + a.getName() + "]: ", a.getName());
        String subject = readOptional("Subject [" + a.getSubject() + "]: ", a.getSubject());
        LocalDate deadline = a.getDeadline();
        String d = readOptional("Deadline [" + a.getDeadline() + "]: ", "");
        if (!d.isEmpty()) {
            try { deadline = LocalDate.parse(d); }
            catch (DateTimeParseException e) { System.out.println("Bad date, keeping old one."); }
        }
        int priority = a.getPriority();
        String p = readOptional("Priority 1-3 [" + a.getPriority() + "]: ", "");
        if (p.equals("1") || p.equals("2") || p.equals("3")) priority = Integer.parseInt(p);
        manager.update(id, name, subject, deadline, priority);
        System.out.println("Updated.");
    }

    private static void deleteAssignment() {
        int id = readInt("ID to delete: ");
        System.out.println(manager.delete(id) ? "Deleted." : "No assignment with ID " + id);
    }

    private static void searchByName() {
        List<Assignment> found = manager.searchByNameLinear(readText("Name contains: "));
        if (found.isEmpty()) System.out.println("No match.");
        else show(found);
    }

    private static void searchById(boolean useHash) {
        int id = readInt("ID: ");
        Assignment a = useHash ? manager.searchByIdHash(id) : manager.searchByIdLinear(id);
        showOne(useHash ? "Found (HashMap)" : "Found (linear)", a);
    }

    private static void sortByDeadline() {
        System.out.println("1. Bubble Sort  2. Insertion Sort  3. Merge Sort");
        int c = readInt("Algorithm: ");
        TaskManager.SortType type = c == 1 ? TaskManager.SortType.BUBBLE
                : c == 2 ? TaskManager.SortType.INSERTION : TaskManager.SortType.MERGE;
        show(manager.sortByDeadline(type));
    }

    private static void save() {
        try {
            storage.save(manager.getAll());
            System.out.println("Saved to " + storage.path());
        } catch (IOException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }

    // ---------- display helpers ----------

    private static void show(List<Assignment> items) {
        if (items.isEmpty()) { System.out.println("(no assignments)"); return; }
        System.out.println(Assignment.header());
        System.out.println("-".repeat(75));
        for (Assignment a : items) System.out.println(a);
    }

    private static void showOne(String title, Assignment a) {
        if (a == null) { System.out.println("Nothing found."); return; }
        System.out.println(title + ":");
        System.out.println(Assignment.header());
        System.out.println(a);
    }

    // ---------- input helpers ----------

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("Cannot be empty.");
        }
    }

    private static String readOptional(String prompt, String fallback) {
        System.out.print(prompt);
        String s = in.nextLine().trim();
        return s.isEmpty() ? fallback : s;
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return Integer.parseInt(in.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Enter a whole number."); }
        }
    }

    private static int readPriority() {
        while (true) {
            int p = readInt("Priority (1 = High, 2 = Medium, 3 = Low): ");
            if (p >= 1 && p <= 3) return p;
            System.out.println("Enter 1, 2 or 3.");
        }
    }

    private static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try { return LocalDate.parse(in.nextLine().trim()); }
            catch (DateTimeParseException e) { System.out.println("Use format yyyy-MM-dd, e.g. 2026-12-31."); }
        }
    }
}
