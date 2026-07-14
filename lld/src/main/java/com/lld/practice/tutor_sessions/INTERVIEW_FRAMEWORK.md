# LLD Interview Framework - Staff Engineer Level

**Purpose:** Standardized workflow for 45-minute Low-Level Design interviews  
**Target Level:** Staff Engineer at Walmart / FAANG companies  
**Philosophy:** Build working system first, then evolve under constraints

---

## 🎯 INTERVIEW STRUCTURE (45 Minutes Total)

```
┌─────────────────────────────────────────────────────────────┐
│ STEP 0: Problem Understanding                    (5 min)    │
│ STEP 1: Basic Design & Implementation           (25 min)    │
│ STEP 2: Advanced Requirements                   (15 min)    │
│ STEP 3: Production Discussion                    (5 min)    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📝 STEP 0: Problem Understanding (5 minutes)

### Candidate Must Do:
- [ ] Ask clarifying questions about functional requirements
  - "What exactly should the system do?"
  - "What are the core use cases?"
  - "Are there any edge cases I should handle?"

- [ ] Ask about non-functional requirements
  - "How many concurrent users/requests?"
  - "Single-threaded or multi-threaded?"
  - "Do we need persistence or in-memory is fine?"
  - "What's the expected latency/throughput?"

- [ ] State assumptions explicitly
  - "I'm assuming single building with N elevators"
  - "I'm assuming no database, all in-memory"
  - "I'm assuming requests come one at a time for v1"

- [ ] Confirm scope and priorities
  - "Let's focus on X in v1, defer Y to v2"
  - "Should I prioritize correctness over optimization?"

### Interviewer Will Do:
- ✅ Provide clear problem statement
- ✅ Answer clarifying questions directly
- ✅ Confirm which features are in-scope vs out-of-scope
- ✅ Set expectations: "Focus on basic flow first, we'll add complexity later"

### Success Criteria:
- Candidate has clear understanding of WHAT to build
- Candidate has explicitly stated 2-3 key assumptions
- Both parties agree on v1 scope

---

## 🏗️ STEP 1: Basic Design & Implementation (25 minutes)

### Phase 1.1: Design (10 minutes)

#### Candidate Must Do:

**1. Identify Actors (Who uses the system?)**
```
Example for Elevator:
- Passenger (presses hall/cabin buttons)
- Building Manager (monitors elevators)
- Maintenance System (marks elevators out of service)
```

**2. Identify Core Entities (Domain models - nouns)**
```
Example for Elevator:
- Elevator
- Floor  
- Request (HallRequest, CabinRequest)
- Building
```

**3. Identify Services (Business logic - verbs)**
```
Example for Elevator:
- DispatcherService (assigns requests to elevators)
- ElevatorService (executes elevator movement)
- BuildingService (manages building state)
```

**4. Identify Enums/Value Objects**
```
Example for Elevator:
- ElevatorStatus (IDLE, MOVING_UP, DOORS_OPEN, OUT_OF_SERVICE)
- RequestStatus (PENDING, ASSIGNED, COMPLETED)
- Direction (UP, DOWN)
```

**5. Define API Contracts**
```java
// Example format:
public interface DispatcherService {
    /**
     * Assigns a hall request to the best available elevator
     * @param request - hall request to dispatch
     * @param elevators - available elevators in building
     * @return assigned request with ASSIGNED status
     * @throws IllegalArgumentException if request is null
     * @throws ElevatorDispatchException if no elevator available
     */
    Request dispatch(HallRequest request, List<Elevator> elevators);
}
```

**6. Document Exception Strategy**
```
RULE: Decide upfront and document

