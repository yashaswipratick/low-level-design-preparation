# P1 - Ulearn Trending Courses: Complete Revision Guide

> Read this before your next attempt or before any interview.  
> This captures every mistake, correction, and lesson learned from Attempt 1.

---

## How to Use This File

1. Read **Section 1** to warm up your assumptions thinking.
2. Read **Section 2** to lock in the correct class structure.
3. Read **Section 3** to rehearse the correct pseudocode flow.
4. Read **Section 4** to identify exactly where your code broke.
5. Read **Section 5** to know what to say out loud in the interview.
6. Read **Section 6** for your attempt 2 checklist.

---

## Section 1: Requirements & Assumptions (Step 1)

### What you wrote first (first attempt — incomplete/incorrect)

```
Problem: Design and implement an in-memory backend module for Ulearn.
Input: Timeframe
Output: List of Course details sorted in Descending order
Timeframes: One Week, One Month, One Year        ← WRONG (should be ONE_DAY, ONE_WEEK, ONE_MONTH)
Ranking: Descending Order                        ← INCOMPLETE (descending by what?)
Tie-breaker: lexicographical                     ← CORRECT
Invalid topN: Empty results                      ← CORRECT
Boundary rule: Inclusive                         ← CORRECT but imprecise
Unknown courseId: NA                             ← WRONG (should be: skip safely, no crash)
```

### What you corrected (after guidance)

```
Problem: Design an in-memory backend to return top trending courses for a given timeframe.

Input: Timeframe timeframe, int topN, Instant now
       (plus in-memory course map + completion events as source data)

Output: List<CourseTrend>
        Each item contains: Course (id, name) + completionCount

Timeframes: ONE_DAY, ONE_WEEK, ONE_MONTH

Ranking primary: Unique user completion count DESC

Tie-breaker: Course name ASC (lexicographical)

Invalid topN: If topN <= 0, return empty list

Boundary rule: windowStart <= completedAt <= now (inclusive)

Unknown courseId: Skip the event safely; do not crash
```

### Your 5 Assumptions (final correct version)

1. Same user completing same course multiple times in timeframe counts only once.
2. Only courses with at least one valid completion in window are returned.
3. If `topN` > available courses, return all available.
4. Time window is computed backward from `now` using timeframe duration.
5. Output is deterministic: count DESC, then course name ASC.

### Gaps found in Step 1
| What you missed | Correct answer |
|---|---|
| Timeframe values | ONE_DAY, ONE_WEEK, ONE_MONTH (not ONE_YEAR) |
| Input is not just Timeframe | Full signature: `Timeframe, int topN, Instant now` |
| Output precision | `List<CourseTrend>` = Course(id, name) + completionCount |
| Ranking primary | Unique user count, not just "descending" |
| Unknown courseId | Skip safely, not just "NA" or "handle internally" |

---

## Section 2: Minimal Class Structure (Step 2)

### What you wrote first (incomplete/wrong)

```
CompletionEvent:
- courseId
- UserId
- count              ← WRONG: no count field; count is computed by aggregation

CourseTrend:
- Course
- CompletionEvent    ← WRONG: should hold completionCount (int), not event object
```

### Correct class structure (locked)

```
Timeframe (enum):
- ONE_DAY
- ONE_WEEK
- ONE_MONTH
- getDuration() -> Duration      ← IMPORTANT: used to compute windowStart

Course:
- courseId (String)
- courseName (String)
- constructor(courseId, courseName)   ← ORDER MATTERS: id first, name second

CompletionEvent:
- userId (String)
- courseId (String)
- completedAt (Instant)              ← USE Instant, NOT LocalDate

CourseTrend:
- course (Course)
- completionCount (int)              ← primitive int, not Integer; not event object

TrendingCourseService (interface):
- List<CourseTrend> getTopTrendingCourses(Timeframe timeframe, int topN, Instant now)

TrendingCourseServiceImpl:
- Map<String, Course> courseMap      ← keyed by courseId, NOT courseName
- List<CompletionEvent> completionEvents
- getTopTrendingCourses(...)
- addCourse(Course course)
- addCompletionEvent(CompletionEvent event)

TrendingCourse (main entry):
- main(String[] args)
```

### Gaps found in Step 2
| What you missed | Correct answer |
|---|---|
| `CompletionEvent.count` | No count field; each event = one event; count comes from `Set.size()` |
| `CourseTrend.CompletionEvent` | `CourseTrend` holds `completionCount (int)`, not the event object |
| `Timeframe` has methods | `getDuration()` is needed to compute rolling window |
| `completedAt` type | Must be `Instant`, not `LocalDate` |
| Service method name | `getTopTrendingCourses(...)` not `getTrendingCourses(...)` |

---

