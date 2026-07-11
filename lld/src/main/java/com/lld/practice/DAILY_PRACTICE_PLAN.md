# 🏋️ Daily Practice Plan — Start Tomorrow Morning

> This file tells you **exactly what to practice each day**.  
> Every session: ask me for the question → implement → submit → I review.

---

## 🔴 Priority Order (Do in This Exact Sequence)

```
Phase 1 → Java Fundamentals (3 days)
Phase 2 → Data Structure Patterns (3 days)
Phase 3 → LLD Design Patterns (3 days)
Phase 4 → P1 Attempt 2 + Review (1 day)
```

---

## 📅 Phase 1 — Java Fundamentals (Days 1–3)

### Day 1 — Java Time API (`Instant`, `Duration`, rolling windows)

**Why:** You used `LocalDate` in P1. This will fail every timestamp problem.

**What to ask me:**
> "Give me 5 Java Time API exercises on Instant, Duration, and rolling windows."

**What I will give you:**
- 5 coding exercises, increasing difficulty
- Each exercise: write the method, I verify output

**Target skills after Day 1:**
- [ ] Know when to use `Instant` vs `LocalDate` vs `LocalDateTime`
- [ ] Write rolling window check: `windowStart <= event <= now`
- [ ] Use `now.minus(Duration.ofDays(7))` correctly
- [ ] Use `isBefore()` and `isAfter()` on `Instant`

---

### Day 2 — Java Collections (`Map`, `Set`, `computeIfAbsent`)

**Why:** You counted inside the loop in P1. The collect-then-count pattern is in every problem.

**What to ask me:**
> "Give me 5 exercises on Map and Set — specifically collect-then-count pattern."

**What I will give you:**
- 5 hands-on problems using `Map<K, Set<V>>`
- Each problem: write the loop, I verify

**Target skills after Day 2:**
- [ ] Write `Map<String, Set<String>>` collect-then-count from memory
- [ ] Use `computeIfAbsent(key, k -> new HashSet<>()).add(value)`
- [ ] Extract count as `set.size()` after loop
- [ ] Know when `Map<K, Integer>` vs `Map<K, Set<V>>` is right

---

### Day 3 — Java Sorting (`Comparator`, multi-key sort)

**Why:** Sorting by count DESC + name ASC is in almost every ranking problem.

**What to ask me:**
> "Give me 5 exercises on Java Comparator — single key, multi-key, and reversed sorting."

**What I will give you:**
- 5 sorting exercises with different criteria
- Edge cases: nulls, ties, reverse order

**Target skills after Day 3:**
- [ ] Write `Comparator.comparingInt(...).reversed()` from memory
- [ ] Chain `.thenComparing(...)` for tie-break
- [ ] Sort a list in-place with `.sort(comparator)`
- [ ] Use method references in comparator

---

## 📅 Phase 2 — Data Structure Patterns (Days 4–6)

### Day 4 — Enum with Behavior

**Why:** Your `Timeframe` enum had no logic. Every LLD problem needs enums that carry state/behavior.

**What to ask me:**
> "Give me 3 enum design exercises where the enum must carry fields and methods."

**What I will give you:**
- 3 enum design problems (no if/else allowed in the caller)
- Review against open/closed principle

**Target skills after Day 4:**
- [ ] Write enum with constructor + field + getter from memory
- [ ] Never write if/else for enum-based logic in service
- [ ] Know Pattern 1 (field+getter), Pattern 2 (behavior method), Pattern 3 (abstract method)

---

### Day 5 — Input Model vs Output Model Design

**Why:** You put `CompletionEvent` inside `CourseTrend`. This is a design anti-pattern.

**What to ask me:**
> "Give me 4 model design exercises — I need to identify and separate input, domain, and output models."

**What I will give you:**
- 4 systems to design (e.g. trending hashtags, top sellers)
- I check your 3-layer model separation

**Target skills after Day 5:**
- [ ] Identify input/event model vs output/response model
- [ ] Never put raw event inside output model
- [ ] Output model only holds aggregated/computed data

---

### Day 6 — POJO Design (Constructor Order, Field Types, Naming)

**Why:** Your constructor was reversed. Silent bugs like this are invisible until runtime.

**What to ask me:**
> "Give me 5 POJO design exercises — focus on constructor order, field types, and naming discipline."

**What I will give you:**
- 5 class design exercises
- I will verify field type choices and constructor correctness

