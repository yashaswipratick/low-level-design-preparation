# 🎯 My Weaknesses & Focused Learning Plan
### Based on P1 Attempt 1 — Ulearn Trending Courses

> **Current Score: 56 / 100** | **Target: 80+ / 100**  
> Read this file every morning before your practice session.

---

## 📊 Score Breakdown (Where I Bled Points)

```
Correctness      ████████░░░░░░░░░░░░  20 / 40  (-20)
LLD Quality      ██████░░░░░░░░░░░░░░  14 / 25  (-11)
Edge Cases       ████░░░░░░░░░░░░░░░░   8 / 15  ( -7)
Code Quality     █████░░░░░░░░░░░░░░░   9 / 20  (-11)
Communication    ████░░░░░░░░░░░░░░░░   5 / 15  (-10)
                                      ──────────────
TOTAL            ████████████░░░░░░░░  56 / 100 (-44)
```

**What this tells you:** You understood the broad problem but failed on precision — wrong types, wrong keys, wrong model design.

---

## 🔴 Weakness 1 — Wrong Java Time API (`LocalDate` vs `Instant`)

> **Points lost: ~10** | **Frequency: Will happen in EVERY timestamp problem**

### What happened
You used `LocalDate` for `completedAt` — a **date-only** type with no hour/minute.  
The problem needed **exact timestamp comparison** to verify `windowStart <= event <= now`.

### The Core Confusion

```
LocalDate      →  2026-05-19              (date only — NO time)
LocalDateTime  →  2026-05-19T10:00        (date + time, NO timezone)
Instant        →  2026-05-19T10:00:00Z    (exact moment, UTC — USE THIS)
Duration       →  PT24H / P7D             (time amount — for window sizes)
```

### When to Use What

| Type | Use for | Example |
|---|---|---|
| `Instant` | Event timestamps, rolling windows, logs | `completedAt`, `createdAt` |
| `Duration` | Time-based window size | `Duration.ofDays(7)` |
| `LocalDate` | Calendar dates (no time needed) | Birthday, holiday |
| `Period` | Calendar-based duration | "1 month later" on a calendar |

### The Rolling Window Pattern (Memorize)

```java
// Step 1: compute window start
Instant windowStart = now.minus(timeframe.getDuration());

// Step 2: inclusive boundary check
boolean inWindow = !event.getCompletedAt().isBefore(windowStart)
                && !event.getCompletedAt().isAfter(now);
```

### ✏️ Practice Drills (Do 3 times this week)

1. Write a method `isWithinLast(Instant event, int days, Instant now)` using `Instant` + `Duration`
2. Filter a list of login events to the last 7 days
3. Filter a list of transactions to the last 30 days

---

## 🔴 Weakness 2 — Collect-Then-Count Pattern (Map + Set)

> **Points lost: ~12** | **Most common data structure in interview problems**

### What happened
You tried to **count inside the loop** instead of **collecting first, counting after**.  
Result: broken deduplication logic and wrong counts.

### The Mental Model

```
❌ WRONG THINKING:
   "When I see an event, I'll count it"

✅ CORRECT THINKING:
   "When I see an event, I'll remember which users completed which course.
    After all events are processed, I'll count."
```

### The Pattern (Write This From Memory)

```java
// 1. Collect phase
Map<String, Set<String>> uniqueUsersPerCourse = new HashMap<>();
for (CompletionEvent event : completionEvents) {
    uniqueUsersPerCourse
        .computeIfAbsent(event.getCourseId(), k -> new HashSet<>())
        .add(event.getUserId());
}

// 2. Count phase
for (Map.Entry<String, Set<String>> entry : uniqueUsersPerCourse.entrySet()) {
    int count = entry.getValue().size();   // ← count comes HERE, after loop
}
```

### Why `computeIfAbsent` is Correct

