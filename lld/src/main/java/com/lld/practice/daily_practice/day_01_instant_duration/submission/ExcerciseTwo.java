package com.lld.practice.daily_practice.day_01_instant_duration.submission;

import java.time.Duration;
import java.time.Instant;

/*
 * ============================================================
 * DAY 1 - EXERCISE 2: Rolling Window Validation
 * ============================================================
 * TOPIC: Instant + Duration — Rolling Window Pattern
 * DIFFICULTY: Easy-Medium
 *
 * PROBLEM:
 *   Write a method that checks if an event falls within a 7-day rolling window.
 *   A rolling window means: from (now - 7 days) up to now.
 *
 * METHOD SIGNATURE:
 *   public static boolean isInRollingWindow(Instant eventTime, Instant now)
 *
 * PARAMETERS:
 *   - eventTime : when the event occurred
 *   - now       : current reference time (injected — never call Instant.now() inside!)
 *
 * RETURN:
 *   true  → event is strictly inside the 7-day window
 *   false → event is at/outside the boundary, or is a future event
 *
 * BOUNDARY DECISION (important for interviews!):
 *   Window = (now - 7 days, now)  ← both ends EXCLUSIVE
 *   Reason: An event at exactly "7 days ago" is already expired.
 *           An event at exactly "now" hasn't been processed yet.
 *   This is the most common convention in time-series / trending systems.
 *
 * EXAMPLES:
 *   eventTime = now - 6 days  → true   (inside the window)
 *   eventTime = now - 7 days  → false  (boundary is exclusive — exactly at the edge)
 *   eventTime = now - 8 days  → false  (outside the window)
 *   eventTime = now + 1 day   → false  (future event — not valid)
 *
 * KEY CONCEPT:
 *   windowStart = now.minus(Duration.ofDays(7))
 *   Use isAfter(windowStart) to exclude the boundary itself.
 *   Use isBefore(now) to exclude future events.
 * ============================================================
 */
public class ExcerciseTwo {

    public static boolean isInRollingWindow(Instant eventTime, Instant now) {
        Instant start = now.minus(Duration.ofDays(7));
        return eventTime.isAfter(start) && eventTime.isBefore(now);
    }
    
    public static void main(String[] args) {
        Instant now = Instant.now();

        System.out.println(isInRollingWindow(now.minus(Duration.ofDays(6)), now)); // expect: true
        System.out.println(isInRollingWindow(now.minus(Duration.ofDays(7)), now)); // expect: false (boundary exclusive)
        System.out.println(isInRollingWindow(now.minus(Duration.ofDays(8)), now)); // expect: false
        System.out.println(isInRollingWindow(now.plus(Duration.ofDays(1)), now));  // expect: false (future)
    }
}
