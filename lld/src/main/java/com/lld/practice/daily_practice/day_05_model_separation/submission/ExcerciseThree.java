package com.lld.practice.daily_practice.day_05_model_separation.submission;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/*
 * ============================================================
 * DAY 5 - EXERCISE 3: Time-Filtered Domain Snapshot to Response DTO
 * ============================================================
 * DIFFICULTY: Intermediate
 * TOPIC: Input filtering + domain aggregation + output projection
 *
 * CONTEXT:
 *   You receive completion events with timestamps. Build response for a rolling
 *   window (last N days) without leaking event model in API output.
 *
 * MODELS:
 *   Input/Event   : CompletionEvent(courseId, userId, completedAt)
 *   Domain        : CourseWindowStats(courseId, completionCountInWindow)
 *   Output/API    : CourseWindowResponse(courseId, completionCount)
 *
 * METHOD:
 *   public static List<CourseWindowResponse> getWindowSummary(
 *       List<CompletionEvent> events, Instant now, int days)
 *
 * RULES:
 *   - Use window: (now - days, now]
 *   - Ignore events outside window
 *   - Sort output by completionCount DESC, then courseId ASC
 *
 * TEST DATA (now=2026-05-24T00:00:00Z, days=7):
 *   C1 at now-1d, C1 at now-2d, C2 at now-3d, C3 at now-8d
 *
 * EXPECTED OUTPUT:
 *   [CourseWindowResponse{courseId='C1', completionCount=2},
 *    CourseWindowResponse{courseId='C2', completionCount=1}]
 * ============================================================
 */
public class ExcerciseThree {

    static class CompletionEvent {
        private final String courseId;
        private final String userId;
        private final Instant completedAt;

        public CompletionEvent(String courseId, String userId, Instant completedAt) {
            this.courseId = courseId;
            this.userId = userId;
            this.completedAt = completedAt;
        }

        public String getCourseId() { return courseId; }
        public String getUserId() { return userId; }
        public Instant getCompletedAt() { return completedAt; }
    }

    static class CourseWindowStats {
        private final String courseId;
        private final int completionCountInWindow;

        public CourseWindowStats(String courseId, int completionCountInWindow) {
            this.courseId = courseId;
            this.completionCountInWindow = completionCountInWindow;
        }

        public String getCourseId() { return courseId; }
        public int getCompletionCountInWindow() { return completionCountInWindow; }
    }

    static class CourseWindowResponse {
        private final String courseId;
        private final int completionCount;

        public CourseWindowResponse(String courseId, int completionCount) {
            this.courseId = courseId;
            this.completionCount = completionCount;
        }

        public String getCourseId() { return courseId; }
        public int getCompletionCount() { return completionCount; }

        @Override
        public String toString() {
            return "CourseWindowResponse{courseId='" + courseId + "', completionCount=" + completionCount + "}";
        }
    }

    public static List<CourseWindowResponse> getWindowSummary(List<CompletionEvent> events, Instant now, int days) {
        // TODO:
        Instant startingWindowDay = now.minus(Duration.ofDays(days));
        Map<String, Integer> map = new HashMap<>();
        // 1) Filter input events by rolling window
        for (CompletionEvent event : events) {
            if (event.getCompletedAt().isBefore(startingWindowDay) || event.getCompletedAt().isAfter(now)) {
                continue;
            } else {
                map.put(event.courseId, map.getOrDefault(event.courseId, 0) + 1);
            }
        }
        // 2) Aggregate in domain model
        List<CourseWindowStats> list = map.entrySet().stream()
                .map(entry -> new CourseWindowStats(entry.getKey(), entry.getValue()))
                .toList();
        // 3) Map to response DTO and sort
        List<CourseWindowResponse> responses = new ArrayList<>(list.stream()
                .map(courseWindowStats ->
                        new CourseWindowResponse(courseWindowStats.getCourseId(), courseWindowStats.getCompletionCountInWindow()))
                .toList());
        responses.sort(Comparator.comparingInt(CourseWindowResponse::getCompletionCount).reversed().thenComparing(CourseWindowResponse::getCourseId));
        return responses;
    }

    public static void main(String[] args) {
        Instant now = Instant.parse("2026-05-24T00:00:00Z");

        List<CompletionEvent> events = new ArrayList<>(Arrays.asList(
                new CompletionEvent("C1", "U1", now.minusSeconds(1L * 24 * 3600)),
                new CompletionEvent("C1", "U2", now.minusSeconds(2L * 24 * 3600)),
                new CompletionEvent("C2", "U3", now.minusSeconds(3L * 24 * 3600)),
                new CompletionEvent("C3", "U4", now.minusSeconds(8L * 24 * 3600))
        ));

        System.out.println("=== Exercise 3: Time-Filtered Domain Snapshot ===");
        System.out.println("Expected:");
        System.out.println("[CourseWindowResponse{courseId='C1', completionCount=2}, CourseWindowResponse{courseId='C2', completionCount=1}]");

        try {
            List<CourseWindowResponse> actual = getWindowSummary(events, now, 7);
            System.out.println("Actual:");
            System.out.println(actual);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Actual:");
            System.out.println("TODO not implemented yet");
        }
    }
}

