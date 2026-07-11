# Tutor Session 01 — Parking Lot

**Problem:** *"Design a multi-floor Parking Lot supporting multiple vehicle types, ticketing, and hourly pricing."*

---

## Pillar 1 — SCOPE

### Q1.1: Who are the actors, and what are the 4–5 core operations you'll expose?

**Hint:** think of the verbs a real driver / operator does at the gate. Don't list 15 ops; pick the essential ones.

**Your answer:**

Actors — ops
- Driver → `park`, `exit`
- Ticket Counter → `calculateFee`

**Feedback:**

Good start — you named the human actors correctly. Two refinements for interview polish:

1. **Split "park" into two ops.** In LLD we separate *entering the lot* (getting a slot + ticket) from *paying* (settling the fee). So `park` becomes `entry` (issue ticket, assign slot) and `exit` needs `pay` before the barrier opens. This lets us model the state machine cleanly later.
2. **"Ticket Counter" is a role, not an actor at the API level.** Both driver and operator hit the same system; think in terms of *system operations*, not *who clicks the button*.

**Ideal answer (Stage 1 — MVP):**

- **Actors:** `Driver` (primary), `Admin` (secondary — configures rates, marks slots out-of-service; ignore for Stage 1)
- **Core operations (4):**
  1. `entry(licensePlate, vehicleType) → Ticket` — assign a slot, issue ticket with entryTime.
  2. `pay(ticketId) → Payment` — compute fee based on duration, mark ticket paid.
  3. `exit(ticketId) → Ack` — release the slot, open barrier (requires PAID).
  4. `findAvailableSlot(vehicleType) → Slot?` — helper/query (used by `entry`, also useful for display boards).

For **Stage 1 MVP**, we'll keep it simpler: assume 1 vehicle type, flat fee, no admin. Just `entry` and `exit` (with fee computed on exit).

---

## Pillar 2 — ENTITIES (Stage 1: MVP)

### Q1.2: For the simplest version (1 floor, 1 vehicle type, flat hourly fee), what are the entities you'd model, and which one holds the *state* (occupied/free)?

**Hint:** Think T1 (nouns stated) + T2 (the container that has capacity) + T3 (the transaction that has a timestamp). Don't add vehicle types or floors yet — that's Stage 2.

**Your answer:**
_(fill in)_
T1 - Vehicle, ParkingLot, Ticket, Payment
T2 - Slots
T3 - Ticket (with entryTime, exitTime, paidStatus)

**Feedback:**

Solid — you picked the right container (`Slot`) and the right transaction (`Ticket`). Two small corrections:

1. **`Payment` is not a T1 noun** — it's a *terminal* entity produced when the ticket is paid. For Stage 1 MVP we can even skip it and just store `fee` + `paidAt` on the Ticket. Split it out only when payments get complex (multiple methods, refunds).
2. **`Ticket` shouldn't hold `paidStatus` as a boolean.** It should hold a `TicketStatus` enum (`ISSUED`, `PAID`, `EXITED`) — this becomes the state machine in Pillar 3. Boolean flags don't extend when you add `LOST` later.

**Ideal answer (Stage 1 — MVP):**

| Tier | Entity | Role | Key fields |
|---|---|---|---|
| T1 | `Vehicle` | actor's asset | `licensePlate` |
| T1 | `ParkingLot` | root aggregate | `slots: List<Slot>` |
| **T2** | `Slot` | **container (has state)** | `slotNum`, `status: SlotStatus {FREE, OCCUPIED}` |
| **T3** | `Ticket` | **transaction (has timestamp)** | `ticketId`, `licensePlate`, `slotNum`, `entryTime`, `exitTime?`, `fee?`, `status: TicketStatus` |

**Key insight:** `Slot.status` and `Ticket.status` are *two coupled state machines*. When `Ticket → EXITED`, `Slot → FREE` (side effect). Remember this — it's the crux of Pillar 3.

**Relationships (cardinality):**
- `ParkingLot → Slot` = 1-N (composition)
- `Slot ↔ Ticket` = **1-1 active** (one active ticket per slot at a time), **1-N historical** (over time a slot has many tickets)
- `Vehicle ↔ Ticket` = 1-1 active, 1-N historical

---

## Pillar 3 — LIFECYCLE (Stage 1)

### Q1.3: For the `Ticket` entity, list its states and the transitions between them. Also — which state is *terminal* (no exit edge)?

**Hint:** Think about the ticket's journey from the moment the driver enters until they drive away. Keep it minimal for Stage 1 — 3 states is enough.

**Your answer:**
_(fill in)_

States - Tickets - ISSUED → PAID → EXITED
Transition - 
ISSUED -> [payment success] → PAID
PAID -> [barrier opens] → EXITED


**Feedback:**

Correct for the happy path — 3 states, linear chain, `EXITED` is terminal. Two small refinements:

1. **Trigger on `ISSUED → PAID` is not just "payment success"** — it's `pay(ticketId)` op which *also* computes the fee (fee = `now - entryTime` × rate). So the trigger is the pay call; success/fail is the outcome. Say it as `ISSUED →[pay()]→ PAID`.
2. **Missing side-effect callout.** On `PAID → EXITED`, the *side effect* is `Slot.status → FREE`. In interviews, always state side-effects next to the transition — it shows you understand coupled state machines.

