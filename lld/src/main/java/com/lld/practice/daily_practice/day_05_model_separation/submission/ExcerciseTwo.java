package com.lld.practice.daily_practice.day_05_model_separation.submission;

import java.util.*;

/*
 * ============================================================
 * DAY 5 - EXERCISE 2: Dedupe in Domain, Clean Output DTO
 * ============================================================
 * DIFFICULTY: Beginner-Intermediate
 * TOPIC: Domain model can hold computation details, output must stay clean
 *
 * CONTEXT:
 *   For each course, count unique learners. Same user completing same course multiple
 *   times should count once in uniqueLearners.
 *
 * MODELS:
 *   Input/Event   : CompletionEvent(courseId, userId)
 *   Domain        : CourseStats(courseId, uniqueLearners)
 *   Output/API    : CourseRankingItem(courseId, uniqueLearners)
 *
 * METHOD:
 *   public static List<CourseRankingItem> rankByUniqueLearners(List<CompletionEvent> events)
 *
 * RULES:
 *   - Output sorted by uniqueLearners DESC, then courseId ASC
 *   - Output model should not contain raw event list
 *
 * TEST DATA:
 *   C1-U1, C1-U1, C1-U2, C2-U3, C2-U4, C3-U5
 *
 * EXPECTED OUTPUT:
 *   [CourseRankingItem{courseId='C1', uniqueLearners=2},
 *    CourseRankingItem{courseId='C2', uniqueLearners=2},
 *    CourseRankingItem{courseId='C3', uniqueLearners=1}]
 * ============================================================
 */
public class ExcerciseTwo {

    static class CompletionEvent {
        private final String courseId;
        private final String userId;

        public CompletionEvent(String courseId, String userId) {
            this.courseId = courseId;
            this.userId = userId;
        }

        public String getCourseId() { return courseId; }
        public String getUserId() { return userId; }
    }

    static class CourseStats {
        private final String courseId;
        private final int uniqueLearners;

        public CourseStats(String courseId, int uniqueLearners) {
            this.courseId = courseId;
            this.uniqueLearners = uniqueLearners;
        }

        public String getCourseId() { return courseId; }
        public int getUniqueLearners() { return uniqueLearners; }
    }

    static class CourseRankingItem {
        private final String courseId;
        private final int uniqueLearners;

        public CourseRankingItem(String courseId, int uniqueLearners) {
            this.courseId = courseId;
            this.uniqueLearners = uniqueLearners;
        }

        public String getCourseId() { return courseId; }
        public int getUniqueLearners() { return uniqueLearners; }

        @Override
        public String toString() {
            return "CourseRankingItem{courseId='" + courseId + "', uniqueLearners=" + uniqueLearners + "}";
        }
    }

    public static List<CourseRankingItem> rankByUniqueLearners(List<CompletionEvent> events) {
        // 1) Build domain stats with dedupe
        Map<String, Set<String>> uniqueLearnersMap = new HashMap<>();
        for (CompletionEvent event : events) {
            uniqueLearnersMap.computeIfAbsent(event.getCourseId(), key -> new HashSet<>()).add(event.getUserId());
        }
        // 2) Convert to CourseRankingItem DTO list
        List<CourseRankingItem> list = new ArrayList<>(uniqueLearnersMap.entrySet().stream()
                .map(entry -> new CourseRankingItem(entry.getKey(), entry.getValue().size()))
                .toList());
        // 3) Sort by uniqueLearners DESC, then courseId ASC
        list.sort(Comparator.comparingInt(CourseRankingItem::getUniqueLearners).reversed()
                .thenComparing(CourseRankingItem::getCourseId));
        return list;
    }

    public static void main(String[] args) {
        List<CompletionEvent> events = new ArrayList<>(Arrays.asList(
                new CompletionEvent("C1", "U1"),
                new CompletionEvent("C1", "U1"),
                new CompletionEvent("C1", "U2"),
                new CompletionEvent("C2", "U3"),
                new CompletionEvent("C2", "U4"),
                new CompletionEvent("C3", "U5")
        ));

        System.out.println("=== Exercise 2: Dedupe in Domain, Clean Output DTO ===");
        System.out.println("Expected:");
        System.out.println("[CourseRankingItem{courseId='C1', uniqueLearners=2}, CourseRankingItem{courseId='C2', uniqueLearners=2}, CourseRankingItem{courseId='C3', uniqueLearners=1}]");

        try {
            List<CourseRankingItem> actual = rankByUniqueLearners(events);
            System.out.println("Actual:");
            System.out.println(actual);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Actual:");
            System.out.println("TODO not implemented yet");
        }
    }
}

