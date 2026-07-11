package com.lld.practice.daily_practice.day_03_comparator.submission;

import java.util.*;

/*
 * ============================================================
 * DAY 3 - EXERCISE 5: Full Ranking Pipeline (Filter + Sort + Limit)
 * ============================================================
 * DIFFICULTY: Hard (closest to P1)
 * TOPIC: Comparator — complete ranking pipeline
 *
 * CONTEXT:
 *   In P1 Trending Courses, after counting completions per course,
 *   you need to: filter → sort → limit → return top N.
 *   This exercise practices that EXACT pipeline.
 *
 *   You are given a list of CourseStat objects (courseId + completionCount).
 *   Return the top N trending courses, where:
 *     - Only include courses with completionCount >= minCount (filter)
 *     - Sort by completionCount DESC, then courseId ASC (multi-key sort)
 *     - Return only the top N course IDs (limit)
 *
 * METHOD:
 *   public static List<String> getTopNCourses(List<CourseStat> stats, int minCount, int topN)
 *
 * INPUT:
 *   stats    — list of CourseStat (courseId, completionCount)
 *   minCount — minimum completionCount to be included (inclusive)
 *   topN     — how many results to return
 *
 * OUTPUT:
 *   List<String> of courseIds — top N after filter and sort
 *
 * PIPELINE (write in this order):
 *   Step 1: Filter  — remove courses with completionCount < minCount
 *   Step 2: Sort    — completionCount DESC, then courseId ASC on tie
 *   Step 3: Limit   — keep only first topN results
 *   Step 4: Extract — return just the courseId strings
 *
 * EXAMPLE:
 *   stats:    [ (C1,10), (C2,50), (C3,5), (C4,50), (C5,30) ]
 *   minCount: 10
 *   topN:     3
 *
 *   After filter (>=10): [ (C1,10), (C2,50), (C4,50), (C5,30) ]
 *   After sort (DESC+ASC): [ (C2,50), (C4,50), (C5,30), (C1,10) ]
 *   After limit (top 3):   [ (C2,50), (C4,50), (C5,30) ]
 *   Output: [ "C2", "C4", "C5" ]
 *
 * KEY APIS TO USE:
 *   list.stream()
 *       .filter(...)
 *       .sorted(Comparator...)
 *       .limit(topN)
 *       .map(CourseStat::getCourseId)
 *       .collect(Collectors.toList())
 *
 * NOTE: You can also do it without streams — sort the list directly, then
 *       loop and collect. Both approaches are valid.
 * ============================================================
 */
public class ExcerciseFive {

    // ---- Model (do not change) ----
    static class CourseStat {
        private final String courseId;
        private final int completionCount;

        public CourseStat(String courseId, int completionCount) {
            this.courseId = courseId;
            this.completionCount = completionCount;
        }

        public String getCourseId()       { return courseId; }
        public int getCompletionCount()   { return completionCount; }

        @Override
        public String toString() { return courseId + "=" + completionCount; }
    }

    // ---- Your method ----
    public static List<String> getTopNCourses(List<CourseStat> stats, int minCount, int topN) {
        // TODO:
        //   Step 1 - Filter: completionCount >= minCount
        //   Step 2 - Sort:   completionCount DESC, then courseId ASC
        //   Step 3 - Limit:  keep top N
        //   Step 4 - Map:    extract courseId only
        return stats.stream().filter(courseStat -> courseStat.getCompletionCount() >= minCount)
                .sorted(Comparator.comparingInt(CourseStat::getCompletionCount).reversed().thenComparing(CourseStat::getCourseId))
                .limit(topN)
                .map(CourseStat::getCourseId)
                .toList();
    }

    public static void main(String[] args) {
        System.out.println("=== Exercise 5: Full Ranking Pipeline ===");

        // --- Test Case 1 ---
        List<CourseStat> stats1 = new ArrayList<>(Arrays.asList(
                new CourseStat("C1", 10),
                new CourseStat("C2", 50),
                new CourseStat("C3", 5),
                new CourseStat("C4", 50),
                new CourseStat("C5", 30)
        ));
        System.out.println("Test 1 | minCount=10, topN=3");
        System.out.println("Expected: [C2, C4, C5]");
        try {
            System.out.println("Actual:   " + getTopNCourses(stats1, 10, 3));
        } catch (UnsupportedOperationException e) {
            System.out.println("Actual:   TODO not implemented yet");
        }
        System.out.println();

        // --- Test Case 2: all below minCount ---
        List<CourseStat> stats2 = new ArrayList<>(Arrays.asList(
                new CourseStat("C1", 2),
                new CourseStat("C2", 3)
        ));
        System.out.println("Test 2 | minCount=10, topN=3 (all below threshold)");
        System.out.println("Expected: []");
        try {
            System.out.println("Actual:   " + getTopNCourses(stats2, 10, 3));
        } catch (UnsupportedOperationException e) {
            System.out.println("Actual:   TODO not implemented yet");
        }
        System.out.println();

        // --- Test Case 3: topN larger than available ---
        List<CourseStat> stats3 = new ArrayList<>(Arrays.asList(
                new CourseStat("C1", 40),
                new CourseStat("C2", 20)
        ));
        System.out.println("Test 3 | minCount=10, topN=5 (only 2 qualify)");
        System.out.println("Expected: [C1, C2]");
        try {
            System.out.println("Actual:   " + getTopNCourses(stats3, 10, 5));
        } catch (UnsupportedOperationException e) {
            System.out.println("Actual:   TODO not implemented yet");
        }
    }
}

