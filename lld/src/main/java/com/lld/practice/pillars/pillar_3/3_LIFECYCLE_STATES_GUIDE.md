# Pillar 3: LIFECYCLE & STATES

> **Goal:** For any stateful entity in an LLD problem, identify its states, valid transitions, reversals, time-based events, and guards — in under 60 seconds — using one repeatable 6-step pattern.

---

## The ONE pattern (memorize this, ignore everything else)

For every **stateful entity** (usually the Tier-3 Transaction from Pillar 2), walk these **6 steps in order**:

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 1  STATES               Enumerate every state the entity  │
│                              can be in. (enum)                 │
│                                                                 │
│ Step 2  INITIAL / TERMINAL   Which is the entry state?         │
│                              Which are final (no exit)?        │
│                                                                 │
│ Step 3  TRANSITIONS          For each edge: FROM → TO          │
│                              + trigger (event/action)          │
│                                                                 │
│ Step 4  REVERSALS / UNDO     Can we go backwards?              │
│                              (cancel, refund, restore, reopen) │
│                                                                 │
│ Step 5  TIME-BASED           Auto-transitions on timer:        │
│                              expiry, timeout, scheduled job    │
│                                                                 │
│ Step 6  GUARDS / INVARIANTS  Preconditions before transition;  │
│                              side effects on transition        │
│                              (release seat, refund money…)     │
└─────────────────────────────────────────────────────────────────┘
```

### The 4 transition flavors (vocabulary)

| Flavor | Meaning | Example |
|---|---|---|
| **User-driven** | Actor performs action | `Booking.confirm()` |
| **System-driven** | Internal event / dependency | `Payment.success → Booking.CONFIRMED` |
| **Time-driven** | Scheduler / TTL fires | `Hold expires after 5 min` |
| **Reversal** | Backward edge (cancel/undo) | `BOOKED → CANCELLED` |

### State machine → Java representation

| Concern | Java choice |
|---|---|
| States | `enum Status { ... }` |
| Transition table | `Map<State, Set<State>>` allowed edges |
| Trigger | method on entity OR event handler |
| Side effects | invoked inside transition method (release, refund, notify) |
| Time-based | scheduled task / TTL field + sweeper |
| Guard | `if` precondition → throw `IllegalStateException` |

> **Consistency rule:** Every terminal state in Step 2 must have NO outgoing edge in Step 3. Every state in Step 1 must be reachable from initial. Every reversal in Step 4 must also appear in Step 3.

---

## How to use the per-domain blocks below

```
DOMAIN — Entity
  Step 1 — States:       [S1, S2, …]
  Step 2 — Initial/Terminal: init=…, terminal=[…]
  Step 3 — Transitions:  S1 →[trigger]→ S2
  Step 4 — Reversals:    which edges go backward
  Step 5 — Time-based:   expiry/timeout rules
  Step 6 — Guards/Side-effects: per transition
  ⚠️ Trap: …
