# Clarifying Questions Cheatsheet — LLD Interviews

**Purpose:** A reusable mental framework to ask high-quality clarifying questions in any LLD interview, in under 5 minutes.

**Goal:** By the end of clarification, you should have enough info to draw the class diagram **without guessing**.

---

## The 6-Pillar Framework

Every LLD problem fits into these 6 pillars. Always run through them in order. Ask 1–2 questions per pillar.

### Pillar 1: Scope (W's)
Defines the boundary of what you'll build.

- **WHO** are the actors / users?
- **WHAT** operations are required vs nice-to-have?
- **WHERE** is the system used? (single machine, distributed, mobile)
- Is this MVP or production?
- Any features explicitly **out of scope**?

### Pillar 2: Entities & Cardinality
Defines the nouns and relationships.

- What are the core domain "nouns"?
- One-to-one, one-to-many, or many-to-many between entities?
- Any uniqueness / identity constraints?
- Are there sub-types of an entity? (e.g., vehicle types)

### Pillar 3: Lifecycle / States
Defines how entities evolve over time.

- What states can each entity be in?
- What are valid state transitions?
- Are reversals allowed (cancel, undo, refund)?
- Time-based transitions (expiry, auto-cleanup)?

### Pillar 4: Rules / Policies / Pricing
Defines the "how" of business logic.

- Any business rules (limits, eligibility, priority)?
- How is anything calculated (price, fee, score, ranking)?
- Allocation / matching policies (FIFO, best-fit, nearest)?
- What's the **min / max / default** for any rule?

### Pillar 5: Edge Cases & Failure Modes
Defines defensive correctness.

- What if input is null / empty / duplicate / malformed?
- What if a resource is full / not found / already used?
- What if a dependency fails (payment, network)?
- What if same operation is requested twice?

### Pillar 6: Non-Functional
Defines runtime characteristics.

- Concurrency / multi-threaded access?
- Persistence / restart recovery?
- Auth, logging, metrics, observability — in scope?
- Expected scale (req/sec, total entities)?

---

## The 3-Question Drill (per pillar)

For each pillar, run this drill:

1. **What** are the inputs / outputs?
2. **What if** something is invalid / missing / duplicate?
3. **What's NOT** in scope?

---

## Opening Template (memorize)

> "Before I start, I'd like to clarify the scope across 6 dimensions: actors and operations, entities and relationships, lifecycle and states, business rules, edge cases, and non-functional needs. Is that okay?"

This **signals structure** to the interviewer. Instant +5 score.

---

## Common Mistakes To Avoid

| Mistake | Fix |
|---|---|
| Asking implementation questions ("Should I use HashMap?") | That's design, not clarification |
| Only yes/no questions | Ask open-ended too: "How is fee calculated?" |
| Not writing assumptions | If interviewer is vague: "I'll assume X. Correct me if wrong." |
| Skipping non-functional pillar | Concurrency / persistence questions show seniority |
| Asking 20+ questions | Cap at 8 strong questions; quality > quantity |
| Asking after design starts | Front-load all clarifications before classes |

---

## Self-Grading Rubric (After Clarification)

Tick each checkbox:

- [ ] Did I cover all 6 pillars?
- [ ] Did I ask at least 1 edge-case question?
- [ ] Did I ask at least 1 non-functional question?
- [ ] Did I write any explicit assumptions where interviewer was vague?
- [ ] Do I now have enough info to draw the class diagram **without guessing**?

If any answer is "no" → you under-clarified.

---

## Time Budget (45-min interview)

| Phase | Minutes | Pillars covered |
|---|---|---|
| Clarification | 5 | All 6 |
| Assumptions | 2 | Locked in writing |
| Class design | 8 | Pillar 2 |
| Data structures | 5 | Pillar 6 |
| API + pseudocode | 15 | Pillars 3, 4, 5 |
| Design patterns | 3 | Justified call-outs |
| Tradeoffs / wrap-up | 7 | Pillar 6 |

---

## Practice Plan (1 Week)

| Day | Drill | Time |
|---|---|---|
| 1 | Take 3 random LLD problems. Write 5 questions per pillar (30 questions total). Don't solve. | 30 min |
| 2 | Read your questions aloud. Cut redundant ones. Keep top 8 per problem. | 20 min |
| 3 | Take a new problem, time yourself for 5 minutes, ask only 8 questions. | 5 min |
| 4 | Repeat day 3 with new problem. | 5 min |
| 5 | Drill 5 problems back-to-back, 5 min each. | 25 min |
| 6 | Pick a hard problem. Cover all 6 pillars in 5 mins. | 5 min |
| 7 | Mock with full timer (45 min): 5 min clarification, 40 min design. | 45 min |

**Goal by Day 7:** Fire 8 high-quality questions in **under 5 minutes** for any LLD problem.

---

## Quick Reference Card (memorize this)

```
┌────────────────────────────────────────────┐
│  6-PILLAR CHECKLIST                        │
├────────────────────────────────────────────┤
│  1. Scope        → who, what, where        │
│  2. Entities     → nouns, cardinality      │
│  3. Lifecycle    → states, transitions     │
│  4. Rules        → pricing, policy, calc   │
│  5. Edge Cases   → null, full, duplicate   │
│  6. Non-Func     → concurrency, persist    │
├────────────────────────────────────────────┤
│  3-Q DRILL: What? What if? What's NOT?     │
└────────────────────────────────────────────┘
```

---

## Examples (for muscle memory)

### Example: "Design a Library Book Catalog"

- **Scope (P1):** Single library or multi-branch? Search by title only or also by author/ISBN?
- **Entities (P2):** Book vs BookCopy? User can hold multiple books?
- **Lifecycle (P3):** Book states (available/issued/lost)? Can user reserve before issue?
- **Rules (P4):** Max books per user? Late-fee model?
- **Edge cases (P5):** What if user tries to issue same book twice? Return without issue?
- **Non-func (P6):** Concurrent issue/return? In-memory or DB?

### Example: "Design a Parking Lot"

- **Scope (P1):** Single floor or multi-level? How many entries/exits?
- **Entities (P2):** Vehicle types? Slot types? Slot-vehicle compatibility matrix?
- **Lifecycle (P3):** Ticket states (active/completed/lost)?
- **Rules (P4):** Pricing per slot type? Hourly or per-minute? Allocation policy when multiple slots fit?
- **Edge cases (P5):** Vehicle already parked? Lot full? Lost ticket fee?
- **Non-func (P6):** Concurrent entries claiming same slot?

### Example: "Design a Movie Ticket Booking"

- **Scope (P1):** One screen or multiple? One show or multiple time slots?
- **Entities (P2):** Show, Seat, Booking, User? Seat categories?
- **Lifecycle (P3):** Seat states (available, held, booked, cancelled)? Hold expiry?
- **Rules (P4):** Pricing per seat category? Discounts/coupons in scope?
- **Edge cases (P5):** Concurrent users picking same seat? Hold timeout?
- **Non-func (P6):** Concurrency must be enforced; persistence?

---

## Bottom Line

**Clarification is not asking what you don't know — it's confirming what the interviewer assumed silently.**

A senior candidate is recognized in the **first 5 minutes** by question quality. Master this framework, and you'll never miss a critical question again.

