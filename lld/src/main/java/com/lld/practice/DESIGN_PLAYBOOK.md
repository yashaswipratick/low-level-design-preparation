# LLD Design Playbook — All 6 Pillars Consolidated

> **Purpose:** One revision document. Each domain = one block fusing Scope → Entities → Lifecycle → Ops → Edges → Extensibility into a **design-ready blueprint**.
>
> **How to use:**
> 1. Read top section (universal flow) — memorize once.
> 2. Pick 1–2 domains, walk top-to-bottom, code the class skeleton from the block.
> 3. Before any interview, skim the domain matching the problem; if no match, run the **Universal 6-step flow** below.

---

## The Universal 6-Step Flow (memorize)

```
┌──────────────────────────────────────────────────────────────┐
│ 1. SCOPE         WHO / WHAT (ops) / single vs multi / MVP    │
│ 2. ENTITIES      T1 nouns + T2 container + T3 transaction    │
│                  cardinality (1-1 / 1-N / N-N) + identity    │
│ 3. LIFECYCLE     states enum → transitions → terminal        │
│                  reversals + time-based + side-effects       │
│ 4. OPS / APIs    verbs → signatures → preconditions          │
│                  side-effects → idempotency → concurrency    │
│ 5. EDGES         input / resource / state / dep / race /     │
│                  partial-failure → recovery (saga / sweeper) │
│ 6. EXTENSIBILITY Strategy / Factory / Observer / Decorator   │
│                  where? (pricing / matching / allocation /   │
│                  notification / eviction / filter pipeline)  │
└──────────────────────────────────────────────────────────────┘
```

### Translation cheat-sheet (use while coding)

| Pillar concept | Java construct |
|---|---|
| State (Pillar 3) | `enum Status` + `Map<Status, Set<Status>>` allowed edges |
| 1-N (Pillar 2) | `List<X>` / `Set<X>` |
| N-N + metadata | Join entity (`Loan`, `Enrollment`, `Split`) |
| Container w/ capacity | atomic `UPDATE … WHERE status='AVAILABLE'` |
| Idempotency (Pillar 4) | dedup table keyed by `requestId` |
| Concurrency (Pillar 4/5) | per-key lock / CAS / row lock / sharded engine |
| Policy variation (Pillar 6) | Strategy interface + factory |
| Cross-cutting (audit, notify) | Observer / event bus |

### 5-minute design ritual (every problem)

1. **30s** — state actors + ops (Pillar 1).
2. **90s** — draw entities + cardinality (Pillar 2): T1 → T2 → T3.
3. **60s** — for the T3 transaction, draw state machine (Pillar 3).
4. **90s** — list 3–5 ops with signatures (Pillar 4).
5. **60s** — call out top 2 edge cases + recovery (Pillar 5).
6. **30s** — name 1–2 strategy / observer extension points (Pillar 6).

---

## DOMAIN BLOCKS (revision-ready)

Each block format:
```
SCOPE         actors / 4–5 core ops
ENTITIES      T1 nouns | T2 container | T3 transaction
              key cardinality (only the non-obvious one)
              identity (A=assigned, D=derived)
LIFECYCLE     states → transitions (only T3); time-based
OPS           top 3 ops with signature + idempotency key
EDGES         the ONE failure mode that always trips candidates
EXTENSION     where the Strategy/Observer lives
⚠ TRAP        the one thing that decides if you pass
```

---

### 1. Library

- **Q:** *"Design a Library Management System where users can borrow, return, and reserve books, with overdue fines."*
- **Scope:** User borrows physical Book Copies; ops = `search`, `issue`, `return`, `reserve`, `payFine`.
- **Entities:** `User`, `Book`, `Copy` (T2) | `Loan` (T3), `Reservation`, `Fine`.
  `Book↔Copy` = 1-N; `Loan↔Copy` = **1-1 active / N-1 historical**.
  `Copy = bookId+copyNum (D)`, `Loan = loanId (A)`.
- **Lifecycle (Loan):** `ACTIVE → RETURNED | OVERDUE`; `OVERDUE → RETURNED | LOST`. Time: scheduler flips `ACTIVE→OVERDUE` on `now > dueDate`.
- **Ops:**
  - `issueBook(userId, copyId) → Loan` — needs `requestId`; CAS on `Copy.status='AVAILABLE'`.
  - `returnBook(loanId)` — **idempotent** no-op if already RETURNED.
  - `reserveBook(userId, bookId)` — FIFO queue per copy.
