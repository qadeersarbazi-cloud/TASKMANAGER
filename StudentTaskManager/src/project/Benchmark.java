package project;

import java.time.LocalDate;
import java.util.Random;

/** Stage 5: measures the different approaches so they can be compared with the Big-O analysis. */
public class Benchmark {

    /** All timings are in milliseconds. */
    public static class Result {
        public final int n;
        public double linear, hash, bubble, insertion, merge, sortFirst, heap;
        Result(int n) { this.n = n; }
    }

    public static Result measure(int n) {
        TaskManager tm = new TaskManager();
        Random rnd = new Random(42);
        LocalDate base = LocalDate.of(2026, 1, 1);
        for (int i = 0; i < n; i++) {
            tm.add("Task " + i, "Subject " + (i % 10), base.plusDays(rnd.nextInt(365)), 1 + rnd.nextInt(3));
        }
        int target = n;            // the LAST id = worst case for linear search
        int slowRepeats = 2000;        // linear search is slow, so fewer repetitions
        int fastRepeats = 200000;      // HashMap / heap peek are so fast they need many repetitions

        for (int i = 0; i < 300; i++) tm.searchByIdLinear(target);          // warm-up so the JIT does not
        for (int i = 0; i < 50000; i++) { tm.searchByIdHash(target); tm.mostUrgent(); }   // distort results

        Result r = new Result(n);
        long t0 = System.nanoTime();
        for (int i = 0; i < slowRepeats; i++) tm.searchByIdLinear(target);
        r.linear = (System.nanoTime() - t0) / 1e6 / slowRepeats;

        t0 = System.nanoTime();
        for (int i = 0; i < fastRepeats; i++) tm.searchByIdHash(target);
        r.hash = (System.nanoTime() - t0) / 1e6 / fastRepeats;

        r.bubble = time(tm, TaskManager.SortType.BUBBLE);
        r.insertion = time(tm, TaskManager.SortType.INSERTION);
        r.merge = time(tm, TaskManager.SortType.MERGE);

        t0 = System.nanoTime();                  // "most urgent" the slow way: sort all, take first
        tm.sortByDeadline(TaskManager.SortType.MERGE).get(0);
        r.sortFirst = (System.nanoTime() - t0) / 1e6;

        t0 = System.nanoTime();
        for (int i = 0; i < fastRepeats; i++) tm.mostUrgent();
        r.heap = (System.nanoTime() - t0) / 1e6 / fastRepeats;
        return r;
    }

    /** Console version (menu option 11 in Main). */
    public static void run() {
        int[] sizes = {1000, 5000, 20000};
        System.out.println("\nTimes are in milliseconds (lower is better). Bubble Sort at n=20000 takes a few seconds.");
        System.out.printf("%-8s | %-10s %-10s | %-9s %-9s %-9s | %-12s %-10s%n",
                "n", "LinearID", "HashID", "Bubble", "Insertion", "Merge", "Sort+first", "HeapPeek");
        System.out.println("-".repeat(95));
        for (int n : sizes) {
            Result r = measure(n);
            System.out.printf("%-8d | %-10.5f %-10.5f | %-9.2f %-9.2f %-9.2f | %-12.2f %-10.5f%n",
                    n, r.linear, r.hash, r.bubble, r.insertion, r.merge, r.sortFirst, r.heap);
        }
        System.out.println("\nSummary: HashMap search and heap peek stay almost flat as n grows,");
        System.out.println("while linear search and full sorting grow with n (Bubble/Insertion fastest growth).");
    }

    private static double time(TaskManager tm, TaskManager.SortType type) {
        long t0 = System.nanoTime();
        tm.sortByDeadline(type);
        return (System.nanoTime() - t0) / 1e6;
    }
}
