# Elevator System Interview - Weakness Analysis & Improvement Plan

**Date:** July 14, 2026  
**Problem:** Elevator System Design  
**Current Level:** Borderline Staff (7/10 Technical, 6/10 System Thinking)  
**Target:** Strong Staff Pass (9/10)

---

## 🔴 CRITICAL WEAKNESSES (Must Fix Before Next Interview)

### 1. Exception Strategy Inconsistency
**What happened:**
- Used `IllegalArgumentException` for input validation (lines 19, 23, 32)
- Used `ElevatorDispatchException` for business failure (line 68)
- But the boundary wasn't clearly explained or documented

**Why it matters at Staff level:**
- Shows unclear thinking about error boundaries
- Staff engineers must design consistent error handling strategies
- Real systems need predictable exception hierarchies for monitoring/alerting

**How to fix:**
```
RULE: Document your exception strategy at the start of implementation

Pre-validation (inputs):     → IllegalArgumentException (unchecked)
Business rule violations:     → Domain exceptions (checked or unchecked, be consistent)
Infrastructure failures:      → Runtime exceptions with specific types

PRACTICE: Before writing any service method, write a comment:
  // Throws: IllegalArgumentException if request is null
  // Throws: ElevatorDispatchException if no elevator available
```

**Action items:**
- [ ] In next practice, write exception documentation FIRST
- [ ] Create 2-3 custom domain exceptions and use them consistently
- [ ] Practice verbalizing: "I use IllegalArgumentException for contract violations, domain exceptions for business failures"

---

### 2. Missing Primary Dispatch Test Case
**What happened:**
- Your dispatcher has logic for "moving elevator that can pick request on the way" (primary rule)
- But all tests only dispatched to IDLE elevators
- The most complex logic path was never validated

**Why it matters at Staff level:**
- Staff engineers must think adversarially about their own code
- The most complex path is where bugs hide
- Shows lack of thoroughness in validation

**How to fix:**
```
MENTAL MODEL: Always test complex paths first, simple paths second

Complex path test for Dispatcher:
1. E1 at floor 2, MOVING_UPWARDS, going to floor 8
2. Request comes: floor 5, UP
3. Verify: E1 is selected (not E2 which is idle at floor 10)
4. Verify: E1 stops at floor 5, picks up passenger

PRACTICE: After implementing selection logic, create test for:
- Best case (moving elevator picks on way) ✅
- Fallback case (nearest idle) ✅  
- Failure case (no elevator) ✅
```

**Action items:**
- [ ] Re-run elevator test with moving elevator scenario
- [ ] In next LLD, write tests in parallel with implementation (not after)
- [ ] Use template: "Happy path → Edge case → Failure path" for every method

---

## ⚠️ MAJOR GAPS (Shows Mid vs Staff Difference)

### 3. No SCAN Algorithm Mention
**What's missing:**
- Current implementation processes requests one-by-one from index 0
- Real elevators use SCAN: collect ALL requests in current direction before reversing
- Example: Going UP, elevator should pick floor 3, 5, 7 requests, not just floor 3

**Why it matters at Staff level:**
- Staff must proactively mention optimization paths even if not implementing
- Shows understanding of real-world system behavior
- Demonstrates trade-off thinking (simplicity vs efficiency)

**How to fix:**
```
INTERVIEW NARRATIVE TEMPLATE (use this verbatim):

"For v1, I'm processing requests FIFO for simplicity and correctness.
In v2, I'd implement SCAN algorithm:
- Sort elevator queue by floor number in movement direction
- Collect all UP requests while moving UP
- Reverse direction only when no more requests in current direction
Trade-off: SCAN is more efficient (less movement) but adds complexity in request ordering."

PRACTICE: After finishing basic implementation, always say:
"This works correctly. For production, I'd optimize X. Trade-off is Y vs Z."
```

**Action items:**
- [ ] Memorize the SCAN narrative template above
- [ ] In next practice, implement basic SCAN (sort elevator queue by floor)
- [ ] Practice saying trade-offs out loud while coding

---

