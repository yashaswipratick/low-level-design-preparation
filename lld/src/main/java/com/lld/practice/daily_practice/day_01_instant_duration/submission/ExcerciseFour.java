package com.lld.practice.daily_practice.day_01_instant_duration.submission;

import java.time.Instant;
import java.util.List;

/*
 * ============================================================
 * DAY 1 - EXERCISE 4: Multiple Rolling Windows
 * ============================================================
 * TOPIC: Instant + Duration — Flexible Time Window Filtering
 * DIFFICULTY: Medium
 *
 * PROBLEM:
 *   Write a method that counts how many events occurred within a specific
 *   rolling window of N days. This is a core building block of the P1
 *   Trending Courses problem — used for 7-day, 30-day, 90-day windows.
 *
 * METHOD SIGNATURE:
 *   public int countEventsInWindow(List<Instant> eventTimes, Instant now, int days)
 *
 * PARAMETERS:
 *   - eventTimes : list of all event timestamps to search through
 *   - now        : current reference time (injected — never call Instant.now() inside!)
 *   - days       : size of the rolling window in days (e.g. 7, 30, 90)
 *
 * RETURN:
 *   Count of events that fall in the range [now - days, now] (both ends INCLUSIVE).
 *
 * BOUNDARY DECISION:
 *   Both boundaries are INCLUSIVE here (unlike Ex 2).
 *   Reason: When counting totals for a report, you want to include the exact
 *           start boundary (e.g. an event "exactly 7 days ago" belongs to the 7-day report).
 *
 * EXAMPLES (given events at: now-6d, now-15d, now-35d, now-65d, now-100d):
 *   countEventsInWindow(events, now,  7) → 1   (only now-6d is within 7 days)
 *   countEventsInWindow(events, now, 30) → 2   (now-6d and now-15d within 30 days)
 *   countEventsInWindow(events, now, 90) → 4   (now-6d, now-15d, now-35d, now-65d within 90 days)
 *                                              Note: now-65d < 90 days, so it IS included!
 *
 * KEY CONCEPT:
 *   windowStart = now.minus(days, ChronoUnit.DAYS)
 *   Inclusive check: !event.isBefore(windowStart) && !event.isAfter(now)
 *   This is equivalent to: event >= windowStart && event <= now
 *
 * NOTE ON EXERCISE SPEC:
 *   The original exercise said 90-day window → 3, but the correct answer is 4
 *   because now-65d (65 < 90) is inside the 90-day window. Code is correct!
 * ============================================================
 */
public class ExcerciseFour {

    public int countEventsInWindow(List<Instant> eventTimes, Instant now, int days) {
        int count = 0;
        Instant windowStart = now.minus(days, java.time.temporal.ChronoUnit.DAYS);
        for (Instant event : eventTimes) {
            if (!event.isBefore(windowStart) && !event.isAfter(now)) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        Instant now = Instant.now();
        List<Instant> eventTimes = List.of(
            now.minus(6, java.time.temporal.ChronoUnit.DAYS),
            now.minus(15, java.time.temporal.ChronoUnit.DAYS),
            now.minus(35, java.time.temporal.ChronoUnit.DAYS),
            now.minus(65, java.time.temporal.ChronoUnit.DAYS),
            now.minus(100, java.time.temporal.ChronoUnit.DAYS)
        );

        ExcerciseFour solution = new ExcerciseFour();
        System.out.println(solution.countEventsInWindow(eventTimes, now, 7));   // expect: 1
        System.out.println(solution.countEventsInWindow(eventTimes, now, 30));  // expect: 2
        System.out.println(solution.countEventsInWindow(eventTimes, now, 90));  // expect: 4 (now-65d is within 90 days!)
    }
}
