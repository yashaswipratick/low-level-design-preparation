package com.lld.practice.daily_practice.day_02_map_set.submission;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/*
 * ============================================================
 * DAY 2 - EXERCISE 3: Unique Viewers in a Rolling Window
 * ============================================================
 * CONTEXT:
 *   Analytics needs unique viewers per course within last N days.
 *
 * METHOD:
 *   uniqueViewersInWindow(events, now, days)
 *
 * INPUT:
 *   - events: List<ViewEvent> (courseId, userId, viewedAt)
 *   - now: fixed current time for deterministic tests
 *   - days: window size (example: 7)
 *
 * OUTPUT:
 *   Map<courseId, uniqueViewerCountInWindow>
 *
 * BOUNDARY RULE:
 *   Include events in [now - days, now] (inclusive), ignore future events.
 *
 * GOAL:
 *   Time filter first, then collect-then-count with Map<K, Set<V>>.
 * ============================================================
 */
public class ExcerciseThree {

    public static Map<String, Integer> uniqueViewersInWindow(List<ViewEvent> events, Instant now, int days) {
        // TODO: Implement using window filter + Map<String, Set<String>> + computeIfAbsent
        Map<String, Set<String>> map = new HashMap<>();

        Instant windowStart = now.minus(Duration.ofDays(days));
        for (ViewEvent event : events) {

            /*
            isAfter   →  strictly >   (exclusive)
            isBefore  →  strictly <   (exclusive)

            !isBefore →  >=           (inclusive) ← what you actually want
            !isAfter  →  <=           (inclusive) ← what you actually want

            The ! is doing exactly one job: converting a strict inequality into an inclusive one,
            because the API doesn't give you >=/<= directly
            */
            if (!event.getViewedAt().isBefore(windowStart)
                    && !event.getViewedAt().isAfter(now)) {
                map.computeIfAbsent(event.getCourseId(), key -> new HashSet<>())
                        .add(event.getUserId());
            }
        }
        return map
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
    }

    public static void main(String[] args) {
        Instant now = Instant.parse("2026-05-21T00:00:00Z");

        List<ViewEvent> events = new ArrayList<>();
        events.add(new ViewEvent("C1", "U1", now.minusSeconds(1 * 24 * 3600)));
        events.add(new ViewEvent("C1", "U1", now.minusSeconds(2 * 24 * 3600)));
        events.add(new ViewEvent("C1", "U2", now.minusSeconds(8 * 24 * 3600)));
        events.add(new ViewEvent("C2", "U3", now));
        events.add(new ViewEvent("C2", "U4", now.plusSeconds(3600)));

        Map<String, Integer> expected = new HashMap<>();
        expected.put("C1", 1);
        expected.put("C2", 1);

        System.out.println("Expected: " + expected);

        try {
            Map<String, Integer> actual = uniqueViewersInWindow(events, now, 7);
            System.out.println("Actual:   " + actual);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Actual:   TODO not implemented yet");
        }
    }
}

