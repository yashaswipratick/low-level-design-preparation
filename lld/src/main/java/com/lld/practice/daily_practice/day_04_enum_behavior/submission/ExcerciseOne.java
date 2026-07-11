package com.lld.practice.daily_practice.day_04_enum_behavior.submission;

/*
 * ============================================================
 * DAY 4 - EXERCISE 1: Enum with Field + Getter (Pattern 1)
 * ============================================================
 * DIFFICULTY: Beginner
 * TOPIC: Enum carrying a value — field + constructor + getter
 *
 * CONTEXT:
 *   In the P1 Trending Courses problem, the Timeframe enum represented
 *   a rolling window (WEEKLY=7, MONTHLY=30, etc.). But it had NO fields —
 *   so the caller had to write if/else to get the number of days.
 *   That is a design bug. The enum should carry its own data.
 *
 *   In this exercise, you will fix that by building a proper enum
 *   where each constant holds its own value internally.
 *
 * YOUR TASK:
 *   Complete the Timeframe enum so that:
 *   - Each constant stores its window size in days
 *   - A getter getDays() returns that value
 *   No if/else or switch is allowed anywhere outside the enum.
 *
 * ENUM TO COMPLETE:
 *   enum Timeframe {
 *       WEEKLY,     // 7 days
 *       MONTHLY,    // 30 days
 *       QUARTERLY;  // 90 days
 *
 *       // TODO: Add private field, constructor, and getter
 *   }
 *
 * RULES:
 *   - The enum must have a private final int field
 *   - The enum must have a constructor that takes int days
 *   - The enum must have a getDays() getter
 *   - Each constant must pass its days value: WEEKLY(7), etc.
 *   - NO if/else or switch allowed in getWindowDays() below
 *
 * EXAMPLE:
 *   Timeframe.WEEKLY.getDays()    → 7
 *   Timeframe.MONTHLY.getDays()   → 30
 *   Timeframe.QUARTERLY.getDays() → 90
 *
 * KEY CONCEPT:
 *   Enum constants CAN have constructors:
 *     WEEKLY(7)  →  calls Timeframe(int days) with days=7
 *
 * CONNECTION TO P1:
 *   In TrendingCourseServiceImpl, you do:
 *     now.minus(timeframe.getDays(), ChronoUnit.DAYS)
 *   instead of:
 *     if (timeframe == WEEKLY) ... else if (timeframe == MONTHLY) ...
 * ============================================================
 */
public class ExcerciseOne {

    // ---- Enum to complete ----
    enum Timeframe {
        WEEKLY(7),     // should carry 7
        MONTHLY(30),    // should carry 30
        QUARTERLY(90);  // should carry 90

        // TODO 1: Add a private final int field called 'days'
        private final int days;

        // TODO 2: Add a constructor: Timeframe(int days)
        Timeframe(int days) {
            this.days = days;
        }

        // TODO 3: Add a getter: public int getDays()
        public int getDays() {
            return this.days;
        }
        // TODO 4: Update each constant to pass its value: WEEKLY(7), MONTHLY(30), QUARTERLY(90)

        // Stub so the file compiles before you implement — replace this with your real getter
    }

    // ---- Method to complete ----
    public static int getWindowDays(Timeframe timeframe) {
        // TODO: Return the number of days for the given timeframe
        // RULE: No if/else, no switch — must call timeframe.getDays() directly
        return timeframe.getDays();
    }

    public static void main(String[] args) {
        System.out.println("=== Exercise 1: Enum with Field + Getter ===");
        System.out.println();

        System.out.println("Test 1 | Timeframe.WEEKLY    → Expected: 7");
        try {
            System.out.println("         Actual: " + getWindowDays(Timeframe.WEEKLY));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual: TODO not implemented yet");
        }

        System.out.println("Test 2 | Timeframe.MONTHLY   → Expected: 30");
        try {
            System.out.println("         Actual: " + getWindowDays(Timeframe.MONTHLY));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual: TODO not implemented yet");
        }

        System.out.println("Test 3 | Timeframe.QUARTERLY → Expected: 90");
        try {
            System.out.println("         Actual: " + getWindowDays(Timeframe.QUARTERLY));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual: TODO not implemented yet");
        }

        // Bonus: verify all values in one loop — no if/else needed
        System.out.println();
        System.out.println("--- All timeframes (loop test) ---");
        try {
            for (Timeframe t : Timeframe.values()) {
                System.out.println(t.name() + " → " + t.getDays() + " days");
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("Loop test: TODO not implemented yet");
        }
    }
}