Input validation → IllegalArgumentException
Business failures → Custom domain exceptions
Infrastructure    → Runtime exceptions with specific types
```

#### Interviewer Will Do:
- ✅ Review class diagram / API design
- ✅ Ask: "Why did you choose X over Y?"
- ✅ Point out missing entities/relationships
- ✅ Confirm design is sufficient before coding starts

---

### Phase 1.2: Implementation (15 minutes)

#### Candidate Must Do:

**1. Implement Models (POJOs first)**
- [ ] Create all entity classes with proper fields
- [ ] Add getters/setters
- [ ] Add constructors
- [ ] Add toString() for debugging

**2. Implement Core Business Logic**
- [ ] One service at a time
- [ ] Start with simplest service first
- [ ] Use clear variable names
- [ ] Add inline comments for complex logic

**3. Write Inline Tests As You Go**
```java
// After implementing each method, add quick validation
public static void testDispatch() {
    Elevator e1 = new Elevator("E1", 0, IDLE, new ArrayList<>());
    HallRequest req = new HallRequest("R1", PENDING, 5, UP);
    Request result = dispatcher.dispatch(req, List.of(e1));
    assert result.getRequestStatus() == ASSIGNED;
    System.out.println("✓ Dispatch test passed");
}
```

**4. Narrate Trade-offs While Coding**
- "I'm using ArrayList for now, could optimize to PriorityQueue in v2"
- "For v1, I'm processing FIFO; production would need SCAN algorithm"
- "This is O(n) selection; could optimize with spatial indexing"

#### Interviewer Will Do:
- ✅ Point out critical bugs immediately
- ✅ Ask: "What happens if X is null?"
- ✅ Challenge assumptions: "What if two requests come at same time?"
- ❌ Will NOT write code for candidate
- ❌ Will NOT allow over-engineering in v1

#### Success Criteria at 25-min mark:
- ✅ All models compiled and correct
- ✅ Core business flow works end-to-end
- ✅ At least 1 inline test validates behavior
- ✅ Code is readable and well-named

---

## 🚀 STEP 2: Advanced Requirements (15 minutes)

### Interviewer Will Give ONE Challenge:

Choose from:

**Option A: Design Pattern Integration**
```
Examples:
- "Add Observer pattern so external monitoring can listen to elevator events"
- "Use Strategy pattern to make dispatch algorithm pluggable"  
- "Add Factory pattern to create different elevator types (Freight, Passenger, Express)"
- "Implement Chain of Responsibility for request validation"
```

**Option B: Concurrency Handling**
```
Examples:
- "Make dispatcher thread-safe for concurrent hall requests"
- "Handle race condition when two elevators try to pick same request"
- "Add synchronized execution so elevator doesn't skip floors"
- "Implement reader-writer lock for elevator status updates"
```

**Option C: Performance Optimization**
```
Examples:
- "Implement SCAN algorithm to reduce total elevator movement"
- "Add caching layer for repeated floor lookups"
- "Optimize request matching using spatial data structure"
- "Implement request batching to reduce dispatcher overhead"
```

### Candidate Must Do:

**1. Identify Applicable Pattern/Technique**
- [ ] Ask interviewer for hint if unsure which pattern
- [ ] Explain why this pattern fits the problem
- [ ] Describe structure before coding

**2. Implement Minimal Working Version**
- [ ] Modify existing code (don't rewrite from scratch)
- [ ] Keep changes localized to 2-3 files maximum
- [ ] Ensure backward compatibility if possible

**3. Explain Trade-offs**
```
Example narrative:
"Observer pattern adds flexibility - external systems can subscribe to events.
Trade-off: Adds complexity and potential memory leaks if observers aren't cleaned up.
In production, I'd use weak references or explicit unsubscribe lifecycle."
```

**4. Validate New Behavior**
- [ ] Add quick test showing new functionality works
- [ ] Verify existing tests still pass

### Interviewer Will Do:
- ✅ Help identify which pattern/technique to use (if candidate is stuck)
- ✅ Evaluate how cleanly candidate evolves code vs rewrites
- ✅ Ask about alternatives: "What other patterns could work here?"
- ✅ Push on edge cases: "What if observer throws exception?"

### Success Criteria at 40-min mark:
- ✅ Advanced feature is implemented and working
- ✅ Existing functionality still works
- ✅ Candidate can explain why this approach was chosen
- ✅ Code changes are clean and minimal

---

## 💬 STEP 3: Production Discussion (5 minutes)

### Interviewer Will Ask:

**1. Scale & Performance:**
- "How would this scale to 1000 elevators in one building?"
- "What's the bottleneck in your current design?"
- "How would you handle 1 million requests per day?"

**2. Failure Modes:**
- "What happens if dispatcher crashes mid-assignment?"
- "What if elevator gets stuck between floors?"
- "How do you prevent request starvation?"

**3. Operational Concerns:**
- "What monitoring/metrics would you add?"
- "How would you debug a stuck elevator in production?"
- "What would you log for observability?"

### Candidate Should Answer:

**V3 Improvements:**
```
"Next iteration would add:
1. Persistent queue (Kafka/RabbitMQ) for request durability
2. Circuit breaker pattern for fault tolerance
3. Distributed caching (Redis) for shared state across buildings"
```

**Monitoring Strategy:**
```
"I'd add metrics for:
- Average wait time per request
- Elevator utilization percentage  
- Request timeout rate
- Dispatch decision time
Use Prometheus + Grafana for dashboards"
```

**Failure Handling:**
```
"For crash recovery:
- Persist pending requests to database
- On restart, reload and re-dispatch
- Add idempotency tokens to prevent duplicate assignments"
```

---

## 📋 MANDATORY CHECKLISTS

### Before Each Interview:
- [ ] Set strict 45-minute timer
- [ ] Have code editor + terminal ready
- [ ] Review last weakness plan
- [ ] Memorize exception strategy framework

### During Step 1 (Basic):
- [ ] Write exception strategy comment first
- [ ] One test per major method
- [ ] Narrate trade-offs out loud
- [ ] Check timer at 10-min and 20-min marks

### During Step 2 (Advanced):
- [ ] Ask: "Which pattern should I use?" if unsure
- [ ] Modify existing code, don't rewrite
- [ ] Explain why this pattern fits
- [ ] Verify backward compatibility

### After Interview:
- [ ] Record what went well
- [ ] Record what needs improvement  
- [ ] Update weakness tracking
- [ ] Plan next practice session

---

## 🎓 PATTERN REFERENCE GUIDE

### Common Patterns for LLD Problems:

| Pattern | When to Use | Example |
|---------|-------------|---------|
| **Strategy** | Multiple algorithms, choose at runtime | Different dispatch algorithms (FIFO, SCAN, Priority) |
| **Observer** | Event notifications to multiple listeners | Elevator status changes notify monitoring systems |
| **Factory** | Create objects of different types | Create Freight/Passenger/Express elevators |
| **Singleton** | Single shared instance | DispatcherService per building |
| **State** | Object behavior changes with state | Elevator behavior differs by status (IDLE vs MOVING) |
| **Command** | Encapsulate requests as objects | Request object pattern (already used) |
| **Chain of Responsibility** | Multiple handlers in sequence | Request validation chain |
| **Decorator** | Add behavior without modifying class | Add logging/monitoring to existing services |

### Concurrency Patterns:

| Technique | When to Use | Example |
|-----------|-------------|---------|
| **synchronized** | Protect critical sections | Dispatcher assignment logic |
| **ReentrantLock** | More control than synchronized | Elevator queue modifications |
| **AtomicReference** | Lock-free updates | Elevator status changes |
| **ConcurrentHashMap** | Thread-safe cache | Floor → Elevator mapping |
| **ExecutorService** | Manage thread pools | Process multiple requests concurrently |
| **CountDownLatch** | Wait for multiple threads | Wait for all elevators to initialize |

---

## 🎯 EVALUATION RUBRIC (How You'll Be Scored)

### Technical Execution (40%)
- ✅ Code compiles and runs
- ✅ Core functionality works correctly
- ✅ Handles edge cases
- ✅ Code is clean and readable
- ✅ Proper OOP principles (encapsulation, SRP)

### System Thinking (30%)
- ✅ Correct entities and relationships
- ✅ Clean separation of concerns
- ✅ Scalability awareness
- ✅ Mentions failure modes
- ✅ Discusses trade-offs

### Design Pattern Usage (15%)
- ✅ Identifies correct pattern
- ✅ Implements pattern correctly
- ✅ Explains why pattern fits
- ✅ Minimal code changes

### Communication (15%)
- ✅ Asks clarifying questions
- ✅ Narrates thinking process
- ✅ Explains trade-offs
- ✅ Responds to feedback
- ✅ Organized and structured approach

### Staff-Level Bar:
- **Pass:** 70%+ across all dimensions
- **Strong Pass:** 85%+ with proactive pattern/concurrency mention
- **Borderline:** 60-69% — correct code but weak system thinking

---

## 📝 PROBLEM TEMPLATE (Interviewer Uses)

```markdown
# PROBLEM: [System Name]

