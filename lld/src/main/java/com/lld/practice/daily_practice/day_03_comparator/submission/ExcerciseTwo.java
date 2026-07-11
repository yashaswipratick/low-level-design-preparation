package com.lld.practice.daily_practice.day_03_comparator.submission;

import java.util.*;

/*
 * ============================================================
 * DAY 3 - EXERCISE 2: Multi-Key Sort (Count DESC + Name ASC)
 * ============================================================
 * DIFFICULTY: Beginner-Intermediate
 * TOPIC: Comparator — chained multi-key sort
 *
 * CONTEXT:
 *   In a ranking system, two courses may have the SAME count.
 *   When there's a tie, break it alphabetically by courseId (A → Z).
 *   This is the exact tie-breaking rule used in P1 Trending Courses.
 *
 * METHOD:
 *   public static List<String[]> sortByCountDescThenNameAsc(List<String[]> courses)
 *
 * INPUT:
 *   List<String[]> where each String[] is { courseId, count }
 *
 * OUTPUT:
 *   Sorted by: count DESC first, then courseId ASC on tie
 *
 * RULES:
 *   - Primary sort:   count DESCENDING
 *   - Secondary sort: courseId ASCENDING (alphabetical)
 *   - Must use .thenComparing(...) — one chained comparator, no if/else
 *
 * EXAMPLE:
 *   Input:  [ {"C1","50"}, {"C2","90"}, {"C3","50"}, {"C4","90"} ]
 *   Tie at 90: C2 vs C4 → C2 comes first (alphabetical)
 *   Tie at 50: C1 vs C3 → C1 comes first (alphabetical)
 *   Output: [ {"C2","90"}, {"C4","90"}, {"C1","50"}, {"C3","50"} ]
 *
 * KEY API TO USE:
 *   Comparator.comparingInt(...).reversed().thenComparing(...)
 * ============================================================
 */
public class ExcerciseTwo {

    public static List<String[]> sortByCountDescThenNameAsc(List<String[]> courses) {
        // TODO: Sort by count DESC, then by courseId (index 0) ASC on ties
        // Hint: .reversed() affects only the first key — thenComparing adds a second key
        List<String[]> list = new ArrayList<>(courses);
        list.sort(Comparator.comparingInt((String[] data) -> Integer.parseInt(data[1])).reversed()
                .thenComparing((String[] data) -> data[0]));
        return list;
    }

    public static void main(String[] args) {
        List<String[]> courses = new ArrayList<>(Arrays.asList(
                new String[]{"C1", "50"},
                new String[]{"C2", "90"},
                new String[]{"C3", "50"},
                new String[]{"C4", "90"}
        ));

        System.out.println("=== Exercise 2: Sort by Count DESC + Name ASC (tie-break) ===");
        System.out.println("Input:    C1=50, C2=90, C3=50, C4=90");
        System.out.println("Expected: C2=90, C4=90, C1=50, C3=50");
        System.out.println();

        try {
            List<String[]> result = sortByCountDescThenNameAsc(courses);
            System.out.print("Actual:   ");
            for (String[] c : result) System.out.print(c[0] + "=" + c[1] + " ");
            System.out.println();

            boolean pass = result.get(0)[0].equals("C2")
                    && result.get(1)[0].equals("C4")
                    && result.get(2)[0].equals("C1")
                    && result.get(3)[0].equals("C3");
            System.out.println("Result: " + (pass ? "PASS ✓" : "FAIL ✗"));
        } catch (UnsupportedOperationException e) {
            System.out.println("Actual:   TODO not implemented yet");
        }
    }
}

