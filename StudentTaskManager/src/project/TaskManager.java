package project;

import java.time.LocalDate;
import java.util.*;

/**
 * Core logic. Three structures are kept in sync:
 *  - ArrayList   : main storage (display, linear search, sorting)
 *  - HashMap     : ID -> Assignment, for O(1) average search by ID
 *  - PriorityQueue (heaps): most urgent / highest priority without sorting the whole list
 */
public class TaskManager {

    /** Heap order 1: earliest deadline first (ties: higher priority, then ID). */
    private static final Comparator<Assignment> URGENCY =
            Comparator.comparing(Assignment::getDeadline)
                      .thenComparingInt(Assignment::getPriority)
                      .thenComparingInt(Assignment::getId);

    /** Heap order 2: highest priority first (ties: earliest deadline, then ID). */
    private static final Comparator<Assignment> PRIORITY =
            Comparator.comparingInt(Assignment::getPriority)
                      .thenComparing(Assignment::getDeadline)
                      .thenComparingInt(Assignment::getId);

    private final List<Assignment> list = new ArrayList<>();
    private final Map<Integer, Assignment> byId = new HashMap<>();
    private final PriorityQueue<Assignment> urgentHeap = new PriorityQueue<>(URGENCY);
    private final PriorityQueue<Assignment> priorityHeap = new PriorityQueue<>(PRIORITY);
    private int nextId = 1;

    // ---------- Stage 1: add / update / delete / display ----------

    public Assignment add(String name, String subject, LocalDate deadline, int priority) {
        return addWithId(nextId, name, subject, deadline, priority);
    }

    /** Used by add() and by file loading so IDs from the file are kept. */
    public Assignment addWithId(int id, String name, String subject, LocalDate deadline, int priority) {
        if (byId.containsKey(id)) throw new IllegalArgumentException("Duplicate ID: " + id);
        Assignment a = new Assignment(id, name, subject, deadline, priority);
        list.add(a);                       // O(1) amortised
        byId.put(id, a);                   // O(1) average
        urgentHeap.offer(a);               // O(log n)
        priorityHeap.offer(a);             // O(log n)
        nextId = Math.max(nextId, id + 1);
        return a;
    }

    public boolean update(int id, String name, String subject, LocalDate deadline, int priority) {
        Assignment a = byId.get(id);
        if (a == null) return false;
        // Remove from heaps BEFORE changing fields, otherwise the heap order breaks.
        urgentHeap.remove(a);              // O(n)
        priorityHeap.remove(a);            // O(n)
        a.setName(name);
        a.setSubject(subject);
        a.setDeadline(deadline);
        a.setPriority(priority);
        urgentHeap.offer(a);
        priorityHeap.offer(a);
        return true;
    }

    public boolean delete(int id) {
        Assignment a = byId.remove(id);
        if (a == null) return false;
        list.remove(a);                    // O(n)
        urgentHeap.remove(a);              // O(n)
        priorityHeap.remove(a);            // O(n)
        return true;
    }

    public List<Assignment> getAll() { return Collections.unmodifiableList(list); }
    public int size() { return list.size(); }
    public void clear() {
        list.clear(); byId.clear(); urgentHeap.clear(); priorityHeap.clear(); nextId = 1;
    }

    // ---------- Stage 2: linear search + sorting ----------

    /** Linear search by name (case-insensitive, partial match). O(n). */
    public List<Assignment> searchByNameLinear(String text) {
        List<Assignment> result = new ArrayList<>();
        String q = text.toLowerCase();
        for (Assignment a : list) {
            if (a.getName().toLowerCase().contains(q)) result.add(a);
        }
        return result;
    }

    /** Linear search by ID. O(n). */
    public Assignment searchByIdLinear(int id) {
        for (Assignment a : list) {
            if (a.getId() == id) return a;
        }
        return null;
    }

    public enum SortType { BUBBLE, INSERTION, MERGE }

    /** Returns a new list sorted by deadline using the chosen algorithm. */
    public List<Assignment> sortByDeadline(SortType type) {
        switch (type) {
            case BUBBLE:    return SortAlgorithms.bubbleSort(list, SortAlgorithms.BY_DEADLINE);
            case INSERTION: return SortAlgorithms.insertionSort(list, SortAlgorithms.BY_DEADLINE);
            default:        return SortAlgorithms.mergeSort(list, SortAlgorithms.BY_DEADLINE);
        }
    }

    // ---------- Stage 3: HashMap search ----------

    /** Search by ID using the HashMap. O(1) average. */
    public Assignment searchByIdHash(int id) {
        return byId.get(id);
    }

    // ---------- Stage 4: Priority Queue (heap) ----------

    /** Most urgent = earliest deadline. peek() is O(1). */
    public Assignment mostUrgent() { return urgentHeap.peek(); }

    /** Highest priority (1 = High). peek() is O(1). */
    public Assignment highestPriority() { return priorityHeap.peek(); }
}