## FUNCTIONAL REQUIREMENTS:
1. Requirement 1 (detailed)
2. Requirement 2 (detailed)
3. ...

## NON-FUNCTIONAL REQUIREMENTS:
- Scale: X users, Y requests/sec
- Latency: < Z ms per operation
- Concurrency: Single-threaded / Multi-threaded
- Persistence: In-memory / Database required

## OUT OF SCOPE (Defer to v2/v3):
- Authentication/Authorization
- UI/Frontend
- Distributed deployment
- [Other features]

## YOUR TASK - STEP 1 (25 min):
Implement basic working system with:
- Actors: ?
- Entities: ?
- Services: ?
- Exception handling: ?

## ADVANCED REQUIREMENT - STEP 2 (15 min):
[One focused challenge - given after Step 1 complete]

Options:
A) Add [Design Pattern X] to solve [specific problem]
B) Make [Component Y] thread-safe for [concurrency scenario]
C) Optimize [Operation Z] using [specific technique]

## DISCUSSION - STEP 3 (5 min):
- Production readiness
- Scale to [large number]
- Failure modes
- Monitoring strategy
```

---

## 🚀 GETTING STARTED

### First-Time Setup:
1. Read this framework completely
2. Review one completed example (Elevator System)
3. Memorize exception strategy rules
4. Memorize 3-4 common design patterns
5. Practice v1/v2 narrative template

### Before Each Problem:
1. Review weakness plan from last problem
2. Set 45-minute timer
3. Have this framework open for reference
4. Clear workspace (no distractions)

### After Each Problem:
1. Interviewer provides strict feedback
2. Update WEAKNESS_AND_IMPROVEMENT_PLAN.md
3. Mark progress in tracking table
4. Identify 1-2 action items for next session

---

## ✅ COMMITMENT

**As Interviewer, I Will:**
- ✅ Always follow this 3-step structure
- ✅ Give ONE focused advanced challenge (not multiple)
- ✅ Help identify patterns if candidate is stuck
- ✅ Provide strict, actionable feedback
- ✅ Never write code for candidate
- ✅ Keep candidate on track with time

**As Candidate, I Will:**
- ✅ Clarify requirements before coding (Step 0)
- ✅ Design before implementing (Phase 1.1)
- ✅ Write tests alongside code (Phase 1.2)
- ✅ Narrate trade-offs out loud (all phases)
- ✅ Ask for hints when stuck (not freeze)
- ✅ Complete weakness exercises between sessions

---

**This framework is your contract for every LLD practice session.**  
**Master it, and you'll reliably clear Staff-level interviews.** 💪

---

*Document Version: 1.0*  
*Last Updated: July 14, 2026*  
*Next Review: After 3 practice problems*