```

---

## DOMAIN BLOCKS

### 1. Library — `Loan`

- **States:** `ACTIVE`, `RETURNED`, `OVERDUE`, `LOST`
- **Initial / Terminal:** init = `ACTIVE`; terminal = `RETURNED`, `LOST`
- **Transitions:**
  - `ACTIVE →[return]→ RETURNED`
  - `ACTIVE →[dueDate passed]→ OVERDUE`
  - `OVERDUE →[return + fine paid]→ RETURNED`
  - `OVERDUE →[declared lost]→ LOST`
- **Reversals:** none (no un-return)
- **Time-based:** `ACTIVE → OVERDUE` when `now > dueDate` (scheduler sweep)
- **Guards / Side-effects:**
  - On `RETURNED`: release `Copy`, notify next `Reservation`
  - On `OVERDUE`: create `Fine`
  - On `LOST`: charge replacement cost
- **⚠️ Trap:** `OVERDUE` is not terminal — book can still be returned.

---

### 2. Parking Lot — `Ticket`

- **States:** `ISSUED`, `PAID`, `EXITED`, `LOST`
- **Initial / Terminal:** init = `ISSUED`; terminal = `EXITED`, `LOST`
- **Transitions:**
  - `ISSUED →[payment success]→ PAID`
  - `PAID →[barrier opens]→ EXITED`
  - `ISSUED →[ticket lost]→ LOST →[penalty paid]→ EXITED`
- **Reversals:** none
- **Time-based:** rolling fee accrues per minute on `ISSUED`
- **Guards / Side-effects:**
  - On `PAID`: compute fee = `exitTime - entryTime` × rate
  - On `EXITED`: release `Slot`
- **⚠️ Trap:** Payment must precede `EXITED`; barrier is the boundary, not the payment.

---

### 3. Hotel — `Reservation`

- **States:** `PENDING`, `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`, `NO_SHOW`
- **Initial / Terminal:** init = `PENDING`; terminal = `CHECKED_OUT`, `CANCELLED`, `NO_SHOW`
- **Transitions:**
  - `PENDING →[payment ok]→ CONFIRMED`
  - `CONFIRMED →[guest arrives]→ CHECKED_IN`
  - `CHECKED_IN →[checkout]→ CHECKED_OUT`
  - `CONFIRMED →[cancel before cutoff]→ CANCELLED`
  - `CONFIRMED →[date passes, no arrival]→ NO_SHOW`
- **Reversals:** `CANCELLED` is reversal of `CONFIRMED`
- **Time-based:** cancellation cutoff (e.g., 24h); `NO_SHOW` if not checked in by end-of-day
- **Guards / Side-effects:**
  - On `CONFIRMED`: lock `Room` for date range
  - On `CANCELLED`: free room, refund per policy
  - On `CHECKED_OUT`: free room, issue `Invoice`
- **⚠️ Trap:** Cancellation policy may yield partial refund — model `Refund` separately.

---

### 4. Ride-Sharing (Uber) — `Trip`

- **States:** `REQUESTED`, `MATCHED`, `DRIVER_ARRIVING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- **Initial / Terminal:** init = `REQUESTED`; terminal = `COMPLETED`, `CANCELLED`
- **Transitions:**
  - `REQUESTED →[driver accepts]→ MATCHED →[en route]→ DRIVER_ARRIVING →[pickup]→ IN_PROGRESS →[drop-off]→ COMPLETED`
  - any pre-`IN_PROGRESS` →[cancel]→ `CANCELLED`
- **Reversals:** cancel allowed only before pickup
- **Time-based:** `REQUESTED → CANCELLED` if no driver in N seconds
- **Guards / Side-effects:**
  - On `MATCHED`: mark driver/vehicle busy
  - On `COMPLETED`: trigger `Payment`, `Rating`
  - On `CANCELLED`: free driver, charge cancellation fee if after grace
- **⚠️ Trap:** Driver-cancel vs rider-cancel produce different fees/penalties.

---

### 5. Movie Booking — `Booking` / `Hold`

- **States (Hold):** `HELD`, `CONFIRMED`, `EXPIRED`
- **States (Booking):** `CONFIRMED`, `CANCELLED`, `USED`
- **Initial / Terminal:** Hold init = `HELD`; Booking terminal = `CANCELLED`, `USED`
- **Transitions:**
  - `Hold.HELD →[payment success]→ CONFIRMED` (becomes Booking)
  - `Hold.HELD →[timer]→ EXPIRED` (release seats)
  - `Booking.CONFIRMED →[cancel before cutoff]→ CANCELLED`
  - `Booking.CONFIRMED →[show ends]→ USED`
- **Reversals:** `CANCELLED` reverses `CONFIRMED`
- **Time-based:** Hold TTL ~5-10 min; `USED` auto-set after show end
- **Guards / Side-effects:**
  - On `HELD`: mark seats unavailable
  - On `EXPIRED` / `CANCELLED`: release seats, refund
  - On `CONFIRMED`: send ticket
- **⚠️ Trap:** `Hold` and `Booking` are two state machines linked at one transition.

---

