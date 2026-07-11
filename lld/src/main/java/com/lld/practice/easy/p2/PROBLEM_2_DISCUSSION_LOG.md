# LLD Problem 2: Parking Lot (Single Floor) — Interview Discussion Log

**Date Started:** June 16, 2026
**Mode:** Strict interviewer simulation
**Problem:** Design a parking lot that parks vehicles by slot type and supports entry/exit with fee calculation.

---

## Round 1: Initial Clarifying Questions (User asked)

1. How many entry/exit will be there?
2. What is slot type and how many different kinds of slot will be there?
3. What kind of vehicles are we supporting?

### Interviewer answers
1. **Entry/exit gates:** Single entry, single exit (single-floor lot).
2. **Slot types:** 3 types — `SMALL`, `MEDIUM`, `LARGE`. Capacity fixed at lot setup time.
3. **Vehicle types:** 3 types — `BIKE`, `CAR`, `TRUCK`.
   - `BIKE` fits in `SMALL`, `MEDIUM`, or `LARGE`
   - `CAR` fits in `MEDIUM` or `LARGE`
   - `TRUCK` fits only in `LARGE`

---

## Round 2: Follow-up Clarifying Questions (User asked)

1. Lost ticket fee?
2. How will payment be associated? (Fee model)
3. Will the charge be hourly?
4. How will pricing be done?

### Interviewer answers
1. **Lost ticket fee:** Flat penalty of ₹500 added on top of normal fee.
2. **Payment association:** Payment processed at exit. One payment per ticket. Modes: `CASH`, `CARD`, `UPI`. Gateway integration out of scope; assume `PaymentProcessor.charge(...)` returns success/failure.
3. **Charge model:** Hourly, rounded up to next full hour (e.g., 1h 5min → 2h).
4. **Pricing:** Per slot type:
   - `SMALL`: ₹20/hour
   - `MEDIUM`: ₹40/hour
   - `LARGE`: ₹80/hour
   - Pricing fixed at setup; no surge, no daily cap.

---

## Round 3: Interviewer Nudge — Categories Missed

User did not know what they were missing. Interviewer provided 10 categories without giving direct answers:

1. Capacity / setup
2. Allocation policy
3. Ticket lifecycle
4. Edge cases on entry
5. Edge cases on exit
6. Time / fee calculation
7. Data input
8. Concurrency
9. Persistence / scope
10. Output contract

User then asked for direct answers across all 10 categories.

---

## Round 4: Locked Specs (Interviewer-provided, used as ground truth)

### 1. Capacity / setup
- Configured at construction: `ParkingLot(name, smallCount, mediumCount, largeCount)`.
- Slot counts fixed; no resize at runtime.

### 2. Allocation policy
- **Best-fit-then-first-available**: pick smallest slot type that fits the vehicle; within that type, pick lowest slot ID.
- Example: `CAR` → try `MEDIUM` first; fall back to `LARGE` if none free.

### 3. Ticket lifecycle
- States: `ACTIVE`, `COMPLETED`, `LOST`.
- Created on entry → `COMPLETED` on paid exit → `LOST` if user reports lost.
- Tickets are not reusable. No cancellation without exit.

### 4. Edge cases on entry
- Same vehicle number already parked → reject with `VEHICLE_ALREADY_PARKED`.
- No slot fits → reject with `LOT_FULL`.
- Null/blank number, unknown type → reject with `INVALID_INPUT`.

### 5. Edge cases on exit
- Invalid ticket → reject with `TICKET_NOT_FOUND`.
- Already-completed ticket → reject with `TICKET_ALREADY_USED`.
- Payment failure → ticket stays `ACTIVE`, slot stays occupied, return `PAYMENT_FAILED`.

### 6. Time / fee calculation
- `Instant` used for entry and exit times.
- **Minimum charge = 1 hour** even for stays < 1 hour.
- Round up partial hours.
- All times in UTC; no timezone handling.

### 7. Data input
- `vehicleNumber` is the unique identifier.
- Case-insensitive, normalized to UPPERCASE on entry.
- Format validation out of scope.

### 8. Concurrency
- Multiple concurrent entries/exits expected.
- Must prevent two vehicles claiming the same slot.
- Multiple exits on different tickets must run in parallel safely.

### 9. Persistence / scope
- In-memory only, single JVM.
- No DB, no restart recovery.

### 10. Output contract
- Success returns result object (e.g., `Ticket`, `ExitReceipt`).
- Failure throws structured exception with `StatusCode` + `message` (same pattern as P1).

---

## Pending User Deliverables (Resume from here)

1. **Numbered, explicit assumptions list** (minimum 7 items, each unambiguous and testable).
2. **Class design** (entities, value objects, services).
3. **Storage / data structure choices** (with justification for concurrency).
4. **API method signatures** (input → output, exceptions).
5. **Business logic in pseudocode** for entry, exit, lost ticket flows.
6. **Design pattern call-outs** (which patterns + why).

---

## Interviewer Notes / Open Threads

- User must explicitly state slot allocation data structure and justify under concurrency.
- User must address fee rounding (`Duration.toHours()` vs ceil) explicitly in pseudocode.
- User must justify whether lost ticket flow needs different fee calculation rules.
- User must call out at least 1–2 design patterns (likely: Strategy for fee/allocation, Singleton for lot if applicable, Factory for vehicle/slot creation).

---

## Next Step (Tomorrow / Next Session)

Start with the assumptions list (deliverable #1) before any code or class diagram.