## Section 3: Pseudocode (Step 3 + 4)

### What you wrote (partial — missing key aggregation steps)

```
// Your first attempt
Loop over all the completion event:
    if completion event is within timeframe:
        if courseId is unknown then skip it
        else calculate the count     ← WRONG: you can't "calculate count" inside loop
```

### Why "calculate count inside loop" is wrong
- Each event represents ONE user completing ONE course.
- You don't count inside the loop — you collect unique users per course.
- The actual count emerges AFTER the loop, from `set.size()`.

### Correct final pseudocode (memorize this)

```
function getTopTrendingCourses(timeframe, topN, now):

    1. if topN <= 0 or timeframe == null or now == null:
           return empty list

    2. windowStart = now - timeframe.getDuration()

    3. Create Map<courseId, Set<userId>>

    4. for each event in completionEvents:
           if event is null or event fields are null:
               continue
           if courseId not in courseMap:
               continue             ← skip unknown course
           if event.completedAt < windowStart or event.completedAt > now:
               continue             ← outside window
           uniqueUsersPerCourse[courseId].add(userId)

    5. Create List<CourseTrend>
       for each (courseId, userSet) in map:
           course = courseMap.get(courseId)
           completionCount = userSet.size()
           add CourseTrend(course, completionCount) to list

    6. Sort list by:
           - completionCount DESC
           - courseName ASC (tie-break)

    7. return list[0 .. min(topN, list.size())]
```

---

## Section 4: Code Bugs From Attempt 1 (Critical Review)

### Bug 1 (Critical): Wrong time type — LocalDate instead of Instant
**Where:** `CompletionEvent.completedAt`, `TrendingCourseServiceImpl` filtering  
**What happened:** Used `LocalDate` (date only) → loses hour/minute precision.  
**Impact:** Boundary checks cannot work correctly.  
**Fix:**
```java
// WRONG
private LocalDate completedAt;

// CORRECT
private Instant completedAt;
```

And in service:
```java
// WRONG
if (!completedAt.isBefore(startTime)) { ... }

// CORRECT
Instant windowStart = now.minus(timeframe.getDuration());
if (!event.getCompletedAt().isBefore(windowStart) && !event.getCompletedAt().isAfter(now)) { ... }
```

---

### Bug 2 (Critical): Timeframe hardcoded with if/else instead of enum method
**Where:** `TrendingCourseServiceImpl.getTopTrendingCourses()` lines 28-36  
**What happened:** Used manual if/else to compute duration.  
**Impact:** Not extensible. Adding new timeframe = surgery.  
**Fix:**
```java
// WRONG
if (Timeframe.ONE_DAY.equals(timeframe)) {
    startTime = today.minusDays(1);
} else if ...

// CORRECT
Instant windowStart = now.minus(timeframe.getDuration());
```
Timeframe enum should carry its own duration:
```java
ONE_DAY(Duration.ofDays(1)),
ONE_WEEK(Duration.ofDays(7)),
ONE_MONTH(Duration.ofDays(30));
```

---

### Bug 3 (Critical): courseMap keyed by courseName, events use courseId
**Where:** `TrendingCourseServiceImpl.addCourse()` line 58  
**What happened:** Stored course by `courseName`, but event lookup uses `courseId`.  
**Impact:** `courseMap.containsKey(event.getCourseId())` always returns false.  
**Fix:**
```java
// WRONG
courseMap.put(course.getCourseName(), course);

// CORRECT
courseMap.put(course.getCourseId(), course);
```

---

### Bug 4 (Critical): CourseTrend built with synthetic null Course
**Where:** `TrendingCourseServiceImpl` stream line 50  
**What happened:** `new Course(null, entry.getKey())` creates a Course with null name.  
**Impact:** Tie-breaker sorts by null → tie order is wrong.  
**Fix:**
```java
// WRONG
.map(entry -> new CourseTrend(new Course(null, entry.getKey()), entry.getValue().size()))

// CORRECT
.map(entry -> new CourseTrend(courseMap.get(entry.getKey()), entry.getValue().size()))
```

---

### Bug 5 (High): Course constructor argument order swapped
**Where:** `Course.java` constructor, `TrendingCourse.java` line 16-18  
**What happened:** Constructor was `(courseName, courseId)` but was called as `(id, name)`.  
**Impact:** `courseId` and `courseName` get swapped silently — silent data corruption.  
**Fix:**
```java
// WRONG constructor
public Course(String courseName, String courseId) { ... }

// CORRECT constructor
public Course(String courseId, String courseName) { ... }
```

---

