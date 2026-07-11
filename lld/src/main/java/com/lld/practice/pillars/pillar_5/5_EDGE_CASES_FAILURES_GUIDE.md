# Pillar 5: EDGE CASES & FAILURE MODES

> **Goal:** For any LLD problem, enumerate the things that can go *wrong* — bad input, exhausted resource, broken invariant, dependency failure, concurrent conflict, partial failure — in under 60 seconds — using one repeatable 6-step pattern.

---

## The ONE pattern (memorize this, ignore everything else)

For every operation / entity, walk these **6 steps in order**:

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 1  INPUT VALIDATION    null / empty / negative / oversize │
│                             / malformed / duplicate / unknown  │
│                                                                 │
│ Step 2  RESOURCE EXHAUSTION What if it's FULL / EMPTY / not    │
│                             FOUND / already USED / locked?     │
│                                                                 │
│ Step 3  STATE / INVARIANT   Op called in wrong state?          │
│                             Business rule violated?            │
│                             (limit, ownership, cutoff)         │
│                                                                 │
│ Step 4  DEPENDENCY FAILURE  Downstream is DOWN / SLOW /        │
│                             returns ERROR / partial response?  │
│                             (payment, DB, notification, queue) │
│                                                                 │
│ Step 5  CONCURRENT CONFLICT Two actors do conflicting things?  │
│                             (double-book, lost update, ABA)    │
│                                                                 │
│ Step 6  PARTIAL FAILURE     Op half-completed; crash mid-way;  │
│                             how do we RECOVER? (compensate,    │
│                             retry, reconcile, dead-letter)     │
└─────────────────────────────────────────────────────────────────┘
```

### The 4 error flavors (vocabulary)

| Flavor | Meaning | HTTP analog | Java |
|---|---|---|---|
| **Validation** | Input is bad — caller's fault | 400 | `IllegalArgumentException` |
| **Conflict** | State doesn't allow op (busy, taken, wrong status) | 409 | `IllegalStateException` / `ConflictException` |
| **NotFound** | Target doesn't exist | 404 | `NotFoundException` |
| **Dependency** | Downstream failed — not caller's fault | 502 / 503 | `ServiceUnavailableException` |

### Recovery patterns (vocabulary)

| Pattern | When to use |
|---|---|
| **Retry + backoff** | Transient dependency failure (network, throttle) |
| **Circuit breaker** | Repeated failures — fail fast, save downstream |
| **Idempotency key** | Safe retries on mutating ops (Pillar 4 ties in) |
| **Compensating txn (saga)** | Partial commit across services — undo previous steps |
| **Dead-letter queue** | Give up after N retries, queue for human review |
| **Reconciliation job** | Periodic sweep to fix drift (inventory, balances) |
| **Read-repair / lazy fix** | Detect inconsistency on read, fix in place |

> **Consistency rule:** Every op in Pillar 4 should answer all 6 questions. Every state in Pillar 3 should have an "illegal entry" answer. Every entity in Pillar 2 should have a "missing / duplicate / orphaned" answer.

---

## How to use the per-domain blocks below

```
DOMAIN
  Step 1 — Input:       null / oversize / duplicate / unknown
  Step 2 — Resource:    full / empty / not found / taken
  Step 3 — State:       wrong status / limit / cutoff / ownership
  Step 4 — Dependency:  payment / DB / queue / external API down
  Step 5 — Concurrency: double-X / lost update / race
  Step 6 — Partial:     crash mid-op; recovery strategy
  ⚠️ Trap: …
