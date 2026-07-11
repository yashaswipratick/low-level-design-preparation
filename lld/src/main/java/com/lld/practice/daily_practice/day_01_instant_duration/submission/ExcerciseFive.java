package com.lld.practice.daily_practice.day_01_instant_duration.submission;

import java.time.Instant;
import java.util.List;

/*
 * ============================================================
 * DAY 1 - EXERCISE 5: Trending Detection (Hard)
 * ============================================================
 * TOPIC: Instant + Duration — Trend Detection with Two Windows
 * DIFFICULTY: Hard
 *
 * PROBLEM:
 *   Write a method that detects if event activity is "trending" — meaning
 *   there has been a significant increase in recent activity compared to
 *   the previous period. This is exactly the logic used in P1 (Trending Courses).
 *
 * METHOD SIGNATURE:
 *   public boolean isTrending(List<Instant> eventTimes, Instant now)
 *
 * PARAMETERS:
 *   - eventTimes : list of all event timestamps (completions, views, etc.)
 *   - now        : current reference time (injected — never call Instant.now() inside!)
 *
 * RETURN:
 *   true  → recent 7 days has > 30% more events than the previous 7 days
 *   false → activity is flat or declining
 *
 * ALGORITHM:
 *   Step 1: Define two adjacent windows
 *     - Recent window   : (now - 7 days,  now]           → "last 7 days"
 *     - Previous window : (now - 14 days, now - 7 days]  → "week before that"
 *
 *   Step 2: Count events in each window
 *     - countRecent   = events in recent window
 *     - countPrevious = events in previous window
 *
 *   Step 3: Apply trending formula
 *     - return countRecent > countPrevious * 1.3
 *
 * BOUNDARY CONVENTION (used in this solution):
 *   - Recent:   isAfter(sevenDaysAgo)     and !isAfter(now)          → (7dAgo, now]
 *   - Previous: isAfter(fourteenDaysAgo)  and !isAfter(sevenDaysAgo) → (14dAgo, 7dAgo]
 *   The "7 days ago" boundary belongs to the PREVIOUS window, not the recent one.
 *
 * EXAMPLES:
 *   Case 1: 10 events in last 7 days, 3 in previous 7 days
 *     → 10 > 3 * 1.3  →  10 > 3.9  →  true  (TRENDING!)
 *
 *   Case 2: 4 events in last 7 days, 5 in previous 7 days
 *     → 4 > 5 * 1.3   →  4 > 6.5   →  false (NOT trending)
 *
 * CONNECTION TO P1:
 *   In the Trending Courses problem, "events" = course completion events.
 *   A course is trending if completions increased >30% in the recent window.
 *   This exact method maps to the core of TrendingCourseServiceImpl logic.
 * ============================================================
 */
public class ExcerciseFive {

    public boolean isTrending(List<Instant> eventTimes, Instant now) {
        Instant sevenDaysAgo = now.minus(7, java.time.temporal.ChronoUnit.DAYS);
        Instant fourteenDaysAgo = now.minus(14, java.time.temporal.ChronoUnit.DAYS);

        long countLast7Days = eventTimes.stream()
                .filter(e -> e.isAfter(sevenDaysAgo) && !e.isAfter(now))
                .count();

        long countPrev7Days = eventTimes.stream()
                .filter(e -> e.isAfter(fourteenDaysAgo) && !e.isAfter(sevenDaysAgo))
                .count();

        return countLast7Days > countPrev7Days * 1.3;
    }

    public static void main(String[] args) {
        ExcerciseFive ex = new ExcerciseFive();
        Instant now = Instant.now();

        // Case 1: 10 events in last 7 days, 3 events in previous 7 days
        // 10 > 3 * 1.3 (10 > 3.9)? YES → true (trending)
        List<Instant> eventTimes1 = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) eventTimes1.add(now.minus(i, java.time.temporal.ChronoUnit.HOURS));
        for (int i = 0; i < 3; i++) eventTimes1.add(now.minus(8 + i, java.time.temporal.ChronoUnit.DAYS));
        System.out.println("Case 1 (expect true): " + ex.isTrending(eventTimes1, now));

        // Case 2: 4 events in last 7 days, 5 events in previous 7 days
        // 4 > 5 * 1.3 (4 > 6.5)? NO → false (not trending)
        List<Instant> eventTimes2 = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) eventTimes2.add(now.minus(i, java.time.temporal.ChronoUnit.HOURS));
        for (int i = 0; i < 5; i++) eventTimes2.add(now.minus(8 + i, java.time.temporal.ChronoUnit.DAYS));
        System.out.println("Case 2 (expect false): " + ex.isTrending(eventTimes2, now));
    }
}
