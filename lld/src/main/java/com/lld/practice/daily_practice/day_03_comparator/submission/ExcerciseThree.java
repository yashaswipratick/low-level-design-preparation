package com.lld.practice.daily_practice.day_03_comparator.submission;

import java.util.*;

/*
 * ============================================================
 * DAY 3 - EXERCISE 3: Sort Custom Objects with Comparator.comparing()
 * ============================================================
 * DIFFICULTY: Intermediate
 * TOPIC: Comparator.comparing() with method references on POJOs
 *
 * CONTEXT:
 *   In real LLD problems, you sort objects — not arrays.
 *   Here you have a CourseScore object with courseId and score.
 *   Sort by score DESC. If tied, sort by courseId ASC.
 *
 * METHOD:
 *   public static List<CourseScore> sortCourseScores(List<CourseScore> scores)
 *
 * INPUT:
 *   List<CourseScore> — each has String courseId, int score
 *
 * OUTPUT:
 *   Sorted: score DESC, then courseId ASC on tie
 *
 * RULES:
 *   - Use Comparator.comparing() or Comparator.comparingInt()
 *   - Use method references: CourseScore::getScore, CourseScore::getCourseId
 *   - No manual field access like c.score — use getters
 *
 * EXAMPLE:
 *   Input:  [ (C3,80), (C1,95), (C2,80), (C4,60) ]
 *   Tie at 80: C2 vs C3 → C2 comes first
 *   Output: [ (C1,95), (C2,80), (C3,80), (C4,60) ]
 *
 * KEY API TO USE:
 *   Comparator.comparingInt(CourseScore::getScore).reversed()
 *             .thenComparing(CourseScore::getCourseId)
 * ============================================================
 */
public class ExcerciseThree {

    // ---- Model (do not change) ----
    static class CourseScore {
        private final String courseId;
        private final int score;

        public CourseScore(String courseId, int score) {
            this.courseId = courseId;
            this.score = score;
        }

        public String getCourseId() { return courseId; }
        public int getScore()       { return score; }

        @Override
        public String toString() { return courseId + "=" + score; }
    }

    // ---- Your method ----
    public static List<CourseScore> sortCourseScores(List<CourseScore> scores) {
        // TODO: Sort by score DESC, then by courseId ASC using Comparator.comparingInt + method references
        List<CourseScore> list = new ArrayList<>(scores);
        list.sort(Comparator.comparingInt(CourseScore::getScore).reversed().thenComparing(CourseScore::getCourseId));
        return list;
    }

    public static void main(String[] args) {
        List<CourseScore> scores = new ArrayList<>(Arrays.asList(
                new CourseScore("C3", 80),
                new CourseScore("C1", 95),
                new CourseScore("C2", 80),
                new CourseScore("C4", 60)
        ));

        System.out.println("=== Exercise 3: Sort CourseScore objects DESC + tie-break ASC ===");
        System.out.println("Input:    C3=80, C1=95, C2=80, C4=60");
        System.out.println("Expected: C1=95, C2=80, C3=80, C4=60");
        System.out.println();

        try {
            List<CourseScore> result = sortCourseScores(scores);
            System.out.println("Actual:   " + result);

            boolean pass = result.get(0).getCourseId().equals("C1")
                    && result.get(1).getCourseId().equals("C2")
                    && result.get(2).getCourseId().equals("C3")
                    && result.get(3).getCourseId().equals("C4");
            System.out.println("Result: " + (pass ? "PASS ✓" : "FAIL ✗"));
        } catch (UnsupportedOperationException e) {
            System.out.println("Actual:   TODO not implemented yet");
        }
    }
}