```

---

## DOMAIN BLOCKS

### 1. Library

- **Input:** unknown `bookId` / `userId`; reserve own already-borrowed copy; ISBN malformed
- **Resource:** all copies borrowed → queue `Reservation` OR reject; copy marked LOST mid-loan
- **State:** user already at 5-loan limit; user blocked (unpaid fine); return on already-RETURNED loan (no-op)
- **Dependency:** notification service down → notify async / queue; fine payment processor down → mark `Fine.PENDING_PAYMENT`
- **Concurrency:** two users grab last copy → CAS on `Copy.status='AVAILABLE'`; concurrent return + new reserve → FIFO queue order
- **Partial:** crash between `Copy.status=BORROWED` and `Loan` insert → either both via DB txn, or sweep orphan-copies job
- **⚠️ Trap:** OVERDUE recompute on `now > dueDate` — needs scheduler; if scheduler down, lazy compute on read.

---

### 2. Parking Lot

- **Input:** unknown vehicle type → default rate; license plate format invalid; negative payment amount
- **Resource:** lot full → reject at entry; cash inventory empty for change; slot marked OOS mid-park
- **State:** exit without payment → `NotPaid`; pay an already-paid ticket → idempotent no-op; ticket expired (stayed past max hours)
- **Dependency:** payment gateway down → accept cash / queue / hold barrier; license-plate camera fails → manual ticket
- **Concurrency:** two cars at entry, last slot → atomic slot reservation; same ticket scanned twice at exit
- **Partial:** crash between `Payment` insert and `Ticket.status=PAID` → reconcile via payment gateway webhook
- **⚠️ Trap:** Lost ticket scenario — penalty flow needs its own state path.

---

### 3. Hotel

- **Input:** check-out date < check-in; dateRange in the past; party size > room capacity; unknown roomType
- **Resource:** no room of type available for range; payment method declined; assigned room flagged dirty/maintenance
- **State:** cancel after cutoff → fee/no refund; check-in before reservation start date; double check-in same reservation
- **Dependency:** payment processor down → hold reservation as PENDING; pricing/inventory service down → reject new bookings
- **Concurrency:** two guests booking same date-range simultaneously → SELECT FOR UPDATE on availability
- **Partial:** payment captured but reservation insert failed → reconcile + auto-refund; compensating action
- **⚠️ Trap:** Overlapping date ranges (not exact match) — interval-tree or range query, not equality.

---

### 4. Ride-Sharing (Uber)

- **Input:** invalid pickup/drop coords; same pickup=drop; negative price; unknown driver/rider
- **Resource:** no drivers nearby → widen radius, then timeout-cancel; surge pricing limit
- **State:** rider already on a trip; driver OFFLINE accepts ride; cancel after pickup
- **Dependency:** maps API down → use cached route / reject; payment auth fails → reject ride start
- **Concurrency:** N drivers see same request → first-accept wins (CAS on `RideRequest.status='OPEN'`); rider double-tap request → idempotency key
- **Partial:** trip ends but payment fails → mark `Payment.PENDING`, retry; driver crashes mid-trip → another driver picks up?
- **⚠️ Trap:** Driver-cancel after acceptance ≠ rider-cancel; different penalty rules.

---

### 5. Movie Booking (BookMyShow)

- **Input:** seats[] empty; non-contiguous seats when policy requires contiguous; unknown showId / seatId
- **Resource:** seat already BOOKED / HELD; show sold out; payment method invalid
- **State:** book past showtime; confirm an expired Hold → `HoldExpired`; cancel after show ended
- **Dependency:** payment gateway timeout → leave Hold ACTIVE up to TTL; theatre system sync lag
- **Concurrency:** **all-or-nothing** seat group — partial lock = bug; two users grab same seat in different shows is OK (per-show key)
- **Partial:** payment success but booking insert fails → auto-refund within minutes
- **⚠️ Trap:** Hold TTL expiry vs payment-in-flight — extend TTL when payment is processing.

---

### 6. ATM

- **Input:** PIN wrong format; amount not multiple of denominations; amount ≤ 0
- **Resource:** account balance < amount; ATM cash < amount; card retained; daily withdraw limit hit
- **State:** session expired (idle timeout); 3 wrong PINs → BLOCKED; account frozen
- **Dependency:** core banking down → reject; receipt printer offline → skip receipt
- **Concurrency:** user at two ATMs simultaneously → row-lock on account; per-account txn serialization
- **Partial:** money dispensed but balance not debited (network drop) → reconcile via daily report + compensating debit
- **⚠️ Trap:** Network partition mid-withdraw is the **classic** failure — needs 2PC or compensation.

---

### 7. KV Store / Cache

- **Input:** null key; oversize value (> max blob size); negative TTL
- **Resource:** cache full → evict per policy; memory pressure → reject writes
- **State:** read after delete; key already exists on putIfAbsent
- **Dependency:** persistent backing store (write-through) slow → buffer + async flush
- **Concurrency:** concurrent put on same key → last-write-wins OR CAS; LRU reorder under contention → segmented locks
- **Partial:** node crash → in-memory data lost (unless durable); replication lag → stale reads
- **⚠️ Trap:** **Cache stampede** on hot key expiry — single-flight / `getOrCompute` with lock; thundering herd on cold start.

---

### 8. URL Shortener

- **Input:** longUrl malformed / not http(s); custom alias contains reserved chars; alias too short
- **Resource:** generated shortCode collides → retry with new code (loop bounded); custom alias taken
- **State:** expand DISABLED / EXPIRED link → 410 Gone; rate limit per IP for create
- **Dependency:** click-event sink down → fire-and-forget OK (analytics, not critical)
- **Concurrency:** two users claim same custom alias → DB unique-index conflict, retry/fail
- **Partial:** mapping inserted but click counter init failed → counter starts at next click (acceptable)
- **⚠️ Trap:** Open-redirect attack — validate longUrl scheme + blocklist domains.

---

### 9. Tic-Tac-Toe

- **Input:** row/col out of [0..2]; non-integer; cell already occupied
- **Resource:** N/A (board is finite, in-memory)
- **State:** move when game terminated; wrong player's turn; player not in game
- **Dependency:** N/A
- **Concurrency:** two move requests for same game → per-game lock; replay attack (same moveNum twice) → check seq
- **Partial:** crash mid-move → board+move written in single txn; otherwise replay from move log
- **⚠️ Trap:** Forfeit / disconnect — needs timeout sweeper to auto-end abandoned games.

---

### 10. Online Learning

- **Input:** progress > 100% / negative; quiz answer invalid format; unknown courseId
- **Resource:** course unpublished / archived; certificate template missing
- **State:** complete locked lesson (prereq not done); refund after access window
- **Dependency:** video CDN down → fallback to lower bitrate / retry; payment refund processor
- **Concurrency:** mark same lesson complete twice → idempotent (set, not increment); concurrent quiz submissions
- **Partial:** payment captured but enrollment insert failed → auto-refund + alert
- **⚠️ Trap:** Refund window vs progress conflict — define cutoff (e.g., < 20% watched).

---

### 11. Twitter

- **Input:** tweet > 280 chars; empty tweet; reply to deleted tweet; follow self
- **Resource:** target user deleted / suspended; tweet deleted between like and write
- **State:** like already-liked tweet → idempotent; follow already-following → idempotent; rate limit per user
- **Dependency:** fanout queue backed up → degrade to pull model; image CDN slow → skip preview
- **Concurrency:** like counter race → atomic increment / sharded counter; concurrent follow/unfollow
- **Partial:** tweet stored but fanout incomplete → reconcile via pull on read; failed pushes go to retry queue
- **⚠️ Trap:** **Celebrity fanout** — millions of followers; hybrid push/pull, pull for celebs.

---

### 12. Food Delivery

- **Input:** cart empty at checkout; address out of delivery radius; restaurant closed
- **Resource:** item out of stock; no agent available; payment method invalid
- **State:** cancel after PREPARING → reject (or fee); accept already-accepted order
- **Dependency:** restaurant POS offline → manual confirm; maps API for ETA down → cached/default
- **Concurrency:** agent assignment race → CAS on `Order.agentId IS NULL`; inventory decrement race → atomic UPDATE
- **Partial:** payment captured but order insert failed → auto-refund; order placed but no agent within SLA → auto-cancel + refund
- **⚠️ Trap:** Order cancellation after dispatch — partial refund + agent reroute logic.

---

### 13. E-commerce / Cart

- **Input:** qty ≤ 0; unknown SKU; coupon invalid/expired; address invalid
- **Resource:** inventory < qty at checkout; payment method declined; warehouse can't fulfill
- **State:** return after window; cancel after SHIPPED; checkout on empty cart
- **Dependency:** payment gateway down → save cart, retry; shipping API down → estimate from cache
- **Concurrency:** inventory oversell → atomic `UPDATE … WHERE qty >= n`; coupon double-use → unique constraint per `(userId, couponCode)`
- **Partial:** payment success but order create failed → auto-refund within minutes; shipped without label printed → reconcile
- **⚠️ Trap:** Reserve-vs-commit window — abandoned carts must release inventory after timeout (cron).

---

### 14. Chess

- **Input:** illegal move per piece; move puts own king in check; invalid promotion piece
- **Resource:** N/A
- **State:** move when game ended; wrong turn; clock expired
- **Dependency:** N/A (pure)
- **Concurrency:** per-game lock — moves serialize; clock tick is server-authoritative
- **Partial:** disconnect → reconnect grace window, else forfeit on clock
- **⚠️ Trap:** Threefold repetition / 50-move draw — server must track, not trust client.

---

### 15. Snake & Ladder

- **Input:** roll request when not your turn; invalid playerId
- **State:** roll after game finished
- **Concurrency:** per-game lock
- **Partial:** dice rolled but position write failed → retry idempotent via `(gameId, seqNum)`

---

### 16. Elevator

- **Input:** floor out of bounds; invalid direction
- **Resource:** all elevators OOS / MAINTENANCE
- **State:** open door mid-motion (safety reject); request opposite direction while committed
- **Dependency:** sensor fails → fail-safe stop
- **Concurrency:** button press 10× → dedup request per `(floor, direction)` and per elevator+floor pair
- **Partial:** stuck between floors → manual override / safety protocol
- **⚠️ Trap:** Starvation — top-floor request never served under heavy bottom traffic; need fairness in scheduler.

---

### 17. Vending Machine

- **Input:** invalid coin denom; select non-existent slot code
- **Resource:** slot empty / out of stock; insufficient money; can't make change
- **State:** dispense without payment; cancel after dispense
- **Concurrency:** typically single-user — minimal
- **Partial:** dispense motor jams mid-dispense → refund + flag slot for service
- **⚠️ Trap:** Change-making — if no exact change, reject sale (don't short-change).

---

### 18. Splitwise

- **Input:** splits don't sum to expense amount (within rounding); negative amount; payer not in group
- **Resource:** group deleted; user removed mid-expense
- **State:** settle zero balance (no-op); edit settled expense
- **Dependency:** notification → async; FX rate service down → use last-known
- **Concurrency:** concurrent expense add + balance read → per-group lock OR event-sourced ledger
- **Partial:** expense persisted but balance recompute failed → reconcile from `Expense + Split` log (source of truth)
- **⚠️ Trap:** Edit/delete must **reverse** prior splits before applying new ones, in single txn.

---

### 19. Notification System

- **Input:** unknown user / topic / template; body too large; invalid channel
- **Resource:** channel rate limit hit (Twilio quota); template missing for locale
- **State:** user opted out; user blocked sender; quiet hours active
- **Dependency:** downstream provider (SES/Twilio/APNs) down → retry with backoff; rate-limited → throttle
- **Concurrency:** same notification dispatched twice → dedup by `notificationId+userId+channel`
- **Partial:** sent to provider but ack lost → may double-send; use provider idempotency key
- **⚠️ Trap:** Retry storm — exponential backoff + DLQ after N attempts.

---

### 20. Chat / Messaging

- **Input:** empty body + no attachments; oversize body; sender not in conversation
- **Resource:** attachment storage full / blocked; conversation deleted; recipient blocked sender
- **State:** delete message after window; mark-read on already-read; type into closed conversation
- **Dependency:** push notification service down → store undelivered, replay on connect; media storage slow → async upload
- **Concurrency:** client retries → dedup by client `clientMsgId`; ordering across devices → server sequence number
- **Partial:** message persisted but push failed → recipient sees on next connect; receipt write fails → no impact on message
- **⚠️ Trap:** Out-of-order delivery — order by server timestamp, not client clock; clock-skew across devices.

---

### 21. Dropbox

- **Input:** invalid filename (reserved chars); path traversal `../`; size > limit
- **Resource:** quota exceeded; folder doesn't exist; permission denied
- **State:** restore from trash past purge; share when sharing disabled by admin
- **Dependency:** storage backend (S3) slow / throttled → retry with backoff; sync conflict
- **Concurrency:** two devices edit same file → last-write-wins OR conflict copy; chunked upload race
- **Partial:** chunk uploaded but commit failed → resume by `uploadSessionId`; orphan chunks GC'd after TTL
- **⚠️ Trap:** Large file = resumable chunked upload; dedup by content hash to save storage.

---

### 22. Stock Exchange

- **Input:** qty ≤ 0; price ≤ 0; unknown ticker; price outside circuit-breaker band
- **Resource:** insufficient funds (buy) / shares (sell); market closed; ticker halted
- **State:** cancel already-filled order; modify post-fill; order on suspended account
- **Dependency:** clearing/settlement system down → queue trades, defer settlement
- **Concurrency:** **matching engine single-threaded per symbol** for ordering; sequence-numbered orders for fairness
- **Partial:** trade matched but position update lag → trade log is source of truth, positions reconciled
- **⚠️ Trap:** **Price-time priority** — equal price, earlier order wins. Sequence numbers required for determinism.

---

### 23. Logger

- **Input:** null message; unsafe format string (injection); ctx map too large
- **Resource:** disk full → rotate / drop oldest; queue full → drop / block
- **State:** appender misconfigured; level changed mid-flight
- **Dependency:** remote log sink down → buffer locally, retry; if buffer full, drop or block
- **Concurrency:** multi-thread log calls → thread-safe queue; async batch flush
- **Partial:** crash before flush → lose buffered logs; trade-off vs sync flush perf
- **⚠️ Trap:** Blocking sink (network) shouldn't block app — async appender with bounded queue + drop policy.

---

### 24. Rate Limiter

- **Input:** missing apiKey / clientId; unknown rule
- **Resource:** distributed store (Redis) down → fail-open (allow) OR fail-closed (deny) per policy
- **State:** N/A (stateless decision per request)
- **Dependency:** Redis latency adds to every request → local cache + async sync
- **Concurrency:** distributed counter race → Redis `DECR` (atomic) or Lua script
- **Partial:** local node decremented but crash before remote sync → minor over-allow acceptable
- **⚠️ Trap:** **Clock skew across nodes** → server timestamps; sliding window more accurate but heavier.

---

### 25. Calendar

- **Input:** start > end; duration negative; unknown attendee; invalid recurrence rule
- **Resource:** organizer's calendar deleted; attendee not reachable
- **State:** RSVP after event ended; modify recurring exception
- **Dependency:** email invite service down → queue + retry
- **Concurrency:** two organizers edit same shared event → ETag / version check
- **Partial:** event saved but invites not sent → background sweeper sends missing invites
- **⚠️ Trap:** **Timezones** — store UTC, render in user TZ; DST transitions break naive math.

---

### 26. Spotify

- **Input:** songId not in region catalog; playlist > max size; subscription action without auth
- **Resource:** subscription expired → degrade to free tier; song removed from catalog
- **State:** play premium-only on free account; concurrent stream limit hit
- **Dependency:** audio CDN slow → lower bitrate / cached; payment processor for renewal down
- **Concurrency:** play on N devices → enforce limit per subscription tier
- **Partial:** subscription charged but state not updated → reconcile via webhook
- **⚠️ Trap:** Offline downloads vs subscription expiry — must invalidate cached tracks.

---

### 27. Netflix

- **Input:** profile not owned by user; videoId not in region; resume position > duration
- **Resource:** subscription expired; video pulled from catalog
- **State:** stream limit (e.g., 2 concurrent) per plan; kids profile + adult content
- **Dependency:** CDN failure → ABR fallback, retry; DRM service down → reject playback
- **Concurrency:** same profile on N devices → enforce per plan
- **Partial:** updateProgress at end fails → resume from last good position
- **⚠️ Trap:** **DRM key rotation** mid-stream → smooth re-key; otherwise playback stutters/fails.

---

### 28. Airbnb

- **Input:** dateRange in past; guest count > capacity; unknown listingId
- **Resource:** dates already booked; payment method invalid; host unresponsive past 24h
- **State:** cancel post-cutoff → policy-based refund; check-in before allowed time
- **Dependency:** payment capture fails on accept → mark `PAYMENT_FAILED`, host can re-accept; messaging service down
- **Concurrency:** two requests for overlapping dates → SELECT FOR UPDATE; first-accept wins, others auto-decline
- **Partial:** payment captured but booking insert failed → auto-refund + alert
- **⚠️ Trap:** **Overlapping date ranges** — range overlap query, not equality; instant-book vs request-to-book paths differ.

---

### 29. Banking

- **Input:** amount ≤ 0; cross-currency without FX; account number checksum invalid; beneficiary not whitelisted
- **Resource:** insufficient balance; daily transfer limit hit; account frozen
- **State:** transfer on closed account; reverse a SETTLED txn (needs chargeback path)
- **Dependency:** correspondent bank down → queue with SLA; FX rate service down → reject or use last-known
- **Concurrency:** **lock ordering** on cross-account transfer (lower accountId first) → deadlock avoidance; row lock per account
- **Partial:** debit succeeded, credit failed → compensating credit-back (saga); 2PC for high-value
- **⚠️ Trap:** **Never double-debit** — every money op needs a `txnId` idempotency key checked at every layer.

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────┐
│  PILLAR 5 — 6 STEPS (memorize)                       │
├──────────────────────────────────────────────────────┤
│  1. INPUT          → null/empty/oversize/malformed   │
│  2. RESOURCE       → full/empty/not found/taken      │
│  3. STATE          → wrong status/limit/cutoff       │
│  4. DEPENDENCY     → downstream down/slow/error      │
│  5. CONCURRENCY    → double-X/lost update/race       │
│  6. PARTIAL        → crash mid-op → recovery plan    │
└──────────────────────────────────────────────────────┘
```