- **Edges:** crash between `Copy=BORROWED` and `Loan` insert → wrap in DB txn OR orphan sweeper.
- **Extension:** `FinePolicy` (Strategy: flat / per-day / capped). `OverdueScheduler` (Observer notifies reservation queue on return).
- **⚠ Trap:** Reservation FIFO order under concurrent returns.

---

### 2. Parking Lot

- **Q:** *"Design a multi-floor Parking Lot supporting multiple vehicle types, ticketing, and hourly pricing."*
- **Scope:** Vehicles enter/exit; ops = `entry`, `pay`, `exit`, `findSlot`.
- **Entities:** `Vehicle`, `Lot`, `Floor`, `Slot` (T2) | `Ticket` (T3), `Payment`, `RateCard`.
  `Lot→Floor→Slot` composition; `Slot↔Vehicle` = 1-1 active.
  `Slot = floorId+slotNum (D)`, `Vehicle = licensePlate (D)`, `Ticket = ticketId (A)`.
- **Lifecycle (Ticket):** `ISSUED → PAID → EXITED`; `ISSUED → LOST → EXITED` (penalty).
- **Ops:**
  - `entry(plate, type) → Ticket | LotFull` — atomic slot reservation (last slot race).
  - `pay(ticketId, amount)` — idempotent per `paymentId`; compute fee = exit−entry × rate.
  - `exit(ticketId)` — guards on `Ticket.PAID`; release `Slot`.
- **Edges:** crash between `Payment` insert and `Ticket=PAID` → reconcile via gateway webhook. Lost ticket = own state path.
- **Extension:** `SlotAllocator` (Strategy: nearest / best-fit). `PricingStrategy` (Strategy: flat / tiered / dynamic). `Vehicle/Slot` type via Factory.
- **⚠ Trap:** Fee computed on payment (not entry); store `entryTime`, compute lazily.

---

### 3. Hotel

- **Q:** *"Design a Hotel Booking System where guests reserve rooms for date ranges, with cancellation policies."*
- **Scope:** Guests book Rooms for date ranges; ops = `search`, `create`, `confirm`, `cancel`, `checkIn/Out`.
- **Entities:** `Guest`, `Hotel`, `Floor`, `Room` (T2), `RoomType` | `Reservation` (T3), `Invoice`, `Payment`.
  `Room↔Reservation` = **1-N over time, 1-1 per date range**.
  `Room = hotelId+roomNum (D)`.
- **Lifecycle (Reservation):** `PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT`; `CONFIRMED → CANCELLED | NO_SHOW`.
- **Ops:**
  - `create(guest, roomType, dateRange)` — pre-lock at **room-type** level, not specific room.
  - `confirm(resId, paymentToken)` — capture payment; idempotent via `requestId`.
  - `cancel(resId)` — partial refund per policy (model `Refund` separately).
- **Edges:** overlap check + assignment **must be atomic** → `SELECT FOR UPDATE` on availability rows.
- **Extension:** `CancellationPolicy` (Strategy), `PricingStrategy` (seasonal/weekday/loyalty), `RoomAllocator` (Strategy).
- **⚠ Trap:** Overlapping date ranges = range query, NOT equality. Interval-tree.

---

### 4. Ride-Sharing (Uber)

- **Q:** *"Design a Ride-Sharing service (Uber/Lyft) that matches riders with nearby drivers and handles trip lifecycle + fare."*
- **Scope:** Rider↔Driver match → trip; ops = `request`, `accept`, `start`, `end`, `cancel`, `rate`.
- **Entities:** `Rider`, `Driver`, `Vehicle` (T2) | `RideRequest`, `Trip` (T3), `Payment`, `Rating`.
  `Driver↔Trip` = 1-1 active / 1-N historical. `RideRequest` and `Trip` are **two** entities.
- **Lifecycle (Trip):** `REQUESTED → MATCHED → DRIVER_ARRIVING → IN_PROGRESS → COMPLETED`; any pre-pickup → `CANCELLED`.
- **Ops:**
  - `requestRide(rider, pickup, drop) → RideRequest` — idempotency key on double-tap.
  - `acceptRide(driver, requestId) → Trip | AlreadyTaken` — **CAS on `RideRequest.status='OPEN'`** (first wins).
  - `endTrip(tripId)` — triggers `Payment` + `Rating`; `Payment.PENDING` if charge fails.
