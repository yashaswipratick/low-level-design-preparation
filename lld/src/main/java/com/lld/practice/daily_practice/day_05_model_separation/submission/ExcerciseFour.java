package com.lld.practice.daily_practice.day_05_model_separation.submission;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
 * ============================================================
 * DAY 5 - EXERCISE 4: Full API Response with Validation + Separation
 * ============================================================
 * DIFFICULTY: Hard
 * TOPIC: Clean boundaries across input/domain/output with invalid events count
 *
 * CONTEXT:
 *   Build an API-like response for trending summary. Some input events are invalid
 *   (null/blank courseId or userId) and must be ignored, but counted separately.
 *
 * MODELS:
 *   Input/Event   : CompletionEvent(courseId, userId, completedAt)
 *   Domain        : TrendAggregate(courseId, recentCompletions)
 *   Output/API    : TrendingResponse(generatedAt, invalidEvents, items)
 *                   TrendingItem(courseId, completionCount)
 *
 * METHOD:
 *   public static TrendingResponse buildTrendingResponse(
 *       List<CompletionEvent> events, Instant now, int days, int topN)
 *
 * RULES:
 *   - Valid event: courseId and userId are non-null and non-blank
 *   - Use window: (now - days, now]
 *   - Response items sorted by completionCount DESC, then courseId ASC
 *   - Return only topN items
 *   - Output must never expose raw CompletionEvent list
 *
 * TEST DATA:
 *   now=2026-05-24T00:00:00Z, days=7, topN=2
 *   Valid in-window:  C1-U1, C1-U2, C2-U3, C3-U4
 *   Invalid events:   courseId blank, userId blank
 *   Out of window:    C4-U5 at now-10d
 *
 * EXPECTED OUTPUT:
 *   TrendingResponse{generatedAt=2026-05-24T00:00:00Z, invalidEvents=2,
 *   items=[TrendingItem{courseId='C1', completionCount=2}, TrendingItem{courseId='C2', completionCount=1}]}
 * ============================================================
 */
public class ExcerciseFour {

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

    static class TrendAggregate {
        private final String courseId;
        private final int recentCompletions;

        public TrendAggregate(String courseId, int recentCompletions) {
            this.courseId = courseId;
            this.recentCompletions = recentCompletions;
        }

        public String getCourseId() { return courseId; }
        public int getRecentCompletions() { return recentCompletions; }
    }

    static class TrendingItem {
        private final String courseId;
        private final int completionCount;

        public TrendingItem(String courseId, int completionCount) {
            this.courseId = courseId;
            this.completionCount = completionCount;
        }

        public String getCourseId() { return courseId; }
        public int getCompletionCount() { return completionCount; }

        @Override
        public String toString() {
            return "TrendingItem{courseId='" + courseId + "', completionCount=" + completionCount + "}";
        }
    }

    static class TrendingResponse {
        private final Instant generatedAt;
        private final int invalidEvents;
        private final List<TrendingItem> items;

        public TrendingResponse(Instant generatedAt, int invalidEvents, List<TrendingItem> items) {
            this.generatedAt = generatedAt;
            this.invalidEvents = invalidEvents;
            this.items = items;
        }

        public Instant getGeneratedAt() { return generatedAt; }
        public int getInvalidEvents() { return invalidEvents; }
        public List<TrendingItem> getItems() { return items; }

        @Override
        public String toString() {
            return "TrendingResponse{generatedAt=" + generatedAt
                    + ", invalidEvents=" + invalidEvents
                    + ", items=" + items + "}";
        }
    }

    public static TrendingResponse buildTrendingResponse(List<CompletionEvent> events, Instant now, int days, int topN) {
        // TODO:
        // 1) Validate input events and count invalid events
        // 2) Filter valid events by window
        // 3) Aggregate in domain model
        // 4) Map to TrendingItem, sort, and limit topN
        // 5) Build TrendingResponse
        throw new UnsupportedOperationException("TODO");
    }

    public static void main(String[] args) {
        Instant now = Instant.parse("2026-05-24T00:00:00Z");

        List<CompletionEvent> events = new ArrayList<>(Arrays.asList(
                new CompletionEvent("C1", "U1", now.minusSeconds(1L * 24 * 3600)),
                new CompletionEvent("C1", "U2", now.minusSeconds(2L * 24 * 3600)),
                new CompletionEvent("C2", "U3", now.minusSeconds(3L * 24 * 3600)),
                new CompletionEvent("C3", "U4", now.minusSeconds(4L * 24 * 3600)),
                new CompletionEvent(" ", "U6", now.minusSeconds(1L * 24 * 3600)),
                new CompletionEvent("C7", " ", now.minusSeconds(1L * 24 * 3600)),
                new CompletionEvent("C4", "U5", now.minusSeconds(10L * 24 * 3600))
        ));

        System.out.println("=== Exercise 4: Full API Response with Validation + Separation ===");
        System.out.println("Expected:");
        System.out.println("TrendingResponse{generatedAt=2026-05-24T00:00:00Z, invalidEvents=2, items=[TrendingItem{courseId='C1', completionCount=2}, TrendingItem{courseId='C2', completionCount=1}]}");

        try {
            TrendingResponse actual = buildTrendingResponse(events, now, 7, 2);
            System.out.println("Actual:");
            System.out.println(actual);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Actual:");
            System.out.println("TODO not implemented yet");
        }
    }
}