### 6. ATM — `Session`

- **States:** `IDLE`, `AUTHENTICATING`, `AUTHENTICATED`, `TRANSACTING`, `CLOSED`, `BLOCKED`
- **Initial / Terminal:** init = `IDLE`; terminal = `CLOSED`, `BLOCKED`
- **Transitions:**
  - `IDLE →[card insert]→ AUTHENTICATING →[pin ok]→ AUTHENTICATED →[op]→ TRANSACTING →[done]→ AUTHENTICATED`
  - `AUTHENTICATING →[3 wrong pins]→ BLOCKED`
  - `AUTHENTICATED →[eject]→ CLOSED`
- **Reversals:** none
- **Time-based:** auto-eject if idle > N seconds
- **Guards / Side-effects:**
  - On `BLOCKED`: retain card, lock account
  - On `CLOSED`: return card, print receipt
- **⚠️ Trap:** Session != Transaction; one session has many transactions.

---

### 7. KV Store / Cache — `Entry`

- **States:** `LIVE`, `EXPIRED`, `EVICTED`
- **Initial / Terminal:** init = `LIVE`; terminal = `EXPIRED`, `EVICTED` (both → removed)
- **Transitions:**
  - `LIVE →[TTL elapsed]→ EXPIRED`
  - `LIVE →[capacity full + policy picks]→ EVICTED`
  - `LIVE →[user delete]→ EVICTED`
- **Reversals:** none
- **Time-based:** TTL sweeper or lazy on-read expiry check
- **Guards / Side-effects:**
  - On `EXPIRED`/`EVICTED`: free memory, fire callback (optional)
  - On `LIVE` write: reset access time (LRU)
- **⚠️ Trap:** Lazy vs active expiry — lazy may serve stale until next read.

---

### 8. URL Shortener — `ShortLink`

- **States:** `ACTIVE`, `EXPIRED`, `DISABLED`
- **Initial / Terminal:** init = `ACTIVE`; terminal = `EXPIRED`, `DISABLED`
- **Transitions:**
  - `ACTIVE →[TTL]→ EXPIRED`
  - `ACTIVE →[owner disable]→ DISABLED`
- **Reversals:** `DISABLED → ACTIVE` (owner re-enable)
- **Time-based:** TTL per link
- **Guards / Side-effects:** non-`ACTIVE` returns 410 Gone instead of redirect

---

### 9. Tic-Tac-Toe — `Game`

- **States:** `WAITING_FOR_PLAYERS`, `IN_PROGRESS`, `WON`, `DRAW`, `ABANDONED`
- **Initial / Terminal:** init = `WAITING_FOR_PLAYERS`; terminal = `WON`, `DRAW`, `ABANDONED`
- **Transitions:**
  - `WAITING → IN_PROGRESS` when 2 players present
  - `IN_PROGRESS →[winning line]→ WON`
  - `IN_PROGRESS →[board full]→ DRAW`
  - `IN_PROGRESS →[player quits]→ ABANDONED`
- **Reversals:** none (move is permanent)
- **Time-based:** turn timeout → forfeit
- **Guards:** cell must be empty; correct player's turn

---

### 10. Online Learning — `Enrollment`

- **States:** `ENROLLED`, `IN_PROGRESS`, `COMPLETED`, `EXPIRED`, `REFUNDED`
- **Initial / Terminal:** init = `ENROLLED`; terminal = `COMPLETED`, `EXPIRED`, `REFUNDED`
- **Transitions:**
  - `ENROLLED →[first lesson]→ IN_PROGRESS →[all lessons + quiz]→ COMPLETED`
  - any pre-completion →[access window passes]→ `EXPIRED`
  - `ENROLLED →[refund window]→ REFUNDED`
- **Reversals:** `REFUNDED` reverses payment
- **Time-based:** course access expiry; refund eligibility window
- **Guards / Side-effects:** On `COMPLETED`: issue `Certificate`

---

### 11. Twitter — `Tweet`

