package com.lld.practice.daily_practice.day_01_instant_duration.submission;

import java.time.Duration;
import java.time.Instant;

/*
 * ============================================================
 * DAY 1 - EXERCISE 3: Duration Between Events
 * ============================================================
 * TOPIC: Instant + Duration — Elapsed Time Calculation
 * DIFFICULTY: Easy
 *
 * PROBLEM:
 *   Write a method that returns how many COMPLETE days have passed since an event.
 *   This is used for ranking/scoring: older events get lower scores.
 *
 * METHOD SIGNATURE:
 *   public static long daysSinceEvent(Instant eventTime, Instant now)
 *
 * PARAMETERS:
 *   - eventTime : when the event occurred (must be in the past)
 *   - now       : current reference time (injected — never call Instant.now() inside!)
 *
 * RETURN:
 *   Number of COMPLETE 24-hour periods between eventTime and now.
 *   Partial days do NOT count (floor division behavior).
 *
 * EXAMPLES:
 *   eventTime = now -  2 hours  → 0   (less than 1 full day)
 *   eventTime = now - 25 hours  → 1   (1 complete day has passed)
 *   eventTime = now -  7 days   → 7   (exactly 7 complete days)
 *
 * KEY CONCEPT:
 *   Duration.between(eventTime, now)  → total elapsed Duration
 *   duration.toDays()                 → floors to complete days (like integer division)
 *   Example: Duration of 25 hours → toDays() = 1  (not 1.04)
 *
 * TRICKY PART:
 *   toDays() does floor division, NOT rounding.
 *   47 hours → 1 day (not 2), 48 hours → 2 days.
 * ============================================================
 */
public class ExcerciseThree {

    public static long daysSinceEvent(Instant eventTime, Instant now) {
        Duration duration = Duration.between(eventTime, now);
        return duration.toDays();
    }

    public static void main(String[] args) {
        Instant now = Instant.now();
        
        Instant eventTime1 = now.minus(Duration.ofHours(2));
        System.out.println("daysSinceEvent(now - 2 hours, now) = " + daysSinceEvent(eventTime1, now));  // expect: 0
        
        Instant eventTime2 = now.minus(Duration.ofHours(25));
        System.out.println("daysSinceEvent(now - 25 hours, now) = " + daysSinceEvent(eventTime2, now)); // expect: 1
        
        Instant eventTime3 = now.minus(Duration.ofDays(7));
        System.out.println("daysSinceEvent(now - 7 days, now) = " + daysSinceEvent(eventTime3, now));   // expect: 7
    }
}
