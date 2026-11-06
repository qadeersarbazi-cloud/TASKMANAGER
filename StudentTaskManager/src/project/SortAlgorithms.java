package project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Hand-written sorting algorithms. Each one returns a NEW sorted list
 * and leaves the original list untouched.
 */
public class SortAlgorithms {

    /** Default order for "sort by deadline": earliest first, ties broken by ID. */
    public static final Comparator<Assignment> BY_DEADLINE =
            Comparator.comparing(Assignment::getDeadline).thenComparingInt(Assignment::getId);

    /** Bubble Sort - O(n^2) average/worst, O(n) best (already sorted, thanks to the early exit). */
    public static List<Assignment> bubbleSort(List<Assignment> input, Comparator<Assignment> cmp) {
        List<Assignment> a = new ArrayList<>(input);
        int n = a.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (cmp.compare(a.get(j), a.get(j + 1)) > 0) {
                    Assignment tmp = a.get(j);
                    a.set(j, a.get(j + 1));
                    a.set(j + 1, tmp);
                    swapped = true;
                }
            }
            if (!swapped) break;
        }
        return a;
    }

    /** Insertion Sort - O(n^2) average/worst, O(n) best. */
    public static List<Assignment> insertionSort(List<Assignment> input, Comparator<Assignment> cmp) {
        List<Assignment> a = new ArrayList<>(input);
        for (int i = 1; i < a.size(); i++) {
            Assignment key = a.get(i);
            int j = i - 1;
            while (j >= 0 && cmp.compare(a.get(j), key) > 0) {
                a.set(j + 1, a.get(j));
                j--;
            }
            a.set(j + 1, key);
        }
        return a;
    }

    /** Merge Sort - O(n log n) in every case, needs O(n) extra space. */
    public static List<Assignment> mergeSort(List<Assignment> input, Comparator<Assignment> cmp) {
        if (input.size() <= 1) return new ArrayList<>(input);
        int mid = input.size() / 2;
        List<Assignment> left = mergeSort(input.subList(0, mid), cmp);
        List<Assignment> right = mergeSort(input.subList(mid, input.size()), cmp);
        return merge(left, right, cmp);
    }

    private static List<Assignment> merge(List<Assignment> left, List<Assignment> right,
                                          Comparator<Assignment> cmp) {
        List<Assignment> out = new ArrayList<>(left.size() + right.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) out.add(left.get(i++));
            else out.add(right.get(j++));
        }
        while (i < left.size()) out.add(left.get(i++));
        while (j < right.size()) out.add(right.get(j++));
        return out;
    }
}