```java
// Without computeIfAbsent (verbose but same thing)
if (!map.containsKey(courseId)) {
    map.put(courseId, new HashSet<>());
}
map.get(courseId).add(userId);

// With computeIfAbsent (clean, interview-ready)
map.computeIfAbsent(courseId, k -> new HashSet<>()).add(userId);
```

### Same Pattern Appears In These Problems

| Problem | Group By | Unique Count |
|---|---|---|
| Trending courses | courseId | unique userIds |
| Trending hashtags | hashtagId | unique userIds |
| Most active IPs | endpoint | unique IPs |
| Top sellers | sellerId | unique productIds sold |
| Voter count | candidateId | unique voterIds |

### ✏️ Practice Drills

1. Count unique users per country from a list of login events
2. Count unique products viewed per user from a list of page views
3. Count unique comment authors per post from a list of comments

---

## 🔴 Weakness 3 — Enum Should Carry Behavior

> **Points lost: ~6** | **Shows up in every LLD problem**

### What happened
You wrote `Timeframe` as a plain constant enum with no logic.  
Then in the service, you wrote this:

```java
// ❌ Your code — if/else explosion
if (Timeframe.ONE_DAY.equals(timeframe)) {
    startTime = today.minusDays(1);
} else if (Timeframe.ONE_WEEK.equals(timeframe)) {
    startTime = today.minusDays(7);
} else if (Timeframe.ONE_MONTH.equals(timeframe)) {
    startTime = today.minusMonths(1);
}
```

**Problem:** Every new timeframe = surgery in the service. This is not extensible.

### The Fix (Enum with Behavior)

```java
// ✅ Correct — enum carries its own logic
public enum Timeframe {
    ONE_DAY(Duration.ofDays(1)),
    ONE_WEEK(Duration.ofDays(7)),
    ONE_MONTH(Duration.ofDays(30));

    private final Duration duration;

    Timeframe(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}

// Service becomes ONE LINE:
Instant windowStart = now.minus(timeframe.getDuration());
```

### Enum Patterns You Must Know

```java
// Pattern 1: Enum with field + getter
enum Priority { LOW(1), MEDIUM(5), HIGH(10);
    Priority(int weight) { this.weight = weight; }
    public int getWeight() { return weight; }
    private final int weight;
}

// Pattern 2: Enum with behavior method
enum Discount { FLAT, PERCENT;
    public double apply(double price, double value) {
        return this == FLAT ? price - value : price * (1 - value/100);
    }
}

// Pattern 3: Enum with abstract method
enum Operation { ADD { public int apply(int a, int b) { return a+b; } },
                 MUL { public int apply(int a, int b) { return a*b; } };
    public abstract int apply(int a, int b);
}
```

### ✏️ Practice Drills

Write these enums with behavior (no if/else allowed in service):
1. `NotificationType` (EMAIL, SMS, PUSH) — each with `getMaxRetries()` returning different int
2. `OrderStatus` (PENDING, SHIPPED, DELIVERED, CANCELLED) — each with `isTerminal()` boolean
3. `RateLimitWindow` (PER_SECOND, PER_MINUTE, PER_HOUR) — each with `getDuration()`

---

## 🟠 Weakness 4 — Input Model vs Output Model Confusion

> **Points lost: ~8** | **Core LLD design principle**

### What happened
You put `CompletionEvent` inside `CourseTrend`. This is a design error.

```
❌ Your design:
   CourseTrend {
       Course course;
       CompletionEvent event;   ← WRONG: raw event inside output object
   }

✅ Correct design:
   CourseTrend {
       Course course;
       int completionCount;     ← CORRECT: aggregated result, not raw event
   }
```

### The 3-Layer Model Rule