- **Edges:** N drivers see same request → CAS; driver-cancel ≠ rider-cancel (different fees).
- **Extension:** `DispatchStrategy` (nearest / surge-aware / pooled), `PricingStrategy` (surge), `MatchingEngine` (Strategy + Observer for ETA).
- **⚠ Trap:** Dispatch is N-driver fan-out; only one accept wins.

---

### 5. Movie Booking (BookMyShow)

- **Q:** *"Design a Movie Ticket Booking system (BookMyShow) with seat selection, holds, and concurrent booking safety."*
- **Scope:** User books seats for a Show; ops = `search`, `hold`, `confirm`, `cancel`.
- **Entities:** `User`, `Movie`, `Theatre`, `Screen`, `Seat` (T2), `Show` | `Hold` → `Booking` (T3), `Payment`.
  `Show = movieId+screenId+startTime (D)`. `Seat` key is **per-show**, not global.
  `Seat+Show ↔ Booking` = 1-1 per show.
- **Lifecycle:** `Hold: HELD → CONFIRMED | EXPIRED`; `Booking: CONFIRMED → USED | CANCELLED`. Hold TTL ~5-10 min.
- **Ops:**
  - `holdSeats(user, show, seats[])` — **all-or-nothing** across N seats (single txn).
  - `confirmBooking(holdId, paymentToken)` — idempotent on `holdId` (one-shot).
  - `releaseExpiredHolds()` — scheduler.
- **Edges:** payment in-flight while Hold expiring → extend TTL while processing.
- **Extension:** `PricingStrategy` (seat-category, surge), `SeatAllocator` (best-contiguous), `Payment` providers (Strategy/Factory).
- **⚠ Trap:** Partial seat lock = bug. Lock all or fail.

---

### 6. ATM

- **Q:** *"Design an ATM system supporting authentication, withdrawal, deposit, and balance inquiry."*
- **Scope:** Customer authenticates, withdraws/deposits; ops = `auth`, `withdraw`, `deposit`, `balance`, `eject`.
- **Entities:** `Customer`, `Account`, `Card`, `ATM`, `CashInventory` (T2) | `Session`, `Transaction` (T3).
  Session has many Transactions.
- **Lifecycle (Session):** `IDLE → AUTHENTICATING → AUTHENTICATED ↔ TRANSACTING → CLOSED`; `AUTHENTICATING → BLOCKED` (3 wrong PINs).
- **Ops:**
  - `withdraw(sessionId, accountId, amount)` — per-account row-lock; `txnId` for retry-safety.
  - Pre: session AUTHENTICATED, balance ≥ amount, ATM cash ≥ amount, daily limit.
- **Edges:** **classic** — money dispensed, balance not debited → reconcile via daily report + compensating debit. 2PC or saga.
- **Extension:** `AuthStrategy` (PIN/biometric), `CashDispenseStrategy` (denomination selection), `NotificationObserver` (SMS).
- **⚠ Trap:** Network partition mid-withdraw. Always have reconciliation.

---

### 7. KV Store / Cache

- **Q:** *"Design an in-memory Key-Value Cache (like Redis/Memcached) with TTL and pluggable eviction policies."*
- **Scope:** `get`, `put`, `delete`, `getOrCompute`; with TTL + eviction.
- **Entities:** `Cache` | `Entry` (T2, key→value). `Entry = key (D)`.
- **Lifecycle (Entry):** `LIVE → EXPIRED | EVICTED`. TTL sweeper OR lazy on-read.
- **Ops:** `put` idempotent; sharded locks or `ConcurrentHashMap`; LRU reorder under contention needs care.
- **Edges:** **cache stampede** on hot key expiry → `getOrCompute` with single-flight lock.
- **Extension:** `EvictionPolicy` (Strategy: LRU / LFU / FIFO / TTL), `ExpiryPolicy` (Strategy), `Loader` (callback). Decorator for write-through / write-behind.
- **⚠ Trap:** Lazy vs active expiry — lazy serves stale until next read.

---

### 8. URL Shortener

