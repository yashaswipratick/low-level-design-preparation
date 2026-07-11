# Day 1 Notes — Instant + Duration + Rolling Windows

**Date Practiced:** May 21, 2026
**Topic:** Java Time API — `Instant`, `Duration`, Rolling Window Pattern

---

## Core Concepts Learned

### 1. `Instant` — What Is It?
- `Instant` represents a **point in time on the UTC timeline** (nanosecond precision).
- Think of it as a **timestamp** — not tied to any timezone or calendar.
- Use `Instant` when you need to record **when** something happened (e.g., course completion time).
- **Never** call `Instant.now()` inside your business logic methods — always inject `now` as a parameter so tests are deterministic.

### 2. `Duration` — What Is It?
- `Duration` represents an **amount of time** (hours, minutes, seconds, nanoseconds).
- Think of it as the **gap between two Instants**.
- Key factory methods:
  ```java
  Duration.ofHours(24)   // 24 hours
  Duration.ofDays(7)     // 7 days
  Duration.between(startInstant, endInstant)  // elapsed time
  ```

### 3. Arithmetic on `Instant`
```java
Instant past   = now.minus(Duration.ofDays(7));   // 7 days ago
Instant future = now.plus(Duration.ofDays(7));    // 7 days from now
Instant past2  = now.minus(7, ChronoUnit.DAYS);   // same as above
```

### 4. Comparing `Instant` Values
```java
a.isAfter(b)   // a > b
a.isBefore(b)  // a < b
a.equals(b)    // a == b (exact same nanosecond)

// Inclusive/Exclusive check examples:
// event >= start  →  !event.isBefore(start)
// event <= now    →  !event.isAfter(now)
// event > start   →  event.isAfter(start)
// event < now     →  event.isBefore(now)
```

---

## Rolling Window Pattern (Core Pattern for P1)

### What Is a Rolling Window?
A rolling window is a time range that moves with "now":
```
|-------- 7 days --------|
[now - 7 days]         [now]
       ↑ windowStart    ↑ current time
```

### How to Build One
```java
Instant windowStart = now.minus(days, ChronoUnit.DAYS);

// Exclusive boundaries (open interval):
boolean inWindow = event.isAfter(windowStart) && event.isBefore(now);

// Inclusive boundaries (closed interval):
boolean inWindow = !event.isBefore(windowStart) && !event.isAfter(now);
```

### When to Use Exclusive vs Inclusive?
| Use Case | Boundary | Why |
|---|---|---|
| "Is this event still active/fresh?" | Exclusive | Events at exact boundaries are considered expired |
| "Count all events in this report period" | Inclusive | Boundary events belong to this period |

---

## Exercise Summaries

### Ex 1 — `isWithinLast24Hours(eventTime, now)`
- **Pattern:** `!eventTime.isAfter(now) && Duration.between(eventTime, now) < 24h`
- **Key mistake to avoid:** Not guarding against future events
- **Result:** ✅ All test cases passed

### Ex 2 — `isInRollingWindow(eventTime, now)` (7-day window)
- **Pattern:** `event.isAfter(start) && event.isBefore(now)` — both EXCLUSIVE
- **Boundary decision:** Exclusive — event at exactly `now - 7 days` returns `false`
- **Result:** ✅ All test cases passed

### Ex 3 — `daysSinceEvent(eventTime, now)`
- **Pattern:** `Duration.between(eventTime, now).toDays()`
- **Key trick:** `toDays()` does floor division — 47h → 1 day, NOT 2
- **Result:** ✅ All test cases passed

### Ex 4 — `countEventsInWindow(eventTimes, now, days)`
- **Pattern:** Count events where `!event.isBefore(windowStart) && !event.isAfter(now)`
- **Bug in original spec:** Exercise said 90-day window → 3, but actual answer is **4**
  - `now - 65 days` is within 90 days → it should be counted ✅ Code was correct!
- **Result:** ✅ Code correct (spec had a typo)

### Ex 5 — `isTrending(eventTimes, now)` (Hard)
- **Pattern:** Compare two adjacent 7-day windows using 30% growth threshold
- **Two windows:**
  ```
  Recent:   (now - 7d,  now]      → "last 7 days"
  Previous: (now - 14d, now - 7d] → "week before that"
  ```
- **Formula:** `countRecent > countPrevious * 1.3`
- **Connection to P1:** This is the core of `TrendingCourseServiceImpl`
- **Result:** ✅ All test cases passed

---

## Common Mistakes to Watch Out For

1. **Using `<` instead of `<=` or vice versa** — Always be explicit about inclusive/exclusive boundaries and explain your choice in interviews.

2. **Calling `Instant.now()` inside the method** — Makes the method non-deterministic and untestable. Always inject `now`.

3. **Using `LocalDate` instead of `Instant`** — `LocalDate` has no time component and timezone ambiguity. Use `Instant` for timestamps.

4. **Forgetting to guard against future events** — `Duration.between(futureEvent, now)` returns a negative duration. Always check `!eventTime.isAfter(now)` first.

5. **Confusing `toDays()` with rounding** — `Duration.ofHours(47).toDays()` = 1, not 2. It floors.

---

## Key APIs Quick Reference

```java
// Create Instant
Instant now = Instant.now();
Instant past = now.minus(7, ChronoUnit.DAYS);
Instant past = now.minus(Duration.ofDays(7));  // same

// Create Duration
Duration d = Duration.ofDays(7);
Duration d = Duration.between(start, end);

// Inspect Duration
d.toDays()     // complete days (floor)
d.toHours()    // complete hours (floor)
d.toMillis()   // total milliseconds

// Compare Duration
d.compareTo(Duration.ofHours(24)) < 0  // d < 24h

// Compare Instants
a.isAfter(b)    // a > b  (strictly)
a.isBefore(b)   // a < b  (strictly)
!a.isAfter(b)   // a <= b (inclusive)
!a.isBefore(b)  // a >= b (inclusive)
```

---

## Connection to P1: Trending Courses

| P1 Concept | Day 1 Exercise |
|---|---|
| Filter completions by timeframe | Ex 2 / Ex 4 |
| Count completions in window | Ex 4 |
| Detect trending (30% growth) | Ex 5 |
| Rolling 7d / 30d / 90d windows | Ex 4 (parameterized days) |

Day 1 exercises are direct building blocks for the P1 solution! 🎯

