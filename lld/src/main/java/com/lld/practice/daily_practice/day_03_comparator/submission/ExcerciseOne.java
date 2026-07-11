package com.lld.practice.daily_practice.day_03_comparator.submission;

import java.util.*;

/*
 * ============================================================
 * DAY 3 - EXERCISE 1: Sort by Single Key (Count DESC)
 * ============================================================
 * DIFFICULTY: Beginner
 * TOPIC: Comparator — single-key descending sort
 *
 * CONTEXT:
 *   You have a leaderboard of courses and their completion counts.
 *   Sort the courses by completion count in DESCENDING order
 *   (highest count first).
 *
 * METHOD:
 *   public static List<String[]> sortByCountDesc(List<String[]> courses)
 *
 * INPUT:
 *   List<String[]> where each String[] is { courseId, count }
 *   Example: { "C1", "50" }
 *   Note: count is stored as String — parse it to int for comparison.
 *
 * OUTPUT:
 *   Same list sorted by count descending.
 *
 * RULES:
 *   - Use Comparator (no manual swapping, no bubble sort)
 *   - Parse count with Integer.parseInt()
 *
 * EXAMPLE:
 *   Input:  [ {"C1","50"}, {"C2","90"}, {"C3","20"} ]
 *   Output: [ {"C2","90"}, {"C1","50"}, {"C3","20"} ]
 *
 * KEY API TO USE:
 *   list.sort(Comparator.comparingInt(...).reversed())
 * ============================================================
 */
public class ExcerciseOne {

    public static List<String[]> sortByCountDesc(List<String[]> courses) {
        // TODO: Sort courses by count (index 1) in descending order using Comparator
        List<String[]> list = new ArrayList<>(courses);
        list.sort(Comparator.comparingInt((String[] data) -> Integer.parseInt(data[1])).reversed());
        return list;
    }

    public static void main(String[] args) {
        List<String[]> courses = new ArrayList<>(Arrays.asList(
                new String[]{"C1", "50"},
                new String[]{"C2", "90"},
                new String[]{"C3", "20"},
                new String[]{"C4", "75"}
        ));

        System.out.println("=== Exercise 1: Sort by Count DESC ===");
        System.out.println("Input:    C1=50, C2=90, C3=20, C4=75");
        System.out.println("Expected: C2=90, C4=75, C1=50, C3=20");
        System.out.println();

        try {
            List<String[]> result = sortByCountDesc(courses);
            System.out.print("Actual:   ");
            for (String[] c : result) System.out.print(c[0] + "=" + c[1] + " ");
            System.out.println();

            // Verify order
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