- **Q:** *"Design a URL Shortener (TinyURL/bit.ly) supporting custom aliases, expiry, and click analytics."*
- **Scope:** `shorten`, `expand`, `disable`, `stats`.
- **Entities:** `Owner`, `ShortLink` (T2), `ClickEvent`. `ShortLink = shortCode (A or D for custom)`.
- **Lifecycle (ShortLink):** `ACTIVE → EXPIRED | DISABLED`; `DISABLED → ACTIVE` (re-enable).
- **Ops:** `shorten` idempotent-by-content (same longUrl → same code, if policy); alias uniqueness via DB unique index + retry.
- **Edges:** generated code collision → bounded retry; open-redirect attack → validate scheme + blocklist.
- **Extension:** `CodeGenerator` (Strategy: base62 / hash / counter), `ExpiryPolicy`, `ClickAnalytics` (Observer).
- **⚠ Trap:** Same longUrl twice → return same OR new? Clarify upfront.

---

### 9. Tic-Tac-Toe

- **Q:** *"Design a Tic-Tac-Toe game for 2 players with win/draw detection (extensible to N×N)."*
- **Scope:** 2 players, 3×3 grid; ops = `create`, `join`, `move`, `forfeit`.
- **Entities:** `Game`, `Player`, `Board`, `Cell` (T2) | `Move` (T3), `GameResult`.
  `Move = gameId+seqNum (D)` → replay-safe.
- **Lifecycle (Game):** `WAITING → IN_PROGRESS → WON | DRAW | ABANDONED`.
- **Ops:** `move(gameId, player, row, col)` — per-game lock; preconds = turn + empty + IN_PROGRESS.
- **Extension:** `WinStrategy` (lines / diagonals / N-in-a-row for generalized boards). Board size pluggable.
- **⚠ Trap:** Forfeit / disconnect → timeout sweeper to auto-end.

---

### 10. Online Learning

- **Q:** *"Design an Online Learning platform (Coursera/Udemy) with enrollments, lesson progress, quizzes, and certificates."*
- **Scope:** Students enroll, learn, get certificate; ops = `search`, `enroll`, `startLesson`, `markComplete`, `quiz`.
- **Entities:** `Student`, `Course`, `Instructor`, `Lesson` (T2) | `Enrollment`, `Progress` (T3), `Certificate`.
  `Student↔Course` = N-N via `Enrollment`. `Progress = enrollmentId+lessonId (D)`.
- **Lifecycle (Enrollment):** `ENROLLED → IN_PROGRESS → COMPLETED`; any → `EXPIRED | REFUNDED`.
- **Ops:** `markLessonComplete` idempotent (set, not increment); on COMPLETED → issue `Certificate`.
- **Extension:** `ProgressionRule` (Strategy: linear / free), `RefundPolicy`, `CertificateGenerator`.
- **⚠ Trap:** Refund window vs progress cutoff (e.g., < 20% watched).

---

### 11. Twitter

- **Q:** *"Design Twitter/X — post tweets, follow users, like/retweet, and generate a home timeline."*
- **Scope:** Post, follow, like, retweet, timeline.
- **Entities:** `User`, `Tweet` (T3), `Follow`, `Like`, `Retweet`, `Feed` (T2).
  `User↔Follow↔User` = N-N self-ref. `Like = userId+tweetId (D)` (set semantics).
- **Lifecycle (Tweet):** `DRAFT → PUBLISHED → DELETED`; `PUBLISHED ↔ HIDDEN`.
- **Ops:** `postTweet` (rate-limit), `like`/`follow` idempotent. Counters → atomic increment / sharded.
- **Edges:** **celebrity fanout** — hybrid push (normal) / pull (celeb).
- **Extension:** `TimelineStrategy` (push / pull / hybrid), `RankingStrategy` (chronological / scored), `NotificationObserver`.
- **⚠ Trap:** Fanout to millions; do NOT push synchronously.

---

### 12. Food Delivery

- **Q:** *"Design a Food Delivery system (Swiggy/Zomato/DoorDash) — customer orders, restaurant prep, agent delivery."*
- **Scope:** Customer → Cart → Order → Restaurant → Agent.
- **Entities:** `Customer`, `Restaurant`, `MenuItem`, `Cart` (T2), `Address` | `Order` (T3), `DeliveryAgent`, `Payment`, `OrderStatusEvent`.
  `Cart = customerId (D, 1-1 active)`.
- **Lifecycle (Order):** linear `PLACED → ACCEPTED → PREPARING → READY → PICKED_UP → DELIVERED`; `CANCELLED` only pre-PREPARING.
- **Ops:** `placeOrder` idempotent on `cartId+ts`; `assignAgent` → CAS on `Order.agentId IS NULL`; emit `OrderStatusEvent` each transition.
- **Edges:** cancel post-dispatch → partial refund + reroute.
- **Extension:** `AgentAssignmentStrategy` (nearest / batched), `PricingStrategy` (surge / coupons via Decorator), `NotificationObserver`.
- **⚠ Trap:** Inventory + agent races; CAS, not read-then-write.