- **States:** `DRAFT`, `PUBLISHED`, `DELETED`, `HIDDEN`
- **Initial / Terminal:** init = `DRAFT`; terminal = `DELETED`
- **Transitions:**
  - `DRAFT →[post]→ PUBLISHED`
  - `PUBLISHED →[author delete]→ DELETED`
  - `PUBLISHED ↔ HIDDEN` (moderation toggle)
- **Time-based:** scheduled tweet (`DRAFT → PUBLISHED` at time T)
- **Guards:** length ≤ 280, rate-limit per user

---

### 12. Food Delivery — `Order`

- **States:** `PLACED`, `ACCEPTED`, `PREPARING`, `READY`, `PICKED_UP`, `DELIVERED`, `CANCELLED`
- **Initial / Terminal:** init = `PLACED`; terminal = `DELIVERED`, `CANCELLED`
- **Transitions:** linear chain; `CANCELLED` allowed up to `PREPARING`
- **Reversals:** `CANCELLED` only pre-prep
- **Time-based:** auto-cancel if restaurant doesn't accept in N minutes
- **Guards / Side-effects:**
  - On each transition: emit `OrderStatusEvent` + notify customer
  - On `CANCELLED`: refund, free agent

---

### 13. E-commerce — `Order`

- **States:** `CREATED`, `PAID`, `PACKED`, `SHIPPED`, `DELIVERED`, `RETURNED`, `CANCELLED`
- **Initial / Terminal:** init = `CREATED`; terminal = `DELIVERED` (or `RETURNED`/`CANCELLED`)
- **Transitions:** linear; `RETURNED` only after `DELIVERED` within return window
- **Reversals:** `CANCELLED` pre-ship; `RETURNED` post-deliver
- **Time-based:** return window (e.g., 30 days)
- **Guards:** Inventory check on `PAID`; refund on `CANCELLED`/`RETURNED`

---

### 14. Chess — `Game`

- **States:** `WAITING`, `IN_PROGRESS`, `CHECK`, `CHECKMATE`, `STALEMATE`, `DRAW_AGREED`, `RESIGNED`, `TIMEOUT`
- **Initial / Terminal:** init = `WAITING`; terminal = `CHECKMATE`, `STALEMATE`, `DRAW_AGREED`, `RESIGNED`, `TIMEOUT`
- **Transitions:** `IN_PROGRESS ↔ CHECK`; any → terminal via rules
- **Time-based:** per-move clock → `TIMEOUT`
- **Guards:** legal move validation per piece

---

### 15. Snake & Ladder — `Game`

- **States:** `WAITING`, `IN_PROGRESS`, `FINISHED`
- **Initial / Terminal:** init = `WAITING`; terminal = `FINISHED`
- **Transitions:** linear; `FINISHED` when a player reaches 100
- **Guards:** turn order, dice roll = 6 to start (optional rule)

---

### 16. Elevator — `Elevator`

- **States:** `IDLE`, `MOVING_UP`, `MOVING_DOWN`, `STOPPED`, `MAINTENANCE`, `OUT_OF_SERVICE`
- **Initial / Terminal:** init = `IDLE`; terminal = none (cyclic)
- **Transitions:** `IDLE → MOVING_* → STOPPED → IDLE`; manual toggle to `MAINTENANCE`
- **Time-based:** door open timeout → close
- **Guards:** can't accept opposite-direction request while moving

---

### 17. Vending Machine — `Transaction`

- **States:** `IDLE`, `COLLECTING_MONEY`, `SELECTING_ITEM`, `DISPENSING`, `DISPENSED`, `REFUNDED`, `CANCELLED`
- **Initial / Terminal:** init = `IDLE`; terminal = `DISPENSED`, `REFUNDED`, `CANCELLED`
- **Transitions:** linear with cancel/refund branches
- **Time-based:** auto-refund if user idle mid-flow
- **Guards:** insufficient money / out-of-stock → `REFUNDED`

---

### 18. Splitwise — `Expense` / `Balance`