**Ideal answer (Stage 1):**

- **States:** `ISSUED`, `PAID`, `EXITED`
- **Initial:** `ISSUED` (created by `entry()`)
- **Terminal:** `EXITED` (no outgoing edge)
- **Transitions:**
  - `ISSUED →[pay(ticketId)]→ PAID` — compute `fee = duration × rate`, set `exitTime`
  - `PAID →[exit(ticketId)]→ EXITED` — **side effect:** `Slot.status = FREE`
- **Guards:**
  - `pay`: only if `status == ISSUED`
  - `exit`: only if `status == PAID`
- **Not modeled yet (Stage 2+):** `LOST` state, `CANCELLED`, time-based auto-expiry.

**Coupled state machine to remember:**
```
Ticket:  ISSUED ─pay()→ PAID ─exit()→ EXITED
                                        │
                                        ▼ (side effect)
Slot:    OCCUPIED ────────────────────→ FREE
```

---

## 🎯 Stage 1 — TIME TO CODE

You now have enough to build the MVP. Requirements recap:

- 1 `ParkingLot` with N `Slot`s (flat list, no floors yet).
- 1 vehicle type, flat hourly rate (e.g., ₹20/hour).
- Ops: `entry(licensePlate) → Ticket`, `pay(ticketId) → Ticket`, `exit(ticketId) → void`.
- In-memory storage, single-threaded, no concurrency, no edge cases beyond "lot full" and "invalid ticket".

### Coding task

Create these files under `tutor_sessions/parking_lot/stage1/`:

```
stage1/
  model/
    SlotStatus.java       (enum: FREE, OCCUPIED)
    TicketStatus.java     (enum: ISSUED, PAID, EXITED)
    Slot.java             (slotNum, status)
    Ticket.java           (ticketId, licensePlate, slotNum, entryTime, exitTime, fee, status)
  service/
    ParkingLotService.java   (entry / pay / exit / findFreeSlot)
  ParkingLotDemo.java     (main: create lot with 5 slots, run entry→pay→exit for 2 vehicles)
```

**Rules for Stage 1:**
- No design patterns yet (no Strategy, no Factory). Just plain classes.
- Flat rate hardcoded as a constant.
- Throw `IllegalStateException` for bad transitions, `IllegalArgumentException` for unknown ticketId.
- Use `Instant` for timestamps, `Duration` for fee calc.

Once you code it, paste the file paths here (or say "done") and I'll review — then we jump to **Stage 2** where we add vehicle types + floors + the state machine gets tested by new edge cases.

---

# 🚀 STAGE 2 — Real Domain: Multiple Vehicle Types + Multi-Floor + Richer Lifecycle

**What changes from Stage 1:**

The interviewer just added complexity. New requirements layered on top of MVP:

1. **Multiple vehicle types:** `CAR`, `BIKE`, `TRUCK` (and easy to add more later — e.g., `EV`).
2. **Multiple slot types:** `CAR_SLOT`, `BIKE_SLOT`, `TRUCK_SLOT` — a vehicle can only park in a compatible slot.
   - Simple rule for now: `CAR → CAR_SLOT`, `BIKE → BIKE_SLOT`, `TRUCK → TRUCK_SLOT` (strict 1-1 mapping).
   - (In interviews they often extend to "bike can fit in car slot" — we'll handle that as a *what-if* later.)
3. **Multi-floor lot:** `ParkingLot → Floor → Slot` (composition tree). Slots are organized per floor, not a flat list.
4. **Per-type hourly rate:** different rate per vehicle type (e.g., BIKE ₹10/hr, CAR ₹20/hr, TRUCK ₹40/hr). Still flat hourly — no surge/tiered yet (that's Stage 3).
5. **Richer ticket lifecycle:** add `LOST` state — driver lost the ticket at exit; pay penalty flat fee to exit.

**What we're NOT adding yet (deferred):**
- ❌ Pluggable pricing strategy (Stage 3)
- ❌ Pluggable slot allocator (Stage 3)
- ❌ Concurrency / thread safety (Stage 4)
- ❌ Payment as separate entity, refunds (Stage 5)

**Patterns you'll likely reach for in Stage 2:**
- **Enum** for `VehicleType`, `SlotType` (obvious)
- **Composition** for `ParkingLot → Floor → Slot`
- Maybe a **compatibility map** (`Map<VehicleType, SlotType>`) — resist over-engineering with Factory here; save it for when it *actually* pays off.

---

## Pillar 1 — SCOPE (Stage 2 extension)

### Q2.1: Do any of your Stage-1 operations change *signatures* now that we have vehicle types and floors? Which ops stay the same, which ones need new params, and are any new ops needed?

**Hint:** Look at each Stage-1 op — `entry`, `pay`, `exit`, `findFreeSlot` — and ask: "does the caller need to give me new info?" Also think: do we need an admin op to configure per-type rates, or is it fine as a config passed at construction?

**Your answer:**
_(fill in)_
 - entry/exit/findFreeSlot/ needs new param Vehicle Type
 - price rate can be stored in Hashmap according to Vehicle Types
 - LOST fee can have flat rates which can be used for calculation


**Feedback:**
_(after you answer)_
