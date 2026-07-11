package com.lld.practice.daily_practice.day_01_instant_duration.submission;

import java.time.Duration;
import java.time.Instant;

/*
 * ============================================================
 * DAY 1 - EXERCISE 1: Basic Instant Comparison
 * ============================================================
 * TOPIC: Instant + Duration
 * DIFFICULTY: Easy
 *
 * PROBLEM:
 *   Write a method that determines if an event happened within the last 24 hours.
 *
 * METHOD SIGNATURE:
 *   public static boolean isWithinLast24Hours(Instant eventTime, Instant now)
 *
 * PARAMETERS:
 *   - eventTime : when the event occurred
 *   - now       : current time (injected so tests are deterministic — never use Instant.now() inside!)
 *
 * RETURN:
 *   true  → event happened strictly less than 24 hours before now
 *   false → event happened exactly 24 hours ago OR more than 24 hours ago OR in the future
 *
 * CONSTRAINT:
 *   Use only Instant and Duration — NO LocalDate, NO ZonedDateTime
 *
 * EXAMPLES:
 *   eventTime = now - 12h  → true   (12 hours ago is within last 24h)
 *   eventTime = now - 24h  → false  (exactly 24h boundary is excluded)
 *   eventTime = now - 25h  → false  (25h ago is outside the window)
 *   eventTime = now        → true   (happening right now counts)
 *
 * KEY CONCEPT:
 *   Duration.between(eventTime, now) gives elapsed time.
 *   Use compareTo(Duration.ofHours(24)) < 0 to check strictly less than 24h.
 *   Also guard against future events with !eventTime.isAfter(now).
 * ============================================================
 */
public class ExcerciseOne {

    public static boolean isWithinLast24Hours(Instant eventTime, Instant now) {
        Duration duration = Duration.between(eventTime, now);

        return !eventTime.isAfter(now) && duration.compareTo(Duration.ofHours(24)) < 0;
    }
    
    public static void main(String[] args) {
        Instant now = Instant.now();
        System.out.println(isWithinLast24Hours(now.minus(Duration.ofHours(12)), now)); // expect: true
        System.out.println(isWithinLast24Hours(now.minus(Duration.ofHours(24)), now)); // expect: false
        System.out.println(isWithinLast24Hours(now.minus(Duration.ofHours(25)), now)); // expect: false
        System.out.println(isWithinLast24Hours(now, now));                             // expect: true
    }
}
