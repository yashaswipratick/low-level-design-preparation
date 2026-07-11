# Pillar 4: OPERATIONS & APIs

> **Goal:** For any LLD problem, enumerate the system's operations (the verbs) — inputs, outputs, errors, side effects, idempotency, concurrency — in under 60 seconds — using one repeatable 6-step pattern.

---

## The ONE pattern (memorize this, ignore everything else)

For every **operation** the system exposes (service method / API endpoint), walk these **6 steps in order**:

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 1  ENUMERATE OPS        List every verb the user / system │
│                              can invoke. (CRUD + domain verbs) │
│                                                                 │
│ Step 2  SIGNATURE            inputs → outputs                  │
│                              (params, return type, exceptions) │
│                                                                 │
│ Step 3  PRECONDITIONS        What must be true before the op?  │
│                              (auth, state, quota, ownership)   │
│                                                                 │
│ Step 4  SIDE EFFECTS         What changes? (entity state,      │
│                              counters, events emitted, money)  │
│                                                                 │
│ Step 5  IDEMPOTENCY / RETRY  Safe to call twice?               │
│                              (idempotency key? at-most-once?)  │
│                                                                 │
│ Step 6  CONCURRENCY          What can race?                    │
│                              (locks, atomicity, isolation)     │
└─────────────────────────────────────────────────────────────────┘
```

### The 4 operation flavors (vocabulary)

| Flavor | Examples | Notes |
|---|---|---|
| **Command** | `book()`, `cancel()`, `pay()` | Mutates state — needs concurrency control |
| **Query** | `search()`, `getDetails()`, `listX()` | Read-only — focus on indexing / pagination |
| **Lifecycle** | `create()`, `confirm()`, `close()` | Drives state machine (Pillar 3) |
| **Admin / Maintenance** | `restock()`, `rotate()`, `recompute()` | Often async / scheduled |

### Operation → Java representation

| Concern | Java choice |
|---|---|
| Op signature | service method `Result op(Input)` |
| Validation | precondition checks → throw `Invalid*Exception` |
| State transition | call entity method (Pillar 3) |
| Side effects | event publish, persistence, notification |
| Idempotency | dedup table keyed by `requestId` |
| Concurrency | `synchronized`, `ReentrantLock`, optimistic version, DB row lock |

> **Consistency rule:** Every Tier-3 transaction in Pillar 2 has at least 3 ops: **create**, **state-transition** (one per Pillar 3 edge user can trigger), **query**. Every operation that mutates a counter / shared resource needs a Step 6 answer.

---

## How to use the per-domain blocks below

```
DOMAIN
  Step 1 — Ops:           op1, op2, op3 …
  Step 2 — Signatures:    op(inputs) → output | throws
  Step 3 — Preconditions: auth / state / quota
  Step 4 — Side-effects:  state change, events, money
  Step 5 — Idempotency:   key + window OR "not idempotent"
  Step 6 — Concurrency:   what's locked, what's atomic
  ⚠️ Trap: …