---

### 13. E-commerce / Cart

- **Q:** *"Design an E-commerce checkout flow (Amazon) — cart, inventory, payment, shipment, returns."*
- **Scope:** Browse → Cart → Order → Pay → Ship → Deliver → Return.
- **Entities:** `User`, `Product`, `Cart` (T2), `Warehouse`, `Inventory` | `Order` (T3), `Payment`, `Shipment`, `Discount`.
  `Inventory = warehouseId+productId (D)`. `Cart↔Product` = N-N via `CartItem`.
- **Lifecycle (Order):** `CREATED → PAID → PACKED → SHIPPED → DELIVERED`; `RETURNED` post-deliver; `CANCELLED` pre-ship.
- **Ops:** `checkout` idempotent on `cartId`; inventory decrement via atomic `UPDATE … WHERE qty >= n`.
- **Edges:** abandoned cart with reserved inventory → release after timeout (cron).
- **Extension:** `PricingStrategy` (taxes / shipping / discounts — Decorator chain), `ShipmentStrategy` (split shipments), `PaymentGateway` (Factory).
- **⚠ Trap:** Reserve-vs-commit window; coupon double-use via unique index.

---

### 14. Chess

- **Q:** *"Design a Chess game with full rule validation, clocks, check/checkmate detection, and draw conditions."*
- **Scope:** 2 players, 8×8, full rules; ops = `move`, `resign`, `offerDraw`, `acceptDraw`.
- **Entities:** `Game`, `Player`, `Board`, `Cell`, `Piece` (T2) | `Move` (T3), `Clock`, `GameResult`.
  `Piece↔Cell` = 1-1 current; `Game↔Move` = 1-N ordered.
- **Lifecycle (Game):** `WAITING → IN_PROGRESS ↔ CHECK → CHECKMATE | STALEMATE | DRAW_AGREED | RESIGNED | TIMEOUT`.
- **Ops:** `makeMove` — per-game lock; legality + king-safety check; server-authoritative clock.
- **Extension:** `MoveValidator` per `PieceType` (Strategy + polymorphism), `DrawDetector` (three-fold / 50-move), `GameMode` (blitz / rapid).
- **⚠ Trap:** Three-fold repetition and 50-move rule — server tracks, never trust client.

---

### 15. Snake & Ladder

- **Q:** *"Design Snake & Ladder for N players on a 100-cell board with configurable snakes/ladders."*
- **Scope:** N players, 1-100 board; ops = `create`, `join`, `roll`.
- **Entities:** `Game`, `Player`, `Board`, `Cell`, `Snake`, `Ladder`, `Dice` | `MoveResult` (T3), `GameResult`.
- **Lifecycle (Game):** `WAITING → IN_PROGRESS → FINISHED`.
- **Ops:** `rollDice(game, player)` — per-game lock; apply snake/ladder; check win at 100.
- **Extension:** Board config (Snakes/Ladders mappings injectable), `WinCondition` (Strategy).

---

### 16. Elevator

- **Q:** *"Design an Elevator system for a multi-floor building with multiple elevators and a dispatch algorithm."*
- **Scope:** Pickup + destination requests; ops = `requestPickup`, `requestDestination`.
- **Entities:** `Building`, `Elevator` (T2), `Floor` | `Request` (T3), `ElevatorState`.
- **Lifecycle (Elevator):** `IDLE → MOVING_UP/DOWN → STOPPED → IDLE`; `MAINTENANCE | OUT_OF_SERVICE`.
- **Ops:** dispatcher single-threaded per building; dedup repeated button presses; state derived per elevator.
- **Extension:** `DispatchStrategy` (SCAN / LOOK / nearest), `Direction` enum, `ElevatorSelector` (per-floor + capacity-aware).
- **⚠ Trap:** Starvation (top floor never served) → fairness.

---

### 17. Vending Machine