- **Expense States:** `ACTIVE`, `EDITED`, `DELETED`
- **Balance States:** `OPEN`, `SETTLED`
- **Transitions:**
  - `Expense.ACTIVE →[edit]→ EDITED →[edit]→ EDITED …`
  - `Expense →[delete]→ DELETED` (reverses splits)
  - `Balance.OPEN →[settlement]→ SETTLED` (then resets to OPEN if new expense)
- **Reversals:** delete expense recomputes balances
- **Guards:** can't settle a zero balance

---

### 19. Notification — `Notification` / `DeliveryAttempt`

- **States:** `QUEUED`, `SENDING`, `SENT`, `FAILED`, `RETRY_SCHEDULED`, `DEAD_LETTER`
- **Initial / Terminal:** init = `QUEUED`; terminal = `SENT`, `DEAD_LETTER`
- **Transitions:**
  - `QUEUED → SENDING →{SENT | FAILED}`
  - `FAILED →[attempts < max]→ RETRY_SCHEDULED → SENDING`
  - `FAILED →[attempts ≥ max]→ DEAD_LETTER`
- **Time-based:** exponential backoff between retries
- **Guards:** respect user opt-out; channel rate limits

---

### 20. Chat — `Message`

- **States:** `SENDING`, `SENT`, `DELIVERED`, `READ`, `DELETED`, `FAILED`
- **Initial / Terminal:** init = `SENDING`; terminal = `READ`, `DELETED`
- **Transitions:** `SENDING → SENT → DELIVERED → READ` (per recipient); `SENDING → FAILED`
- **Reversals:** `* → DELETED` (sender deletes within window)
- **Time-based:** "delete for everyone" window (e.g., 7 min)
- **Guards / Side-effects:**
  - Per-recipient `ReadReceipt` row created on `READ`
  - Group chat: `DELIVERED` when all in group received

---

### 21. Dropbox — `File` / `Version`

- **File States:** `ACTIVE`, `TRASHED`, `PERMANENTLY_DELETED`
- **Version States:** `CURRENT`, `OLD`
- **Transitions:**
  - `ACTIVE →[delete]→ TRASHED →[purge after N days]→ PERMANENTLY_DELETED`
  - `TRASHED →[restore]→ ACTIVE`
  - New upload: prev `CURRENT → OLD`, new = `CURRENT`
- **Time-based:** trash auto-purge (e.g., 30 days)
- **Guards:** quota check on upload

---

### 22. Stock Exchange — `Order`

- **States:** `NEW`, `OPEN`, `PARTIALLY_FILLED`, `FILLED`, `CANCELLED`, `REJECTED`, `EXPIRED`
- **Initial / Terminal:** init = `NEW`; terminal = `FILLED`, `CANCELLED`, `REJECTED`, `EXPIRED`
- **Transitions:**
  - `NEW →[validation]→ OPEN | REJECTED`
  - `OPEN → PARTIALLY_FILLED → FILLED` (as matches happen)
  - `OPEN/PARTIALLY_FILLED →[user]→ CANCELLED`
  - `OPEN →[day end / TTL]→ EXPIRED`
- **Time-based:** GTC vs DAY vs IOC order types
- **Guards:** sufficient funds (buy) / shares (sell); price/qty > 0

---

### 23. Logger — `LogEvent`

- (stateless — event is fire-and-forget)
- **States:** `EMITTED → FILTERED | FORMATTED → APPENDED` (pipeline stages, not entity states)
- **Time-based:** async batch flush, rotation by size/time
- **Guards:** level ≥ threshold; filter passes

---

### 24. Rate Limiter — `Bucket`

- **States:** `AVAILABLE` (tokens > 0), `THROTTLED` (tokens = 0)
- **Transitions:**
  - `AVAILABLE →[consume]→ AVAILABLE | THROTTLED`
  - `THROTTLED →[refill tick]→ AVAILABLE`
- **Time-based:** refill rate (e.g., 10 tokens/sec)
- **Guards:** on `THROTTLED` consume → reject request (429)

---

### 25. Calendar — `Event`