```
┌─────────────────────────────────────────────────────┐
│  Layer 1: Input/Event Model                         │
│  → Raw data coming in                               │
│  → CompletionEvent(userId, courseId, completedAt)   │
│  → Contains fields exactly as received              │
├─────────────────────────────────────────────────────┤
│  Layer 2: Domain Model                              │
│  → Core business entities                           │
│  → Course(courseId, courseName)                     │
│  → Stable, shared across the system                 │
├─────────────────────────────────────────────────────┤
│  Layer 3: Output/Response Model                     │
│  → Aggregated result going out                      │
│  → CourseTrend(course, completionCount)             │
│  → NEVER contains Layer 1 objects directly          │
└─────────────────────────────────────────────────────┘
```

### Quick Rule
> **Output models hold computed/aggregated data. Input models hold raw events. Never mix them.**

### ✏️ Practice Drills

Identify input model vs output model for:

| System | Input Event | Output Model |
|---|---|---|
| Trending hashtags | `HashtagUse(userId, hashtag, usedAt)` | `HashtagTrend(hashtag, count)` |
| Top sellers | `Sale(sellerId, productId, amount, soldAt)` | `SellerRank(seller, totalRevenue)` |
| Active employees | `LoginEvent(empId, loginAt)` | `EmployeeStat(employee, loginCount)` |
| Movie ratings | `Rating(userId, movieId, score, ratedAt)` | `MovieTrend(movie, avgScore)` |

---

## 🟠 Weakness 5 — Vague Requirement Answers Under Pressure

> **Points lost: ~10** | **First 5 minutes of interview = 30% of score**

### What happened

| Question | Your answer | Correct answer |
|---|---|---|
| Timeframes? | "One Week, One Month, One Year" | `ONE_DAY`, `ONE_WEEK`, `ONE_MONTH` |
| Ranking by? | "Descending order" | "Unique user completion count DESC" |
| Unknown courseId? | "NA" | "Skip the event safely, do not crash" |
| Boundary? | "Inclusive" | "`windowStart <= completedAt <= now`" |

### The Precision Formula

```
❌ Vague:   "Handle it internally"
✅ Precise: "If courseId is not in courseMap, skip the event and continue to next.
             Do not throw exception."

❌ Vague:   "Sort descending"
✅ Precise: "Sort by unique completion count descending.
             On tie, sort by course name ascending (A-Z)."

❌ Vague:   "Inclusive boundary"
✅ Precise: "Include events where windowStart <= completedAt <= now,
             using Instant comparison."
```

### Interview Narration Scripts (Say These Out Loud)

**On timeframe:**
> "I'll treat ONE_DAY as a rolling 24-hour window from `now`, not a calendar day. The boundary is inclusive on both ends."

**On deduplication:**
> "If the same user completes the same course twice in the window, that counts as one unique completion. I'll use a `Set<userId>` per course to enforce this."

**On unknown courseId:**
> "If an event references a courseId that doesn't exist in my course map, I skip that event safely. No crash, no exception."

**On time type:**
> "I'm using `Instant` for timestamps, not `LocalDate`, because I need sub-day precision for the boundary check."

### ✏️ Practice Drill (Daily, 2 minutes)

Pick any system (real app, news story, anything). Answer these 5 questions in precise technical language:
1. **INPUT** — what type? what fields?
2. **OUTPUT** — what type? what fields?
3. **SORT** — primary key? direction? tie-break?
4. **INVALID** — what happens for null/empty/negative?
5. **BOUNDARY** — inclusive or exclusive? what is the edge case?

---

## 🟡 Weakness 6 — Constructor Argument Order Bugs

> **Points lost: ~5** | **Silent bug that corrupts data without compile error**

### What happened

```java
// Course.java — you wrote:
public Course(String courseName, String courseId) { ... }   // ← name first

// TrendingCourse.java — you called:
new Course("C1", "Java")   // ← intended id first, name second

// Result: courseId = "Java", courseName = "C1"   ← SILENT SWAP
```

### The Rule

```
Always write constructor as: (id, name) — id first.
Always read call site left-to-right before moving on.
```

```java
// ✅ Correct constructor
public Course(String courseId, String courseName) { ... }

// ✅ Verify call site mentally
new Course("C1", "Java")
             ↑    ↑
           id    name    ← reads correctly
```