### 4. No Starvation Prevention Discussion
**What's missing:**
- What if all elevators are busy for 10 minutes?
- What if a request keeps getting skipped by moving elevators?
- No mechanism to prevent request from waiting forever

**Why it matters at Staff level:**
- Real production systems must handle pathological cases
- Staff engineers think about failure modes proactively
- Shows systems thinking beyond happy path

**How to fix:**
```
INTERVIEW NARRATIVE (after implementing dispatch):

"Current limitation: A request could wait indefinitely if elevators are busy.
For production, I'd add:
1. Request timeout (after 2 min, mark as priority)
2. Priority queue (older requests get higher weight in selection)
3. Dispatcher polling (retry PENDING requests every 30 seconds)

Trade-off: Adds complexity but prevents starvation in high-load scenarios."

PRACTICE: For every design, ask yourself:
"What breaks under load? How do I prevent it?"
```

**Action items:**
- [ ] Read about priority queues and request aging
- [ ] In next practice, add a "timestamp" field to Request
- [ ] Implement simple priority: `score = distance + (currentTime - requestTime) / 1000`

---

### 5. Missing Concurrency Discussion
**What's missing:**
- What if 2 hall requests come at the same time?
- What if Dispatcher and ElevatorService both modify same elevator?
- No mention of threading, locks, or synchronization

**Why it matters at Staff level:**
- Real elevator systems are concurrent (multiple requests, multiple elevators moving)
- Staff must identify shared mutable state and race conditions
- Even if not implementing, must mention the issue

**How to fix:**
```
INTERVIEW NARRATIVE (when asked about scalability):

"Current design assumes single-threaded execution.
For multi-threaded system, I'd add:
1. Synchronized access to elevator queue (ConcurrentLinkedQueue or synchronized methods)
2. Atomic status updates (use AtomicReference<ElevatorStatus>)
3. Dispatcher lock per building (only one dispatch at a time per building)

Alternative: Event-driven architecture with message queue (Kafka/RabbitMQ)
- Dispatcher publishes assignment events
- ElevatorService consumes and processes
- Naturally thread-safe, horizontally scalable

Trade-off: Locking is simpler but limits throughput; event-driven scales better but adds infrastructure complexity."

PRACTICE: Always mention concurrency even if not asked directly.
```

**Action items:**
- [ ] Read Java concurrency basics (synchronized, AtomicReference, ConcurrentHashMap)
- [ ] In next practice, add `synchronized` keyword to critical methods
- [ ] Practice saying: "For concurrency, I'd synchronize elevator queue access"

---

## 📋 NEXT PRACTICE CHECKLIST

### Before Starting Next LLD Problem:

**Set Interview Timer:**
- [ ] 45 minutes strict timer
- [ ] 5 min requirements
- [ ] 10 min models
- [ ] 20 min core logic
- [ ] 10 min edge cases + narration

**Document Exception Strategy First:**
- [ ] Write comment: "Exception strategy: X for inputs, Y for business failures"
- [ ] Create domain exception class before implementing service

**Write Tests in Parallel:**
- [ ] After implementing each method, write 1 test immediately
- [ ] Don't wait until end

**Use V1/V2 Narrative:**
- [ ] After core implementation, verbalize: "V1 is simple, V2 would add SCAN/priority/concurrency"
- [ ] Practice out loud, not just in head

---

## 🎯 PRACTICE EXERCISES (Do Before Next Mock Interview)

### Exercise 1: Exception Consistency (15 min)
Go back to your DispatcherService and:
1. Create a single `ElevatorValidationException` for ALL failures
2. Replace all IllegalArgumentException with it
3. Document in Javadoc: what each method throws and why

### Exercise 2: SCAN Implementation (30 min)
Modify ElevatorServiceImpl to:
1. Sort `elevator.getRequests()` by destination floor
2. Process in order based on current movement direction
3. Test: 3 requests (floors 2, 5, 8), elevator at floor 1 going UP
   - Should visit 2 → 5 → 8, not 2 → (random)