## Universal probing pattern (for any new op / entity)

```
Q1 (input):       "What if the input is null / empty / huge / malformed / unknown ID?"
Q2 (resource):    "What if the resource is full / empty / already used / not found?"
Q3 (state):       "What if the entity is in the wrong state? Limit hit? Past cutoff?"
Q4 (dependency):  "What if payment/DB/queue/external API is down or slow?"
Q5 (concurrency): "What if two callers do this at the same time? Lost update? Double-do?"
Q6 (partial):     "What if we crash halfway? Money out, no record? How do we recover?"
```

These 6 questions surface ~90% of edge cases.

---

## Common traps (quick recap)

1. **Validation at the wrong layer** — validate inputs at API edge; invariants at entity; both, not either.
2. **Silent failure** — swallowing exceptions hides bugs. Distinguish *expected* (return error) from *unexpected* (throw + alert).
3. **No idempotency on money/mutations** — every retry-able mutating op needs a dedup key (ties to Pillar 4).
4. **Cancel after side-effect** — refund / reroute / restock are real work, not free state transitions.
5. **Stale read after write** — replication lag breaks "read your own writes"; use sticky reads or read-from-leader.
6. **Cache stampede / thundering herd** — single-flight pattern on hot reads.
7. **No reconciliation job** — drift between systems is inevitable; periodic sweeper is mandatory for money/inventory.
8. **Fail-open vs fail-closed** — for rate limiter / auth, pick the policy *intentionally* and document it.
9. **Retry without backoff** — retry storms take down the dependency you were trying to call.
10. **Distributed dedup ≠ local dedup** — pass idempotency key all the way to downstream providers.