- **Q:** *"Design a Vending Machine that accepts coins, dispenses items, and returns change."*
- **Scope:** Insert money → select item → dispense / refund.
- **Entities:** `Machine`, `Slot` (T2), `Item`, `CashInventory` | `Transaction` (T3), `Refund`.
- **Lifecycle (Transaction):** `IDLE → COLLECTING_MONEY → SELECTING_ITEM → DISPENSING → DISPENSED | REFUNDED | CANCELLED`.
- **Ops:** single-user; state-machine prevents double-dispense.
- **Extension:** `PaymentMethod` (coin / card — Strategy), `ChangeStrategy` (denominations).
- **⚠ Trap:** No exact change → reject sale.

---

### 18. Splitwise

- **Q:** *"Design Splitwise — track shared expenses in groups, compute balances, and simplify settlements."*
- **Scope:** Groups, expenses, splits, settle-up; ops = `addExpense`, `editExpense`, `deleteExpense`, `settleUp`.
- **Entities:** `User`, `Group` (T2 logical), `Expense` (T3), `Split`, `Balance` (per-pair), `Settlement`.
  `Split = expenseId+userId (D)`; `Balance = fromUserId+toUserId (D)`.
- **Lifecycle (Expense):** `ACTIVE → EDITED* → DELETED`. `Balance: OPEN ↔ SETTLED`.
- **Ops:** `addExpense` idempotent via `requestId`; recompute pairwise balances; edit/delete must **reverse** prior splits in single txn.
- **Extension:** `SplitStrategy` (equal / exact / percent / shares), `SettlementOptimizer` (min-transactions algorithm), `CurrencyConverter`.
- **⚠ Trap:** Balance recompute under concurrent expense add → per-group lock OR event-sourced ledger as source of truth.

---

### 19. Notification System

- **Q:** *"Design a Notification System supporting email/SMS/push with templates, subscriptions, and retries."*
- **Scope:** Multi-channel (email/SMS/push) with retries + templates.
- **Entities:** `User`, `Subscription`, `Template`, `Channel` (T2) | `Notification` (T3), `DeliveryAttempt`.
- **Lifecycle (Notification):** `QUEUED → SENDING → SENT | FAILED → RETRY_SCHEDULED → … → DEAD_LETTER`.
- **Ops:** `send` dedup by `notificationId+userId+channel`; pass idempotency key to downstream (Twilio/SES).
- **Extension:** `Channel` (Strategy: Email / SMS / Push — polymorphism), `RetryPolicy` (exp backoff), `Template` (i18n), `Filter` (opt-out / quiet hours — chain).
- **⚠ Trap:** Retry storm → exponential backoff + DLQ.

---

### 20. Chat / Messaging

- **Q:** *"Design a Chat/Messaging app (WhatsApp) — 1-1 and group conversations with delivery and read receipts."*
- **Scope:** 1-1 + group; messages + attachments + receipts.
- **Entities:** `User`, `Conversation` (T2), `Participant` | `Message` (T3), `ReadReceipt`, `Attachment`.
  `Conversation↔User` = N-N via `Participant`. `ReadReceipt = messageId+userId (D)`.
- **Lifecycle (Message):** `SENDING → SENT → DELIVERED → READ` (per recipient); `→ DELETED` (window); `SENDING → FAILED`.
- **Ops:** `sendMessage` dedup by client `clientMsgId`; ordering = server sequence number (not client clock).
- **Extension:** `MessageType` (text/media/system), `DeliveryStrategy` (push / poll), `EncryptionStrategy`.
- **⚠ Trap:** Group `DELIVERED` only when ALL recipients received.

---

### 21. Dropbox

- **Q:** *"Design a File Storage + Sync service (Dropbox/Google Drive) with versioning, sharing, and chunked upload."*
- **Scope:** Upload / download / share / sync.
- **Entities:** `User`, `File`, `Folder` (tree, T2), `Quota` | `Version`, `ShareLink`, `Permission`, `SyncEvent`.
  `File↔Version` = 1-N. `Folder↔Folder` = 1-N self-ref.
- **Lifecycle (File):** `ACTIVE → TRASHED → PERMANENTLY_DELETED`; `TRASHED → ACTIVE`. Version: prev `CURRENT → OLD`.
- **Ops:** chunked upload with `uploadSessionId`; commit idempotent; dedup by content hash.
- **Edges:** simultaneous edits → last-write-wins OR conflict copy.
- **Extension:** `StorageBackend` (Strategy: S3 / local), `ConflictResolver` (Strategy), `SyncStrategy` (full / delta).
- **⚠ Trap:** Resumable chunks for large files; orphan chunks GC.

---

### 22. Stock Exchange