### Exercise 3: Moving Elevator Dispatch Test (20 min)
Add test case to ElevatorSystemDriver:
```java
// Setup: E1 at floor 3, MOVING_UPWARDS, has request for floor 10
elevator1.setFloor(3);
elevator1.setElevatorStatus(ElevatorStatus.MOVING_UPWARDS);
HallRequest existingReq = new HallRequest("R0", RequestStatus.ASSIGNED, 10, Direction.UP);
elevator1.getRequests().add(existingReq);

// New request: floor 7, UP (E1 should pick it on the way)
HallRequest newReq = new HallRequest("R5", RequestStatus.PENDING, 7, Direction.UP);
Request assigned = dispatcher.dispatch(newReq, elevators);

// Verify: newReq was assigned to E1 (not E2 which is idle)
assert elevator1.getRequests().contains(newReq);
```

### Exercise 4: Concurrency Narrative Practice (10 min)
Read this scenario, then write (by hand) your response:
> "Interviewer: Your dispatcher and elevator service will run on separate threads. What breaks?"

Expected answer should mention:
- Shared mutable state (elevator queue)
- Race condition (dispatcher adding while service removing)
- Solution (synchronized methods or concurrent data structures)

---

## 📝 INTERVIEW DAY CHECKLIST

**Before whiteboard/coding:**
- [ ] Clarify requirements (functional, non-functional, scale)
- [ ] Write exception strategy comment
- [ ] Draw class diagram (even if just box-and-arrow)

**While coding:**
- [ ] Narrate trade-offs as you code ("I'm using List for now, could use PriorityQueue later")
- [ ] Write one test per major method
- [ ] Check timer every 10 minutes

**After core implementation:**
- [ ] Verbalize v2 improvements (SCAN, priority, concurrency)
- [ ] Ask: "Should I optimize X or move to Y?"

**If stuck:**
- [ ] Ask for hint, don't freeze
- [ ] Verbalize your thinking ("I'm considering approach A vs B because...")

---

## 🎓 LEARNING RESOURCES

**For SCAN Algorithm:**
- Wikipedia: "Elevator algorithm" (SCAN, LOOK, C-SCAN)
- Practice: Implement basic sorting of requests by floor number

**For Concurrency:**
- Java concurrency tutorial (synchronized, volatile, AtomicReference)
- Practice: Add synchronized keyword to 3 methods in your code

**For Priority Queues:**
- Java PriorityQueue documentation
- Practice: Replace List<Request> with PriorityQueue<Request> using custom Comparator

**For System Design Thinking:**
- Read "Designing Data-Intensive Applications" Chapter 1 (Reliability, Scalability)
- Practice: For every feature, ask "What breaks under load?"

---

## 📊 PROGRESS TRACKING

| Weakness | Status | Target Date | Notes |
|----------|--------|-------------|-------|
| Exception consistency | 🔴 Not started | Next session | Create single domain exception |
| Moving elevator test | 🔴 Not started | Next session | Add test case to driver |
| SCAN narrative | 🔴 Not started | This week | Memorize template |
| Starvation discussion | 🔴 Not started | This week | Practice saying it out loud |
| Concurrency mention | 🔴 Not started | This week | Read Java concurrency basics |

Legend: 🔴 Not started | 🟡 In progress | 🟢 Mastered

---

## 🚀 CONFIDENCE BUILDER

**What you did RIGHT in this interview:**
- ✅ Identified HallRequest vs Request polymorphism issue proactively
- ✅ Asked clarifying questions about dispatcher input
- ✅ Implemented clean state machine for elevator execution
- ✅ Wrote functionally correct code that passed all tests
- ✅ Used deterministic tie-breaking (elevatorId comparison)

**You're 80% there.** The remaining 20% is:
1. Proactive trade-off narration (5%)
2. Comprehensive test coverage (5%)
3. Consistent exception strategy (5%)
4. Concurrency awareness mention (5%)

All of these are **communication and awareness**, not coding skill. You can master them in 2-3 more practice sessions.

---

**Next Action:** Pick one exercise from above and complete it NOW (while fresh). Don't wait.

**Recommendation:** Do Exercise 3 (moving elevator test) right now. It takes 20 min and validates your most complex logic path. Then tomorrow do Exercise 1 (exception consistency).

You've got this! 💪