### ✏️ Practice Drill

Every time you write a constructor, pause and do this:
1. Write constructor with `id` as first argument
2. Write a test call: `new ClassName("id_value", "name_value")`
3. Read it left-to-right → does it make sense?

---

## 📅 2-Week Focused Learning Plan

### Week 1 — Core Java + Data Structure Patterns

| Day | Topic | What to do |
|---|---|---|
| **Mon** | `Instant`, `Duration`, rolling windows | Write 5 rolling window filters from scratch |
| **Tue** | `Map<K, Set<V>>` collect-then-count | Write unique-count for 3 problems from memory |
| **Wed** | `Comparator` multi-key sorting | Write count DESC + name ASC comparator from memory |
| **Thu** | Enum with fields + methods | Write 3 enums with behavior |
| **Fri** | All combined | Rewrite `TrendingCourseServiceImpl` from scratch in 20 min |

### Week 2 — LLD Design + Interview Communication

| Day | Topic | What to do |
|---|---|---|
| **Mon** | Input vs Output model design | Identify 5 systems' layer-3 model |
| **Tue** | Requirement precision | Answer 5 questions for 3 problems with full technical precision |
| **Wed** | Constructor discipline | Write 5 POJOs, verify every constructor call site |
| **Thu** | Narration practice | Record yourself explaining P1 design in 3 minutes |
| **Fri** | **P1 Attempt 2 (45 min timed)** | Submit for review — target 80+ |

---

## ✅ Pre-Interview Quick Check (30 seconds before every attempt)

```
□ Am I using Instant for timestamps? (not LocalDate)
□ Is my enum carrying getDuration()? (no if/else in service)
□ Is courseMap keyed by courseId? (not courseName)
□ Is CourseTrend holding completionCount (int)? (not CompletionEvent)
□ Is constructor order: (id, name)? (not reversed)
□ Am I using fixed now in main? (not Instant.now())
□ Did I write 5 assumptions before touching code?
```

---

## 🧠 Patterns to Write From Memory Weekly

### Pattern 1: Collect + Count unique per group
```java
Map<String, Set<String>> map = new HashMap<>();
map.computeIfAbsent(groupId, k -> new HashSet<>()).add(userId);
int count = map.get(groupId).size();
```

### Pattern 2: Rolling window with Instant
```java
Instant windowStart = now.minus(timeframe.getDuration());
if (!event.getTime().isBefore(windowStart) && !event.getTime().isAfter(now)) {
    // event is in window
}
```

### Pattern 3: Multi-key sort (count desc, name asc)
```java
list.sort(
    Comparator.comparingInt(CourseTrend::getCompletionCount).reversed()
              .thenComparing(t -> t.getCourse().getCourseName())
);
```

### Pattern 4: Enum with duration
```java
public enum Window {
    ONE_DAY(Duration.ofDays(1)),
    ONE_WEEK(Duration.ofDays(7));
    private final Duration duration;
    Window(Duration d) { this.duration = d; }
    public Duration getDuration() { return duration; }
}
```

### Pattern 5: topN safe slice
```java
return list.stream().limit(topN).collect(Collectors.toList());
// or
return list.subList(0, Math.min(topN, list.size()));
```

---

## 🎯 Final Reminder

```
┌────────────────────────────────────────────────────────────────┐
│                                                                │
│   Your biggest problem is NOT that you can't code.            │
│   Your biggest problem is:                                     │
│                                                                │
│   1. Wrong types under pressure (LocalDate vs Instant)        │
│   2. Skipping the collect → aggregate mental model            │
│   3. Putting logic in service that belongs in enum/model      │
│   4. Vague requirement answers in first 5 minutes             │
│                                                                │
│   Fix these 4 things = you go from 56 → 80+ easily.          │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

**Next action:** Do `Week 1 - Monday` drill today. Write 5 rolling window filters using `Instant` + `Duration`. Take 20 minutes. Do it now.