### Bug 6 (Medium): Non-deterministic demo using Instant.now()
**Where:** `TrendingCourse.java` lines 30-32  
**What happened:** Used `Instant.now()` in `main()` instead of fixed `now`.  
**Impact:** Output changes depending on when you run it — can't verify against expected.  
**Fix:**
```java
// WRONG
System.out.println(trendingCourseService.getTopTrendingCourses(Timeframe.ONE_DAY, 3, Instant.now()));

// CORRECT
Instant now = Instant.parse("2026-05-19T12:00:00Z");
System.out.println(trendingCourseService.getTopTrendingCourses(Timeframe.ONE_DAY, 3, now));
```

---

## Section 5: What to Say Out Loud in the Interview

These are exact things to verbalize while coding. Interviewers score you on communication.

### During requirement clarification (first 5 min)
> "I'll treat the timeframe as a rolling window. ONE_DAY means last 24 hours from `now`, not calendar day. Boundaries are inclusive."

> "If the same user completes the same course twice in the window, I count them once. I'll use a `Set<userId>` per course to handle deduplication."

> "For tie-breaking, if two courses have the same count, I'll sort by course name ascending alphabetically."

> "If `topN` is zero or negative, I return an empty list immediately as an invalid input guard."

### When choosing data types
> "I'm using `Instant` for timestamps, not `LocalDate`, because I need sub-day precision for the inclusive boundary check."

### When explaining service logic
> "My service holds `Map<courseId, Course>` keyed by courseId, and a `Map<courseId, Set<userId>>` built at query time. This gives me O(E) for the loop and O(C log C) for sorting."

### When explaining Timeframe design
> "I put `getDuration()` on the `Timeframe` enum itself so I never have to write if/else branches in the service. Adding a new timeframe is just adding one enum constant."

---

## Section 6: Attempt 2 Pre-Coding Checklist

Work through this before writing any Java code:

- [ ] Read problem 3 times
- [ ] Fill out the WHAT / CONSTRAINTS / RANKING / TIES / INVALIDS template
- [ ] Write 5 assumptions on paper
- [ ] Draw class diagram with correct field types (Instant, not LocalDate)
- [ ] Write pseudocode for the service method (7 steps above)
- [ ] Confirm `courseMap` key = `courseId`
- [ ] Confirm `completedAt` = `Instant`
- [ ] Confirm constructor = `Course(courseId, courseName)` not reversed
- [ ] Confirm `CourseTrend` = `(Course, int completionCount)` not `(Course, CompletionEvent)`
- [ ] Confirm `Timeframe` enum has `getDuration()`
- [ ] Use fixed `now` in `main`

---

## Section 7: Score Summary

| Category | Attempt 1 Score | Max | Gap |
|---|---|---|---|
| Correctness | 20 | 40 | -20 (time type, tie-break, courseMap key) |
| LLD quality | 14 | 25 | -11 (Timeframe without duration, null CourseTrend) |
| Edge cases | 8 | 15 | -7 (unknown courseId not skipped, null guards) |
| Code quality | 9 | 20 | -11 (naming, constructor order, non-determinism) |
| Communication | 5 | 15 | -10 (LocalDate vs Instant not justified, no tradeoffs) |
| **Total** | **56** | **100** | **-44** |

**Target for Attempt 2: >= 80 / 100**

---

## Section 8: Key Patterns to Memorize

### Deduplication pattern (unique users per course)
```java
Map<String, Set<String>> uniqueUsersPerCourse = new HashMap<>();
uniqueUsersPerCourse
    .computeIfAbsent(event.getCourseId(), key -> new HashSet<>())
    .add(event.getUserId());
// count = uniqueUsersPerCourse.get(courseId).size()
```

### Rolling window check
```java
Instant windowStart = now.minus(timeframe.getDuration());
if (completedAt.isBefore(windowStart) || completedAt.isAfter(now)) continue;
```

### Sorting comparator
```java
Comparator.comparingInt(CourseTrend::getCompletionCount)
          .reversed()
          .thenComparing(t -> t.getCourse().getCourseName())
```

### TopN slice
```java
.limit(topN)
// or
list.subList(0, Math.min(topN, list.size()))
```

---

## Quick Reference: Correct Final Output (expected)

Using `now = 2026-05-19T12:00:00Z`, `topN = 3`:

| Timeframe | Expected |
|---|---|
| ONE_DAY | Java=2, Python=1 (rolling 24h from now) |
| ONE_WEEK | Java=3, Python=2, Go=1 |
| ONE_MONTH | Java=3, Python=3, Go=1 (tie → Java < Python alphabetically) |

> Note: ONE_DAY returns Java=2 (not 3) because U3's event is at `2026-05-18T11:00:00Z`, which is >24h before `2026-05-19T12:00:00Z` in a rolling window.

---

**Read this file once before every reattempt. After 2-3 reads, these patterns become muscle memory.**