- **States:** `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- **Transitions:** time-driven `SCHEDULED → IN_PROGRESS → COMPLETED`; user `→ CANCELLED`
- **Time-based:** state derived from `now` vs `start/end`; reminder fires at `start - offset`
- **Guards:** RSVP deadline; recurrence rule expansion

---

### 26. Spotify — `Subscription`

- **States:** `TRIAL`, `ACTIVE`, `PAST_DUE`, `CANCELLED`, `EXPIRED`
- **Transitions:**
  - `TRIAL →[payment]→ ACTIVE`
  - `ACTIVE →[renewal fail]→ PAST_DUE →[paid]→ ACTIVE`
  - `PAST_DUE →[grace ends]→ EXPIRED`
  - any →[user cancel]→ `CANCELLED` (access until period end)
- **Time-based:** trial period, renewal cycle, grace period

---

### 27. Netflix — `WatchHistory` entry

- **States:** `STARTED`, `IN_PROGRESS`, `COMPLETED`, `ABANDONED`
- **Transitions:** progress %; `COMPLETED` at ≥90% watched
- **Time-based:** mark `ABANDONED` if not resumed in N days

---

### 28. Airbnb — `Booking`

- **States:** `REQUESTED`, `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`, `DECLINED`
- **Transitions:**
  - `REQUESTED →[host accepts]→ CONFIRMED` (or `DECLINED`)
  - `CONFIRMED → CHECKED_IN → CHECKED_OUT`
  - `CONFIRMED →[cancel]→ CANCELLED` (refund per policy)
- **Time-based:** host must respond in 24h or `DECLINED`
- **Guards:** date range available; payment captured on `CONFIRMED`

---

### 29. Banking — `Transaction`

- **States:** `INITIATED`, `PENDING`, `POSTED`, `FAILED`, `REVERSED`
- **Transitions:**
  - `INITIATED →[validation]→ PENDING →[settled]→ POSTED`
  - `PENDING →[bank rejects]→ FAILED`
  - `POSTED →[dispute / chargeback]→ REVERSED`
- **Time-based:** settlement window (T+1, T+2)
- **Guards:** sufficient balance, fraud check; reversal creates compensating txn

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────┐
│  PILLAR 3 — 6 STEPS (memorize)                       │
├──────────────────────────────────────────────────────┤
│  1. STATES         → enum every status               │
│  2. INITIAL/TERM   → entry + dead-ends               │
│  3. TRANSITIONS    → FROM →[trigger]→ TO             │
│  4. REVERSALS      → cancel / undo / refund          │
│  5. TIME-BASED     → TTL, timeout, scheduler         │
│  6. GUARDS / SIDE  → preconditions + effects         │
└──────────────────────────────────────────────────────┘
```

## Universal probing pattern (for any new domain not listed)

```
Q1 (states):     "What are all the statuses this entity can be in?"
Q2 (terminal):   "Which statuses are dead-ends? Which is the start?"
Q3 (triggers):   "What action / event causes each transition?"
Q4 (reversal):   "Can we undo? Cancel? Refund? Restore?"
Q5 (time):       "Is there a timeout, expiry, or scheduled change?"
Q6 (effects):    "What else happens on this transition? (release,
                 notify, refund, audit log)"
```

These 6 questions surface ~90% of lifecycle complexity.

---

## Common traps (quick recap)

1. **Missing terminal check** — terminal states must have NO outgoing edges. Drawing one creates infinite loops.
2. **Reversal isn't a backward edge** — `CANCELLED` is a new terminal state, not a return to `PENDING`.
3. **Time-based transitions need a sweeper** — `OVERDUE`, `EXPIRED`, `NO_SHOW` don't happen on their own.
4. **Side effects matter as much as state** — releasing a seat / refunding money on cancel is the *real* work.
5. **Two coupled state machines** — `Hold` + `Booking`, `Order` + `Payment`, `Reservation` + `Invoice`. Model both.
6. **Guard vs invariant** — guard blocks an invalid transition; invariant must hold in every state (e.g., `tokens ≥ 0`).