```

---

## DOMAIN BLOCKS

### 1. Library

- **Ops:** `searchBook`, `issueBook`, `returnBook`, `reserveBook`, `payFine`, `addCopy`, `listOverdueLoans`
- **Signatures:**
  - `issueBook(userId, copyId) → Loan | NotAvailable | LimitExceeded`
  - `returnBook(loanId) → Loan(RETURNED) | Fine?`
  - `reserveBook(userId, bookId) → Reservation | AlreadyReserved`
- **Preconditions:** user not blocked; loan limit (e.g., ≤ 5 active); copy currently `AVAILABLE`
- **Side-effects:** Copy → BORROWED; create `Loan`; if late on return → create `Fine`; notify next reservation
- **Idempotency:** `returnBook` is idempotent (no-op if already RETURNED). `issueBook` is NOT — need `requestId` to dedup
- **Concurrency:** Two users grabbing last copy → row-lock on `Copy.status` or atomic `UPDATE … WHERE status='AVAILABLE'`
- **⚠️ Trap:** Reservation queue ordering must be FIFO under concurrent returns.

---

### 2. Parking Lot

- **Ops:** `entryVehicle`, `payTicket`, `exitVehicle`, `findSlot`, `markSlotOOS`
- **Signatures:**
  - `entryVehicle(licensePlate, vehicleType) → Ticket | LotFull`
  - `payTicket(ticketId, amount) → Payment | InsufficientAmount`
  - `exitVehicle(ticketId) → Ack | NotPaid | InvalidTicket`
- **Preconditions:** for entry: slot of matching type available; for exit: ticket `PAID`
- **Side-effects:** assign `Slot`; compute fee on pay; release `Slot` on exit
- **Idempotency:** `payTicket` should be idempotent per `paymentId`
- **Concurrency:** Slot allocation must be atomic (two cars at gate simultaneously)
- **⚠️ Trap:** Time-based fee calc on read — store entryTime, compute on payment.

---

### 3. Hotel

- **Ops:** `searchRooms`, `createReservation`, `confirmReservation`, `cancelReservation`, `checkIn`, `checkOut`
- **Signatures:**
  - `createReservation(guestId, roomType, dateRange) → Reservation(PENDING) | NoAvailability`
  - `confirmReservation(resId, paymentToken) → Reservation(CONFIRMED)`
  - `cancelReservation(resId) → Refund(amount)`
- **Preconditions:** date range available for room type; payment valid; cancellation before cutoff
- **Side-effects:** lock room for range; capture payment; on cancel → release + partial refund per policy
- **Idempotency:** create with `requestId`; cancel idempotent
- **Concurrency:** **Overlap check + assignment must be atomic** — use SELECT FOR UPDATE on availability rows
- **⚠️ Trap:** Don't lock a specific room at creation; lock at check-in. Pre-lock at room-type level.

---

### 4. Ride-Sharing (Uber)

- **Ops:** `requestRide`, `acceptRide`, `cancelRide`, `startTrip`, `endTrip`, `rateTrip`
- **Signatures:**
  - `requestRide(riderId, pickup, drop) → RideRequest`
  - `acceptRide(driverId, requestId) → Trip | AlreadyTaken`
  - `endTrip(tripId, finalLoc) → Trip(COMPLETED) + Payment`
- **Preconditions:** rider has payment method; driver `AVAILABLE` and nearby
- **Side-effects:** mark driver busy; on end → charge fare, free driver, trigger ratings
- **Idempotency:** `acceptRide` is **CAS-style** — first wins, others get `AlreadyTaken`
- **Concurrency:** Matching engine: optimistic accept on `RideRequest.status='OPEN'`
- **⚠️ Trap:** Dispatch is N-driver fan-out → only one wins. Use distributed lock or DB CAS.

---

### 5. Movie Booking (BookMyShow)

- **Ops:** `searchShows`, `holdSeats`, `confirmBooking`, `cancelBooking`, `releaseExpiredHolds`
- **Signatures:**
  - `holdSeats(userId, showId, seatIds[]) → Hold(holdId, expiresAt) | SeatsTaken`
  - `confirmBooking(holdId, paymentToken) → Booking | HoldExpired`
- **Preconditions:** all seats currently `AVAILABLE` for show; user under booking limit
- **Side-effects:** mark seats `HELD` then `BOOKED`; on expire/cancel → release
- **Idempotency:** `confirmBooking` keyed by `holdId` (one-shot)
- **Concurrency:** **Multi-seat hold must be atomic** — all-or-nothing across N seat rows (transaction)
- **⚠️ Trap:** Group of 4 seats: partial hold = bug. Either lock all or fail.

---

### 6. ATM

- **Ops:** `authenticate`, `selectAccount`, `withdraw`, `deposit`, `balanceInquiry`, `eject`
- **Signatures:**
  - `withdraw(sessionId, accountId, amount) → Transaction | InsufficientFunds | InsufficientCash | LimitExceeded`
- **Preconditions:** session `AUTHENTICATED`; account balance ≥ amount; ATM has cash; under daily limit
- **Side-effects:** debit account, decrement `CashInventory`, log txn, print receipt
- **Idempotency:** withdraw NOT naturally idempotent — use `txnId` for retry safety
- **Concurrency:** Per-account row lock on balance update
- **⚠️ Trap:** Network partition mid-withdraw → money out but balance not debited. Two-phase commit or compensating txn.

---

### 7. KV Store / Cache

- **Ops:** `get(k)`, `put(k, v, ttl?)`, `delete(k)`, `getOrCompute(k, loader)`
- **Signatures:** standard `V get(K)`, `void put(K, V)`, `boolean compareAndSet(K, V_old, V_new)`
- **Preconditions:** capacity check (else evict per policy)
- **Side-effects:** insert/update entry; reorder LRU; evict victim on overflow
- **Idempotency:** `put` is idempotent (same k+v); `delete` is idempotent
- **Concurrency:** Sharded locks (per-key stripe) or `ConcurrentHashMap`; LRU update needs care under contention
- **⚠️ Trap:** Cache stampede on hot key expiry → use single-flight / `getOrCompute` with lock.

---

### 8. URL Shortener

- **Ops:** `shorten(longUrl, customAlias?, ttl?)`, `expand(shortCode)`, `disable(shortCode)`, `stats(shortCode)`
- **Signatures:**
  - `shorten(longUrl) → ShortLink` ; `expand(shortCode) → longUrl | 404 | 410(expired)`
- **Preconditions:** owner authenticated for create/disable; alias unique if custom
- **Side-effects:** insert `ShortLink`; on expand: increment click counter, emit `ClickEvent`
- **Idempotency:** `shorten(longUrl)` can return existing mapping (idempotent-by-content)
- **Concurrency:** Alias uniqueness via DB unique index + retry on collision (for generated codes)
- **⚠️ Trap:** Same longUrl twice → return same short OR new one? Clarify upfront.

---

### 9. Tic-Tac-Toe

- **Ops:** `createGame`, `joinGame`, `makeMove(gameId, playerId, row, col)`, `forfeitGame`
- **Preconditions:** correct player's turn; cell empty; game `IN_PROGRESS`
- **Side-effects:** place symbol; check win/draw; transition state
- **Idempotency:** Move identified by `(gameId, moveNum)` — replay safe
- **Concurrency:** Single-threaded per game (per-game lock) — moves serialize naturally

---

### 10. Online Learning

- **Ops:** `searchCourses`, `enroll`, `startLesson`, `markLessonComplete`, `takeQuiz`, `issueCertificate`, `refund`
- **Preconditions:** payment for paid course; prerequisite lessons done (if ordered)
- **Side-effects:** create `Enrollment`; update `Progress`; on full completion → `Certificate`
- **Idempotency:** `markLessonComplete` idempotent (set, not increment)
- **Concurrency:** Per-enrollment lock on progress updates
- **⚠️ Trap:** Refund window vs progress — define cutoff (e.g., < 20% watched).

---

### 11. Twitter

- **Ops:** `postTweet`, `deleteTweet`, `like`, `unlike`, `retweet`, `follow`, `unfollow`, `getTimeline(userId)`
- **Preconditions:** length ≤ 280; rate-limit per user
- **Side-effects:** insert tweet; fanout to follower feeds (push) OR pull at read; increment counters
- **Idempotency:** `like`/`follow` idempotent (set semantics, dedup on `(user, target)`)
- **Concurrency:** Counter updates → use atomic increment or sharded counters
- **⚠️ Trap:** Celebrity fanout (millions of followers) — hybrid push/pull (push for normal, pull for celebs).

---

### 12. Food Delivery

- **Ops:** `searchRestaurants`, `addToCart`, `placeOrder`, `acceptOrder` (restaurant), `assignAgent`, `markPickedUp`, `markDelivered`, `cancelOrder`
- **Preconditions:** restaurant open; items in stock; payment captured; cancel only pre-PREPARING
- **Side-effects:** decrement stock; capture payment; emit `OrderStatusEvent` on each transition; notify customer
- **Idempotency:** `placeOrder` keyed by `cartId+timestamp`; status transitions naturally idempotent (no-op if already in state)
- **Concurrency:** Agent assignment — CAS on `Order.agentId IS NULL`
- **⚠️ Trap:** Order cancellation after dispatch → partial refund + agent reroute logic.

---

### 13. E-commerce / Cart

- **Ops:** `addToCart`, `updateQuantity`, `removeFromCart`, `checkout`, `payOrder`, `shipOrder`, `returnOrder`, `cancelOrder`
- **Preconditions:** inventory ≥ qty at checkout; payment authorized; return within window
- **Side-effects:** on checkout: reserve inventory; on pay: confirm reservation; on ship: decrement; on return: refund + restock
- **Idempotency:** `checkout` keyed by `cartId`; `payOrder` keyed by `orderId+paymentToken`
- **Concurrency:** Inventory: atomic `UPDATE … WHERE qty >= requested`
- **⚠️ Trap:** Reserve-vs-commit window — abandoned carts must release inventory after timeout.

---

### 14. Chess

- **Ops:** `createGame`, `joinGame`, `makeMove(from, to, promo?)`, `resign`, `offerDraw`, `acceptDraw`
- **Preconditions:** legal move per piece + king safety; correct turn; clock not expired
- **Side-effects:** update board; transition `CHECK`/`CHECKMATE`/`STALEMATE`; tick opponent clock
- **Idempotency:** `(gameId, moveNum)` makes moves replay-safe
- **Concurrency:** Per-game lock; clock tick is server-side

---

### 15. Snake & Ladder

- **Ops:** `createGame`, `joinGame`, `rollDice(gameId, playerId)`
- **Preconditions:** player's turn; game `IN_PROGRESS`
- **Side-effects:** advance position; apply snake/ladder; check win
- **Concurrency:** per-game lock

---

### 16. Elevator

- **Ops:** `requestPickup(floor, direction)`, `requestDestination(elevatorId, floor)`, `markMaintenance`, `releaseMaintenance`
- **Preconditions:** floor within bounds; elevator not OOS
- **Side-effects:** enqueue request; dispatcher assigns elevator; update elevator state
- **Concurrency:** Dispatcher is single-threaded per building (or sharded)
- **⚠️ Trap:** Request deduplication: pressing up button 10× should be one request.

---

### 17. Vending Machine

- **Ops:** `insertMoney`, `selectItem`, `dispense`, `cancelAndRefund`
- **Preconditions:** item in stock; money ≥ price
- **Side-effects:** decrement slot count; dispense; return change
- **Idempotency:** transaction state machine prevents double-dispense
- **Concurrency:** typically single-user device — no real concurrency

---

### 18. Splitwise

- **Ops:** `createGroup`, `addMember`, `addExpense(groupId, payerId, amount, splits[])`, `editExpense`, `deleteExpense`, `settleUp`, `getBalances`
- **Preconditions:** payer ∈ group; splits sum = amount; user authorized
- **Side-effects:** insert `Expense` + `Split` rows; recompute pairwise balances; on settle → `Settlement` entry
- **Idempotency:** `addExpense` keyed by `requestId`; settle keyed by `(from, to, amount, ts)`
- **Concurrency:** Per-group lock when mutating balances; or eventual via event log + projection
- **⚠️ Trap:** Edit/delete must reverse previous splits before applying new — wrap in transaction.

---

### 19. Notification System

- **Ops:** `subscribe(userId, topic, channel)`, `unsubscribe`, `send(notification)`, `retryFailed`, `dispatch(scheduledTime)`
- **Preconditions:** user subscribed to topic+channel; respects opt-out + rate limit
- **Side-effects:** enqueue per channel; create `DeliveryAttempt`; on success → SENT; on fail → schedule retry with backoff
- **Idempotency:** `send` keyed by `notificationId+userId+channel` (don't send twice)
- **Concurrency:** Per-channel worker pools; visibility timeout on queue
- **⚠️ Trap:** Distributed dedup — use idempotency key in downstream provider (Twilio, SES).

---

### 20. Chat / Messaging

- **Ops:** `createConversation(participants[])`, `sendMessage(convId, senderId, body, attachments[])`, `markRead(messageId, userId)`, `deleteMessage`, `typingIndicator`
- **Preconditions:** sender is participant; message size limits; not blocked
- **Side-effects:** insert `Message`; fanout to participants; create `ReadReceipt` per recipient on read; emit push notification
- **Idempotency:** `sendMessage` keyed by client-side `clientMsgId` to dedup retries
- **Concurrency:** Per-conversation append order; use sequence number / Lamport clock
- **⚠️ Trap:** Out-of-order delivery — order by server timestamp, not client.

---

### 21. Dropbox

- **Ops:** `upload`, `download`, `delete`, `restore`, `share(fileId, permission)`, `listFolder`, `sync(deviceId, since)`
- **Preconditions:** quota check on upload; ownership/permission on download
- **Side-effects:** create `Version` (prev → OLD); update folder; on delete → TRASHED; share creates `ShareLink`
- **Idempotency:** chunked upload with `uploadSessionId`; commit is idempotent
- **Concurrency:** Last-write-wins OR conflict copy on simultaneous edits
- **⚠️ Trap:** Large file upload → resumable chunks; deduplicate by content hash.

---

### 22. Stock Exchange

- **Ops:** `placeOrder(userId, ticker, side, qty, price, type)`, `cancelOrder`, `getOrderBook(ticker)`, `getPosition(userId, ticker)`
- **Preconditions:** funds (buy) / shares (sell); market open; price/qty > 0
- **Side-effects:** insert into `OrderBook`; matching engine produces `Trade`s; update positions
- **Idempotency:** `placeOrder` keyed by `clientOrderId`
- **Concurrency:** **Matching engine is single-threaded per symbol** (sharded by ticker) for ordering guarantees
- **⚠️ Trap:** Price-time priority — equal price, earlier order wins. Sequence numbers required.

---

### 23. Logger

- **Ops:** `log(level, msg, ctx)`, `addAppender`, `setLevel(loggerName, level)`
- **Preconditions:** level ≥ threshold; filter passes
- **Side-effects:** format + append to all configured sinks
- **Idempotency:** N/A (logs are append-only events)
- **Concurrency:** Async queue between caller and appender; batch flush; thread-safe sinks
- **⚠️ Trap:** Blocking sink (network) shouldn't block app — use async appender with bounded queue.

---

### 24. Rate Limiter

- **Ops:** `allow(clientId, apiKey) → boolean`
- **Preconditions:** rule exists for API
- **Side-effects:** consume token from bucket; refill on tick
- **Idempotency:** N/A — each call is a distinct decision
- **Concurrency:** Atomic decrement (`AtomicLong` or Redis `DECR`); distributed: Redis Lua script for atomicity
- **⚠️ Trap:** Clock skew across nodes → use server-side timestamp; sliding window is more accurate but heavier.

---

### 25. Calendar

- **Ops:** `createEvent`, `inviteAttendees`, `rsvp`, `cancelEvent`, `findFreeSlot(participants[], duration)`, `setReminder`
- **Preconditions:** organizer authorized; attendees exist
- **Side-effects:** insert event + invitees; send invites; schedule reminders
- **Idempotency:** `createEvent` keyed by `requestId`; `rsvp` idempotent (overwrite)
- **Concurrency:** Recurrence expansion can be lazy (on read) vs eager (on write)
- **⚠️ Trap:** Timezones — store UTC, render in user TZ.

---

### 26. Spotify

- **Ops:** `play(songId)`, `pause`, `skip`, `addToPlaylist`, `createPlaylist`, `subscribe`, `cancelSubscription`
- **Preconditions:** subscription `ACTIVE` for premium; song available in region
- **Side-effects:** create `PlayEvent`; update recommendation signals; charge on subscription renewal
- **Idempotency:** subscription create keyed by `userId` (1-1)
- **Concurrency:** Playback session is per-device; playlist edits use last-write-wins

---

### 27. Netflix

- **Ops:** `play(videoId, profileId, fromPosition?)`, `updateProgress(videoId, position)`, `addToWatchlist`, `rate`
- **Preconditions:** subscription active; video available in region; profile belongs to user
- **Side-effects:** create/update `WatchHistory` (upsert by `profileId+videoId`); recommend
- **Idempotency:** `updateProgress` idempotent (set position)
- **Concurrency:** Concurrent stream limit per subscription tier — check on `play`

---

### 28. Airbnb

- **Ops:** `searchListings`, `requestBooking`, `acceptBooking` (host), `cancelBooking`, `checkIn`, `checkOut`, `submitReview`
- **Preconditions:** date range available; payment method on file; host responds in 24h
- **Side-effects:** capture payment on `CONFIRMED`; lock dates; on cancel → refund per policy
- **Idempotency:** `requestBooking` keyed by `requestId`
- **Concurrency:** Date-range availability — SELECT FOR UPDATE on overlapping rows
- **⚠️ Trap:** Two requests for overlapping dates — first-accept wins; second auto-decline.

---

### 29. Banking

- **Ops:** `deposit`, `withdraw`, `transfer(fromAcc, toAcc, amount)`, `getStatement`, `getBalance`, `addBeneficiary`
- **Preconditions:** auth + 2FA on transfer; sufficient balance; fraud check pass; daily limit
- **Side-effects:** debit/credit; create `Transaction`; on cross-bank → settlement after T+N
- **Idempotency:** All money ops keyed by `txnId` (critical — never double-debit)
- **Concurrency:** **Transfer = atomic two-leg** in single DB txn OR saga with compensating action
- **⚠️ Trap:** Lock ordering on cross-account transfer (always lock lower accountId first) → deadlock prevention.

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────┐
│  PILLAR 4 — 6 STEPS (memorize)                       │
├──────────────────────────────────────────────────────┤
│  1. OPS            → enumerate every verb            │
│  2. SIGNATURE      → inputs → output | errors        │
│  3. PRECONDITIONS  → auth / state / quota            │
│  4. SIDE-EFFECTS   → state, events, money            │
│  5. IDEMPOTENCY    → key + window?                   │
│  6. CONCURRENCY    → lock / atomic / CAS             │
└──────────────────────────────────────────────────────┘
```

## Universal probing pattern (for any new op)

```
Q1 (verb):       "What action is the user taking?"
Q2 (signature):  "What params come in? What goes back? What errors?"
Q3 (pre):        "What must be true to allow this?"
Q4 (effects):    "What state changes? What events fire? What money moves?"
Q5 (retry):      "If the client retries, what happens? Safe? Need a key?"
Q6 (race):       "If two callers do this at once, what breaks?"
```

These 6 questions surface ~90% of operational complexity.

---

## Common traps (quick recap)

1. **Forgetting idempotency on money ops** — every payment / transfer / refund needs a dedup key.
2. **Non-atomic multi-step** — "hold N seats" or "transfer between accounts" must be one transaction or saga.
3. **Hidden race on counters / inventory** — `UPDATE … WHERE qty >= n` is atomic; `read-then-write` is not.
4. **Missing error taxonomy** — `IllegalState`, `NotFound`, `Conflict`, `RateLimited` should be distinct.
5. **Cancel after side effect** — cancelling a SHIPPED order isn't free; model partial reversals.
6. **Cache stampede / thundering herd** — single-flight pattern on hot reads.
7. **Distributed dedup ≠ local dedup** — use idempotency key passed to downstream providers.