**Target skills after Day 6:**
- [ ] Always write constructor as `(id, name, ...)`
- [ ] Always verify call site mentally before moving on
- [ ] Use `Instant` for timestamps, `int` for counts, `String` for ids
- [ ] Name fields consistently: `courseId`, not `id` or `cid`

---

## 📅 Phase 3 — LLD Design Patterns (Days 7–9)

### Day 7 — Clarifying Requirements + Assumption Writing

**Why:** You gave vague answers in Walmart and in our session. First 5 minutes = 30% of score.

**What to ask me:**
> "Give me a raw machine-coding problem statement. I will clarify requirements and write 5 assumptions. You review."

**What I will give you:**
- 1 raw problem statement (no hints)
- I grade your 5 questions and 5 assumptions

**Target skills after Day 7:**
- [ ] Ask precise WHAT / CONSTRAINTS / RANKING / TIES / INVALIDS questions
- [ ] Write assumptions as typed, actionable statements
- [ ] No vague words: "handle", "NA", "internally"

---

### Day 8 — Pseudocode + Flow Design

**Why:** You coded before thinking. Pseudocode prevents mid-implementation confusion.

**What to ask me:**
> "Give me a service method to pseudocode. No Java — only logic steps."

**What I will give you:**
- 2 service problems to pseudocode
- I check logic order, missing steps, and edge case coverage

**Target skills after Day 8:**
- [ ] Write 7-step pseudocode for any ranking/filtering service
- [ ] Never miss the collect → aggregate → sort → limit order
- [ ] Identify missing edge cases before coding

---

### Day 9 — Interview Narration (Say It Out Loud)

**Why:** You had correct logic but couldn't justify it. Communication = 15% of score.

**What to ask me:**
> "Give me a 3-minute narration drill for P1 Ulearn Trending Courses. I will say it out loud and text it back."

**What I will give you:**
- A prompt to narrate your design
- I grade on: precision, justification of types, tradeoffs mentioned

**Target skills after Day 9:**
- [ ] Narrate data type choices (Instant vs LocalDate)
- [ ] Narrate data structure choices (Map<K,Set<V>>)
- [ ] Narrate complexity (O(E) loop, O(C log C) sort)
- [ ] State tradeoffs clearly

---

## 📅 Phase 4 — P1 Attempt 2 (Day 10)

### Day 10 — Timed 45-minute Attempt (P1)

**What to ask me:**
> "I am starting P1 Attempt 2. Start my timer. Give me the problem statement."

**Rules:**
- 45 minutes exactly, no hints
- Submit: all files + console output + 5 assumptions + tradeoffs

**I will review:**
- Critical bugs first
- LLD gaps
- Interview communication feedback
- New score vs Attempt 1

**Target: 80+ / 100**

---

## 📋 How Every Session Works

```
1. You open this file and check today's topic
2. You message me:
   "Today is Day [X]. Give me [topic] exercises."
3. I give you the problem(s)
4. You implement
5. You submit: code + output
6. I review: bugs → design → communication → score
7. We fix and move on
```

---

## 📌 Topics Quick Reference

| Day | Topic | Ask Me |
|---|---|---|
| 1 | Java Time API | "Give me Instant + Duration exercises" |
| 2 | Map + Set patterns | "Give me collect-then-count exercises" |
| 3 | Comparator sorting | "Give me multi-key sort exercises" |
| 4 | Enum with behavior | "Give me enum design exercises" |
| 5 | Input vs Output model | "Give me model separation exercises" |
| 6 | POJO design | "Give me POJO constructor exercises" |
| 7 | Requirements + Assumptions | "Give me a raw problem to clarify" |
| 8 | Pseudocode flow | "Give me a service to pseudocode" |
| 9 | Interview narration | "Give me a narration drill" |
| 10 | P1 Attempt 2 | "Start my P1 Attempt 2 timer" |

---

## ⚠️ Rules (Non-Negotiable)

- Start at **Day 1 tomorrow morning**. No skipping.
- Complete all exercises in a session before asking for the next day.
- Submit even if incomplete — partial review is better than no review.
- If you miss a day, you redo it the next morning. No shortcuts.
- **Missing 2 consecutive days = Strike. Recovery task assigned.**

---

## 🎯 End Goal

```
After 10 days:
  ✓ P1 score: 80+ / 100
  ✓ Can implement a ranking system in 45 minutes
  ✓ Asks precise clarifying questions in first 5 minutes
  ✓ Can narrate design, types, and tradeoffs confidently
  ✓ Ready to move to P2: Trending Instructors
```