- **Q:** *"Design a Stock Exchange / Order Matching Engine supporting limit/market orders with price-time priority."*
- **Scope:** Place + match buy/sell orders.
- **Entities:** `User`, `Stock`, `OrderBook` (T2, 1 per ticker) | `Order` (T3), `Trade`, `Portfolio`, `Position`.
  `Position = userId+ticker (D)`.
- **Lifecycle (Order):** `NEW → OPEN → PARTIALLY_FILLED → FILLED`; or `CANCELLED | REJECTED | EXPIRED`.
- **Ops:** `placeOrder` idempotent on `clientOrderId`; **matching engine = single-threaded per symbol** (sharded by ticker) for ordering.
- **Extension:** `OrderType` (LIMIT / MARKET / IOC / GTC — Strategy), `MatchingAlgorithm` (price-time / pro-rata), `RiskCheck` (chain).
- **⚠ Trap:** **Price-time priority** — equal price, earlier wins. Sequence numbers mandatory.

---

### 23. Logger

- **Q:** *"Design a Logging framework (log4j-style) with hierarchical loggers, levels, filters, and multiple sinks."*
- **Scope:** Multi-level, multi-sink logging.
- **Entities:** `Logger` (hierarchical), `Appender`, `Formatter`, `Filter` | `LogEvent` (T3).
- **Lifecycle:** stateless event → pipeline (filter → format → append).
- **Ops:** async queue between caller and appender; batch flush; thread-safe sinks.
- **Extension:** `Appender` (Console / File / Network — Strategy), `Formatter` (plain / JSON), `Filter` (chain), Logger hierarchy (composite).
- **⚠ Trap:** Blocking network sink → async appender + bounded queue + drop policy.

---

### 24. Rate Limiter

- **Q:** *"Design a Rate Limiter supporting per-client quotas with pluggable algorithms (token bucket, sliding window)."*
- **Scope:** `allow(client, api) → boolean`.
- **Entities:** `Client`, `Rule`, `Bucket` (T2, per client+rule), `Window`, `Quota`.
  `Bucket = clientId+ruleId (D)`.
- **Lifecycle (Bucket):** `AVAILABLE ↔ THROTTLED`; refill on tick.
- **Ops:** atomic decrement (`AtomicLong` local / Redis `DECR` distributed / Lua for atomicity).
- **Edges:** Redis down → **fail-open vs fail-closed** policy choice (document!).
- **Extension:** `Algorithm` (Strategy: token-bucket / leaky-bucket / fixed-window / sliding-window), `KeyExtractor` (per IP / userId / apiKey).
- **⚠ Trap:** Clock skew across nodes → use server-side timestamps.

---

### 25. Calendar

- **Q:** *"Design a Calendar app (Google Calendar) supporting events, invitees, RSVPs, recurrence, and reminders."*
- **Scope:** Events + invitees + recurrence + reminders.
- **Entities:** `User`, `Calendar` (T2) | `Event` (T3), `Invitee`, `RSVP`, `Recurrence`, `Reminder`.
  `Invitee = eventId+userId (D)`.
- **Lifecycle (Event):** `SCHEDULED → IN_PROGRESS → COMPLETED`; user → `CANCELLED`.
- **Ops:** `createEvent` idempotent on `requestId`; `rsvp` idempotent (overwrite); recurrence expansion = lazy on read.
- **Extension:** `RecurrenceRule` (RRULE parser), `ConflictDetector` (free-busy), `ReminderChannel` (Observer per channel).
- **⚠ Trap:** **Timezones** — store UTC, render in user TZ; DST breaks naive math.

---

### 26. Spotify

- **Q:** *"Design a Music Streaming service (Spotify) with playlists, playback, subscriptions, and recommendations."*
- **Scope:** Stream songs + playlists + subscription.
- **Entities:** `User`, `Artist`, `Album`, `Song`, `Playlist` (T2) | `PlayEvent` (T3), `Subscription`, `Recommendation`.
  `Playlist↔Song` = N-N ordered.
- **Lifecycle (Subscription):** `TRIAL → ACTIVE ↔ PAST_DUE → EXPIRED | CANCELLED`.
- **Ops:** `play` checks subscription tier + region + concurrent stream limit.
- **Extension:** `RecommendationStrategy`, `ShuffleStrategy`, `BitrateStrategy` (ABR), `PaymentProvider` (Factory).

---

### 27. Netflix

