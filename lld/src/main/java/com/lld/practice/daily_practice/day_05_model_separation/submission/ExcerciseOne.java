package com.lld.practice.daily_practice.day_05_model_separation.submission;

import java.util.*;

/*
 * ============================================================
 * DAY 5 - EXERCISE 1: Input vs Output Model Basics
 * ============================================================
 * DIFFICULTY: Beginner
 * TOPIC: Separate event model from response model
 *
 * CONTEXT:
 *   You receive raw completion events. API should return compact course summaries.
 *   Do NOT return raw events directly in response.
 *
 * MODELS:
 *   Input/Event   : CompletionEvent(courseId, userId)
 *   Domain        : CourseAggregate(courseId, uniqueUserCount)
 *   Output/API    : CourseSummary(courseId, totalCompletions)
 *
 * TASK:
 *   Implement buildCourseSummaries(events) to return one CourseSummary per course.
 *
 * METHOD:
 *   public static List<CourseSummary> buildCourseSummaries(List<CompletionEvent> events)
 *
 * RULES:
 *   - Keep input model separate from output model
 *   - Do not include CompletionEvent inside CourseSummary
 *   - Sort output by courseId ascending for deterministic output
 *
 * TEST DATA:
 *   C1-U1, C1-U2, C2-U3, C1-U1
 *
 * EXPECTED OUTPUT:
 *   [CourseSummary{courseId='C1', totalCompletions=3},
 *    CourseSummary{courseId='C2', totalCompletions=1}]
 * ============================================================
 */
public class ExcerciseOne {

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

    static class CourseAggregate {
        private final String courseId;
        private final int totalCompletions;

        public CourseAggregate(String courseId, int totalCompletions) {
            this.courseId = courseId;
            this.totalCompletions = totalCompletions;
        }

        public String getCourseId() { return courseId; }
        public int getTotalCompletions() { return totalCompletions; }
    }

    static class CourseSummary {
        private final String courseId;
        private final int totalCompletions;

        public CourseSummary(String courseId, int totalCompletions) {
            this.courseId = courseId;
            this.totalCompletions = totalCompletions;
        }

        public String getCourseId() { return courseId; }
        public int getTotalCompletions() { return totalCompletions; }

        @Override
        public String toString() {
            return "CourseSummary{courseId='" + courseId + "', totalCompletions=" + totalCompletions + "}";
        }
    }

    public static List<CourseSummary> buildCourseSummaries(List<CompletionEvent> events) {
        // 1) Aggregate completion counts per course (domain step)
        Map<String, Integer> totalCompletions = new HashMap<>();
        for (CompletionEvent event : events) {
            totalCompletions.put(event.getCourseId(), totalCompletions.getOrDefault(event.getCourseId(), 0) + 1);
        }
        List<CourseAggregate> list = new ArrayList<>(totalCompletions.entrySet().stream().map(entry -> new CourseAggregate(entry.getKey(), entry.getValue()))
                .toList());
        // 2) Map aggregates to CourseSummary (output step)
        list.sort(Comparator.comparing(CourseAggregate::getCourseId));
        // 3) Sort by courseId ASC
        return list.stream().map(aggregate -> new CourseSummary(aggregate.getCourseId(), aggregate.getTotalCompletions()))
                .toList();
    }

    public static void main(String[] args) {
        List<CompletionEvent> events = new ArrayList<>(Arrays.asList(
                new CompletionEvent("C1", "U1"),
                new CompletionEvent("C1", "U2"),
                new CompletionEvent("C2", "U3"),
                new CompletionEvent("C1", "U1")
        ));

        System.out.println("=== Exercise 1: Input vs Output Model Basics ===");
        System.out.println("Expected:");
        System.out.println("[CourseSummary{courseId='C1', totalCompletions=3}, CourseSummary{courseId='C2', totalCompletions=1}]");

        try {
            List<CourseSummary> actual = buildCourseSummaries(events);
            System.out.println("Actual:");
            System.out.println(actual);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Actual:");
            System.out.println("TODO not implemented yet");
        }
    }
}

