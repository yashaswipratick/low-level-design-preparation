# Pillar 6: EXTENSIBILITY & DESIGN PATTERNS

> **Goal:** For any LLD problem, identify the axes that will change, the design patterns that absorb that change, and the abstraction boundaries that keep the design open-for-extension — in under 60 seconds — using one repeatable 6-step pattern.

---

## The ONE pattern (memorize this, ignore everything else)

For every design, walk these **6 steps in order**:

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 1  AXES OF CHANGE       What is LIKELY to vary?           │
│                              (pricing, eviction, dispatch,     │
│                              payment, channel, algorithm)      │
│                                                                 │
│ Step 2  PATTERN MATCH        Which GoF pattern absorbs it?     │
│                              (Strategy, Factory, Observer,     │
│                              State, Decorator, CoR, Composite) │
│                                                                 │
│ Step 3  ABSTRACTION BOUNDARY What's STABLE vs SWAPPABLE?       │
│                              (interface + impls; what does     │
│                              the core depend on?)              │
│                                                                 │
│ Step 4  OPEN/CLOSED CHECK    Adding a new variant — does       │
│                              existing code CHANGE, or just     │
│                              EXTEND? (new class, no edits)     │
│                                                                 │
│ Step 5  DEPENDENCY DIRECTION High-level depends on             │
│                              ABSTRACTION, not concretion.      │
│                              (DIP — inject, don't `new`)       │
│                                                                 │
│ Step 6  TRADE-OFFS / YAGNI   Is the abstraction WARRANTED?     │
│                              (one impl = premature; ≥2 likely  │
│                              variants = justified)             │
└─────────────────────────────────────────────────────────────────┘
```

### The 4 pattern flavors (vocabulary)

| Flavor | Meaning | Example patterns |
|---|---|---|
| **Behavioral** | How objects interact / who decides | Strategy, State, Observer, Command, Chain of Responsibility, Template Method, Iterator |
| **Structural** | How objects compose | Decorator, Adapter, Composite, Facade, Proxy |
| **Creational** | How objects come into being | Factory, Builder, Singleton, Prototype, Abstract Factory |
| **Concurrency** | How threads coordinate | Producer-Consumer, Thread Pool, Read-Write Lock, Future/Promise |

### Pattern → Java representation

| Pattern | Java choice |
|---|---|
| **Strategy** | `interface X` + N impls, injected via constructor |
| **Factory** | `static of(...)` or `Factory.create(type)` |
| **Observer** | `List<Listener>` + `EventBus`/`publish/subscribe` |
| **State** | `State` interface; context delegates to current state |
| **Decorator** | wrapper impl of same interface, holds inner ref |
| **Singleton** | `enum Singleton { INSTANCE }` (preferred) or holder idiom |
| **Builder** | fluent `Builder` inner class, `.build()` returns immutable |
| **Adapter** | wraps third-party type into our interface |
| **Template Method** | `abstract` class with `final` skeleton + hook methods |
| **Command** | `Command` interface (`execute`/`undo`) + invoker / queue |
| **Chain of Responsibility** | handler with `next` ref, or `List<Handler>` loop |
| **Composite** | `Component` interface; `Leaf` + `Composite` (has children) |

> **Consistency rule:** Every axis identified in Pillar 1 (clarification) should map to a pattern here. Every State in Pillar 3 should be reviewed for the **State** pattern. Every "policy / algorithm / mode" in Pillar 4 should be reviewed for **Strategy**. Every "event" in Pillar 2/3 should be reviewed for **Observer**. Don't introduce a pattern without ≥2 likely variants (YAGNI).

---

## How to use the per-domain blocks below

```
DOMAIN
  Axes:        what will vary
  Patterns:    P1 (where), P2 (where), …
  Boundary:    key interface(s) — what's stable vs swappable
  Open/Closed: adding a new variant looks like …
  ⚠️ Trap:     …
```

---

## DOMAIN BLOCKS

### 1. Library

- **Axes:** fine calculation; notification channel; reservation queue policy
- **Patterns:** Strategy (`FinePolicy` — flat/per-day/grace); Observer (`Reservation` notified when `Copy` returns); Factory (`Loan` per book type)
- **Boundary:** `FinePolicy`, `NotificationChannel`
- **Open/Closed:** add `CappedFinePolicy` → new class, `LoanService` untouched
- **⚠️ Trap:** Hard-coding fine rate inside `Loan.returnBook()` — extract to policy.

---

### 2. Parking Lot

- **Axes:** pricing (flat / hourly / dynamic / vehicle-tier); slot allocation (nearest / random / first-fit); vehicle hierarchy
- **Patterns:** Strategy (`PricingStrategy`, `SlotAllocationStrategy`); Factory (`VehicleFactory` by type); Singleton (`ParkingLot` controller)
- **Boundary:** `PricingStrategy`, `SlotAllocationStrategy`, `Vehicle` (abstract)
- **Open/Closed:** add `WeekendSurgePricing` → new class only
- **⚠️ Trap:** `if (vehicleType == BIKE) ... else if (CAR) ...` → use polymorphism on `Vehicle`.

---

### 3. Hotel

- **Axes:** pricing (seasonal / dynamic); cancellation policy (strict / moderate / flex); room amenities
- **Patterns:** Strategy (`PricingStrategy`, `CancellationPolicy`); Decorator (`Room` + amenity wrappers); Builder (`Reservation`)
- **Boundary:** `PricingStrategy`, `CancellationPolicy`
- **Open/Closed:** add `BlackFridayPricing` → register new strategy
- **⚠️ Trap:** Cancellation refund math scattered across services — centralize behind policy.

---

### 4. Ride-Sharing (Uber)

- **Axes:** matching algorithm (nearest / batched / surge-aware); pricing (base / surge / pool); vehicle category
- **Patterns:** Strategy (`MatchingStrategy`, `PricingStrategy`); Factory (`Vehicle` by category); Observer (driver location updates → dispatch)
- **Boundary:** `MatchingStrategy`, `PricingStrategy`
- **Open/Closed:** add `PoolMatching` → new class plugs into dispatcher
- **⚠️ Trap:** Coupling matcher to a single pricing model — keep orthogonal.

---

### 5. Movie Booking (BookMyShow)

- **Axes:** pricing tiers (silver/gold/platinum, weekday/weekend); seat layout per screen; Hold expiry handling
- **Patterns:** Strategy (`PricingStrategy`); Composite (`Screen` → rows → seats); State (`Hold`: HELD/CONFIRMED/EXPIRED — Pillar 3)
- **Boundary:** `PricingStrategy`, `SeatLayout`
- **Open/Closed:** new tier `Recliner` → extend pricing + layout, booking flow untouched
- **⚠️ Trap:** Hard-coded seat layout per theatre — load from config / Composite tree.

---

### 6. ATM

- **Axes:** transaction types (withdraw / deposit / transfer / balance); authentication method (PIN / biometric)
- **Patterns:** Command (`Transaction` as command — supports undo/log/queue); State (`Session`: IDLE→AUTH→TXN); Strategy (`AuthStrategy`)
- **Boundary:** `Transaction` (Command), `AuthStrategy`
- **Open/Closed:** add `MiniStatement` → new `Command` class, switch in menu
- **⚠️ Trap:** Giant `if (op==WITHDRAW) ...` in controller — that's the Command pattern shouting.

---

### 7. KV Store / Cache

- **Axes:** eviction policy (LRU / LFU / FIFO / TTL / ARC); persistence (in-mem / write-through / write-back); expiry
- **Patterns:** Strategy (`EvictionPolicy`, `ExpiryPolicy`); Decorator (`PersistentCache` wraps `InMemoryCache`); Template Method (`AbstractCache.get/put`)
- **Boundary:** `EvictionPolicy`, `Storage`
- **Open/Closed:** add `ARCPolicy` → new class implementing `EvictionPolicy`
- **⚠️ Trap:** Eviction state (recency list) leaking out of policy — keep all bookkeeping internal.

---

### 8. URL Shortener

- **Axes:** code generation (base62 / hash / custom alias / counter); storage backend
- **Patterns:** Strategy (`CodeGenerationStrategy`); Factory (pick strategy by config)
- **Boundary:** `CodeGenerator`
- **Open/Closed:** add `MnemonicCodeGenerator` → new class, register
- **⚠️ Trap:** Collision retry logic inside `ShortenerService` — push into generator contract.

---

### 9. Tic-Tac-Toe

- **Axes:** win condition (rows/cols/diagonals; variant boards); board size
- **Patterns:** Strategy (`WinCondition`); Composite (`Board` → `Cell`s); Observer (game-end → notify players)
- **Boundary:** `WinCondition`, `Board`
- **Open/Closed:** add `NxN` board or `4-in-a-row` rule → new strategy, no engine change
- **⚠️ Trap:** Hard-coded `3×3` constants littered through code — parameterize.

---

### 10. Online Learning

- **Axes:** quiz scoring (per-question / weighted / negative-marking); certificate template; payment provider
- **Patterns:** Strategy (`ScoringStrategy`, `PaymentProvider`); Builder (`Certificate` PDF); Observer (lesson-complete → progress update)
- **Boundary:** `ScoringStrategy`, `PaymentProvider`, `CertificateBuilder`
- **Open/Closed:** add `AdaptiveQuizScoring` → new strategy
- **⚠️ Trap:** Certificate PDF generation crammed into `Enrollment.complete()` — extract to builder.

---

### 11. Twitter

- **Axes:** fanout (push / pull / hybrid by follower-count); feed ranking (chronological / ML); notification
- **Patterns:** Strategy (`FanoutStrategy`, `RankingStrategy`); Observer (tweet → fanout subscribers); Decorator (rate-limited / cached repo)
- **Boundary:** `FanoutStrategy`, `RankingStrategy`
- **Open/Closed:** add `CelebrityHybridFanout` → new class plugs into pipeline
- **⚠️ Trap:** Single fanout strategy can't serve both normal users and celebrities — design for hybrid up front.

---

### 12. Food Delivery

- **Axes:** agent dispatch (nearest / batched / multi-pickup); pricing/surge; restaurant ranking
- **Patterns:** Strategy (`DispatchStrategy`, `PricingStrategy`); Observer (`OrderStatusEvent` subscribers — customer/restaurant/agent); Chain of Responsibility (order validation: open? in-range? payment?)
- **Boundary:** `DispatchStrategy`, `PricingStrategy`
- **Open/Closed:** add `BatchedDispatch` → new strategy
- **⚠️ Trap:** Notification logic inlined in state transitions — use Observer/EventBus.

---

### 13. E-commerce / Cart

- **Axes:** discount stacking (coupon / loyalty / BOGO); payment method; shipping calculator
- **Patterns:** Chain of Responsibility (`DiscountChain` — applied in order); Strategy (`PaymentMethod`, `ShippingCalculator`); Decorator (`Order` wrapped by gift-wrap / insurance)
- **Boundary:** `Discount`, `PaymentMethod`, `ShippingCalculator`
- **Open/Closed:** add `StudentDiscount` → new handler in chain
- **⚠️ Trap:** Discount ordering matters (percent vs flat) — chain order must be explicit.

---

### 14. Chess

- **Axes:** piece movement rules; move validation steps; variant rules (Chess960)
- **Patterns:** Strategy (per-piece `MoveStrategy`); Chain of Responsibility (`MoveValidator`: turn → bounds → piece-legal → king-safety); Command (`Move` — supports undo)
- **Boundary:** `Piece.moves()`, `MoveValidator`
- **Open/Closed:** add new piece variant → new `Piece` subclass with its own moves
- **⚠️ Trap:** Special moves (castling / en-passant / promotion) tempt `instanceof` — keep polymorphic via Piece capability methods.

---

### 15. Snake & Ladder

- **Axes:** dice (fair / loaded / 2-dice); board (snakes/ladders layout); win rule (exact-100 or overshoot bounces)
- **Patterns:** Strategy (`Dice`, `WinRule`); Composite (`Board` cells + snake/ladder mappings)
- **Boundary:** `Dice`, `WinRule`
- **Open/Closed:** add `WeightedDice` → new impl, game engine untouched
- **⚠️ Trap:** Random hidden in `Game` class — inject `Dice` for testability.

---

### 16. Elevator

- **Axes:** dispatch algorithm (SCAN / LOOK / nearest-car / destination-dispatch); door behavior
- **Patterns:** Strategy (`DispatchStrategy`); State (`Elevator`: IDLE/UP/DOWN/MAINTENANCE — Pillar 3); Command (`Request` queued)
- **Boundary:** `DispatchStrategy`, `ElevatorState`
- **Open/Closed:** swap `LOOK` for `DestinationDispatch` → set on `Building`, no controller change
- **⚠️ Trap:** Dispatch logic embedded in `Elevator.move()` — should live in strategy operating on fleet.

---

### 17. Vending Machine

- **Axes:** payment (coin/note/card/UPI); product categories; dispense mechanism
- **Patterns:** State (`Machine`: IDLE/COLLECTING/DISPENSING — Pillar 3); Strategy (`PaymentMethod`); Factory (item per slot)
- **Boundary:** `MachineState`, `PaymentMethod`
- **Open/Closed:** add UPI → new `PaymentMethod` impl
- **⚠️ Trap:** Nested `if` chains over `currentState` — State pattern eliminates them.

---

### 18. Splitwise

- **Axes:** split type (equal / percent / exact / shares); settlement algorithm (pairwise / minimum-transactions); currency conversion
- **Patterns:** Strategy (`SplitStrategy`, `SettlementAlgorithm`, `FxStrategy`); Command (`Expense` — supports edit/delete with reverse)
- **Boundary:** `SplitStrategy`, `SettlementAlgorithm`
- **Open/Closed:** add `AdjustmentSplit` → new strategy
- **⚠️ Trap:** Rounding remainder allocation (e.g., $10/3) — must live inside strategy, not service.

---

### 19. Notification System

- **Axes:** channel (email / SMS / push / Slack); template per locale; delivery (sync / async / batched)
- **Patterns:** Strategy (`Channel`); Builder (`Template` per channel); Observer (domain event → notification trigger); Chain of Responsibility (filter: opt-out → quiet-hours → rate-limit)
- **Boundary:** `Channel`, `Template`
- **Open/Closed:** add `WhatsAppChannel` → new class, register in factory
- **⚠️ Trap:** Channel quirks (email-subject vs push-title) force a leaky abstraction — model channel-specific payload separately.

---

### 20. Chat / Messaging

- **Axes:** delivery (online push / offline store-and-forward); encryption (none / E2E); message types (text / media / system)
- **Patterns:** Strategy (`DeliveryStrategy`, `EncryptionStrategy`); Observer (typing / read receipts); Factory (`Message` by type)
- **Boundary:** `DeliveryStrategy`, `EncryptionStrategy`
- **Open/Closed:** add E2E → new strategy, transport unchanged
- **⚠️ Trap:** Putting media-vs-text branching in `Message` class — subclass / Factory.

---

### 21. Dropbox

- **Axes:** storage backend (local / S3 / GCS); conflict resolution (LWW / conflict-copy / manual); compression
- **Patterns:** Strategy (`StorageBackend`, `ConflictResolver`); Decorator (`EncryptedStorage` wraps `S3Storage`); Adapter (third-party SDK → our interface)
- **Boundary:** `StorageBackend`, `ConflictResolver`
- **Open/Closed:** add Azure Blob → new adapter
- **⚠️ Trap:** Coupling sync logic to a single backend — go through abstraction.

---

### 22. Stock Exchange

- **Axes:** matching algorithm (FIFO / Pro-rata); order types (LIMIT / MARKET / IOC / STOP); fee schedule
- **Patterns:** Strategy (`MatchingAlgorithm`, `FeeSchedule`); Factory (`Order` by type); State (`Order`: NEW/OPEN/FILLED — Pillar 3); Command (audit log of orders)
- **Boundary:** `MatchingAlgorithm`, `Order` (sealed hierarchy)
- **Open/Closed:** add `Iceberg` order → new `Order` subclass + matcher extension
- **⚠️ Trap:** Matching engine assumes one algorithm globally — make it per-symbol-configurable.

---

### 23. Logger

- **Axes:** sink (console / file / network / Kafka); format (plain / JSON / XML); filter; level
- **Patterns:** Strategy (`Appender`, `Formatter`); Chain of Responsibility (`Filter`); Singleton (`LoggerFactory`); Composite (hierarchical loggers — `com.foo` parent of `com.foo.Bar`)
- **Boundary:** `Appender`, `Formatter`, `Filter`
- **Open/Closed:** add `KafkaAppender` → new class, configure in factory
- **⚠️ Trap:** Singleton + mutable config = test pain. Prefer DI; keep Singleton thin.

---

### 24. Rate Limiter

- **Axes:** algorithm (token-bucket / leaky-bucket / fixed-window / sliding-window); storage (local / Redis)
- **Patterns:** Strategy (`RateLimitAlgorithm`); Decorator (`DistributedRateLimiter` wraps local); Factory (per-rule)
- **Boundary:** `RateLimitAlgorithm`, `BucketStore`
- **Open/Closed:** add `SlidingLog` → new strategy
- **⚠️ Trap:** Algorithm state coupled to storage — keep algorithm pure, inject store.

---

### 25. Calendar

- **Axes:** recurrence (daily / weekly / monthly / custom RRULE); reminder type (email / push / SMS); timezone handling
- **Patterns:** Strategy (`RecurrenceRule`); Composite (recurring event = master + exceptions); Observer (reminder fires → notification)
- **Boundary:** `RecurrenceRule`, `ReminderChannel`
- **Open/Closed:** add `EveryWeekday` rule → new strategy
- **⚠️ Trap:** Expanding recurrences naively explodes memory — use iterator/lazy.

---

### 26. Spotify

- **Axes:** recommendation algorithm (collab-filter / content / hybrid); subscription tier features; bitrate
- **Patterns:** Strategy (`Recommender`, `SubscriptionTier`); Decorator (premium features wrap base player); Observer (`PlayEvent` → recommender update)
- **Boundary:** `Recommender`, `SubscriptionTier`
- **Open/Closed:** add `FamilyTier` → new tier impl
- **⚠️ Trap:** Tier checks scattered as `if (user.isPremium)` — push into polymorphic tier object.

---

### 27. Netflix

- **Axes:** ABR algorithm (bandwidth / buffer / hybrid); recommendation; DRM; CDN selection
- **Patterns:** Strategy (`ABRStrategy`, `Recommender`, `CDNSelector`); State (`WatchHistory`: STARTED/COMPLETED — Pillar 3); Adapter (per-DRM provider)
- **Boundary:** `ABRStrategy`, `CDNSelector`, `DRM`
- **Open/Closed:** add new CDN provider → new adapter
- **⚠️ Trap:** Coupling player to a single DRM SDK — go through adapter.

---

### 28. Airbnb

- **Axes:** pricing (static / dynamic / seasonal / smart); cancellation policy; booking mode (instant vs request)
- **Patterns:** Strategy (`PricingStrategy`, `CancellationPolicy`, `BookingMode`); Observer (booking events → host/guest notifications)
- **Boundary:** `PricingStrategy`, `CancellationPolicy`, `BookingMode`
- **Open/Closed:** add `SuperhostFlexCancellation` → new policy
- **⚠️ Trap:** Instant-book vs request-book paths share too much — model as Strategy on listing, not branching in service.

---

### 29. Banking

- **Axes:** interest calc (simple / compound / tiered); fraud detection rules; transfer type (intra / inter / SWIFT)
- **Patterns:** Strategy (`InterestPolicy`, `TransferStrategy`); Chain of Responsibility (`FraudCheck`: amount → velocity → geo → device); Command (`Transaction` with `execute`/`compensate` for saga)
- **Boundary:** `InterestPolicy`, `FraudCheck`, `TransferStrategy`
- **Open/Closed:** add new fraud rule → append handler to chain
- **⚠️ Trap:** Fraud rules order-sensitive — make chain config-driven, not hard-coded.

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────┐
│  PILLAR 6 — 6 STEPS (memorize)                       │
├──────────────────────────────────────────────────────┤
│  1. AXES           → what will vary?                 │
│  2. PATTERN        → which GoF absorbs it?           │
│  3. BOUNDARY       → stable interface vs swappable   │
│  4. OPEN/CLOSED    → extend without editing?         │
│  5. DIP            → depend on abstraction, inject   │
│  6. TRADE-OFF      → ≥2 variants likely? else YAGNI  │
└──────────────────────────────────────────────────────┘
```

## Universal probing pattern (for any new design)

```
Q1 (axes):       "What part of this is likely to change in 6 months?"
Q2 (pattern):    "Is this a swappable algorithm (Strategy)? An event
                 fanout (Observer)? A lifecycle (State)? A pipeline
                 (Chain)? A tree (Composite)?"
Q3 (boundary):   "What interface do callers depend on? What's behind it?"
Q4 (open/closed):"To add a new variant, what files change vs added?"
Q5 (DIP):        "Does the high-level module `new` the concrete class?
                 Or is it injected?"
Q6 (trade-off):  "Do I have ≥2 real variants? If not, inline it."
```

These 6 questions surface ~90% of extensibility decisions.

---

## Common traps (quick recap)

1. **Pattern-for-pattern's-sake** — applying GoF without an axis of change creates ceremony, not flexibility.
2. **Premature abstraction** — single-impl Strategy/Factory is just an interface tax. Wait for the second variant.
3. **`instanceof` smell** — branching on type means a missing polymorphic method or a missing subclass.
4. **Singleton hiding global mutable state** — kills testability. Prefer DI; if you must, keep Singleton immutable.
5. **God Factory** — one factory that knows every type. Split by family or use registry / Service Loader.
6. **Leaky abstraction** — channel-specific quirks (email subject vs push title) leaking through `Channel` interface. Model per-channel payload.
7. **Observer memory leaks** — forgetting to unregister listeners. Use weak refs or explicit `close()`.
8. **State explosion via `if` chains** — nested `if (status == ...)` everywhere is the State pattern asking to be born.
9. **DIP violated** — high-level service `new`s the concrete dependency. Inject via constructor; let DI / config wire it.
10. **Strategy state leakage** — one strategy instance mutating shared state breaks under concurrency. Make strategies stateless OR scoped per request.