- **Q:** *"Design a Video Streaming service (Netflix) with multi-profile, watch history, resume, and stream-limit per plan."*
- **Scope:** Multi-profile streaming + resume.
- **Entities:** `User`, `Profile`, `Video`, `Show`, `Season`, `Episode` | `WatchHistory` (T3, upsert by profile+video), `Subscription`.
- **Lifecycle (WatchHistory):** `STARTED → IN_PROGRESS → COMPLETED | ABANDONED`. `COMPLETED` at ≥90% watched.
- **Ops:** `updateProgress` idempotent (set position); enforce concurrent-stream limit per plan tier.
- **Extension:** `RecommendationEngine`, `ABRStrategy` (bitrate ladder), `DRMProvider`.
- **⚠ Trap:** DRM key rotation mid-stream must be smooth.

---

### 28. Airbnb

- **Q:** *"Design Airbnb — hosts list properties, guests book for date ranges with dynamic pricing and reviews."*
- **Scope:** Host lists + Guest books for date range.
- **Entities:** `Host`, `Guest`, `Listing` (T2) | `Booking` (T3), `Pricing`, `Availability`, `Payment`, `Review`.
  `Pricing/Availability = listingId+date (D)`. `Listing↔Booking` = 1-N non-overlapping per date.
- **Lifecycle (Booking):** `REQUESTED → CONFIRMED | DECLINED → CHECKED_IN → CHECKED_OUT`; `CONFIRMED → CANCELLED`.
- **Ops:** `requestBooking` idempotent on `requestId`; date-range availability via `SELECT FOR UPDATE` on overlapping rows; first-accept wins.
- **Extension:** `PricingStrategy` (dynamic / weekend), `CancellationPolicy` (flexible/moderate/strict), `SearchRanker`.
- **⚠ Trap:** Overlapping date ranges = range overlap query; instant-book vs request-to-book are different paths.

---

### 29. Banking

- **Q:** *"Design a Banking system supporting accounts, deposits, withdrawals, and cross-account transfers with idempotency."*
- **Scope:** Accounts, txns, transfers; ops = `deposit`, `withdraw`, `transfer`, `getBalance`.
- **Entities:** `Customer`, `Account`, `Card`, `Loan`, `Beneficiary` | `Transaction` (T3), `Statement`.
- **Lifecycle (Transaction):** `INITIATED → PENDING → POSTED`; `PENDING → FAILED`; `POSTED → REVERSED`.
- **Ops:** **every money op needs `txnId` idempotency** at every layer; transfer = atomic two-leg in single DB txn OR saga.
- **Edges:** debit succeeded, credit failed → compensating credit-back.
- **Extension:** `AccountType` (Savings/Checking/Loan — polymorphism), `FraudCheck` (chain), `InterestStrategy`, `Currency` + FX (Strategy).
- **⚠ Trap:** **Lock ordering** on cross-account transfer (lower accountId first) → deadlock avoidance.

---

## Pre-Interview Revision Checklist (5 minutes)

For the problem you're about to face:

- [ ] Scope: actors + 4–5 ops named?
- [ ] Entities: T1 / T2 / T3 split clear? Identified the **container** (Pillar 2 critical)?
- [ ] Cardinality: any 1-1 active vs 1-N historical? Any N-N needing a join entity?
- [ ] Lifecycle: state enum + transition diagram for the T3 transaction?
- [ ] Ops: idempotency key for every mutating op? Concurrency control named?
- [ ] Edges: top 2 partial-failure scenarios + recovery (saga / sweeper / dedup)?
- [ ] Extensibility: at least one Strategy + one Observer named?

If all 7 checks pass → you have a complete design. Code the skeleton.

---

## Quick Reference Card (memorize)

```
┌──────────────────────────────────────────────────────────┐
│  6-PILLAR DESIGN FLOW                                    │
├──────────────────────────────────────────────────────────┤
│  1. SCOPE         → actors / ops / single vs multi       │
│  2. ENTITIES      → T1 / T2 container / T3 transaction   │
│  3. LIFECYCLE     → enum + transitions + time-based      │
│  4. OPS           → sig + precond + idempotency + lock   │
│  5. EDGES         → input/res/state/dep/race/partial     │
│  6. EXTENSIBILITY → Strategy / Observer / Factory        │
├──────────────────────────────────────────────────────────┤
│  CODING ORDER:                                           │
│   enums → entities → repos → services → strategies       │
│   → state machine → ops with guards → tests              │
└──────────────────────────────────────────────────────────┘
```
