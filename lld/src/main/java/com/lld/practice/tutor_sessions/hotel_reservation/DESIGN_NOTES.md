# Hotel Booking System - Design Notes

**Date:** July 16, 2026
**Interview Phase:** Step 1 - Design in Progress

---

## ✅ STEP 0: Assumptions (LOCKED)

1. Single hotel with 100-500 rooms (no multi-hotel support)
2. In-memory storage (no database persistence)
3. Single-threaded for v1 (concurrency handled in Step 2)
4. Payment NOT in scope
5. Room initialization is fixed at startup (Admin APIs are v2)

---

## ✅ ACTORS (LOCKED)

### V1 (In Scope):
| Actor | Type | Responsibilities |
|-------|------|-----------------|
| **Guest** | Primary | Search rooms, make reservation, view booking, cancel reservation |
| **Front Desk / Receptionist** | Primary | Check-in guest, check-out guest |
| **Notification System** | System | Track notification events (reservation confirmed, cancelled) — no actual sending in v1 |

### V2 (Deferred):
| Actor | Reason Deferred |
|-------|----------------|
| Admin | Room management (add/delete/modify rooms) |
| Housekeeping System | Out of scope |
| Email/SMS Provider | Actual notification sending deferred |

---

## ✅ ENTITIES (LOCKED)

| Entity | Key Fields | Notes |
|--------|-----------|-------|
| **Hotel** | hotelId, name, address, List\<Room\> | Root container for all rooms |
| **Room** | roomId, roomNumber, roomType, pricePerNight, RoomStatus | Has availability state |
| **Guest** | guestId, name, email, phone | Identity of the person booking |
| **Reservation** | reservationId, Guest, List\<Room\>, checkInDate, checkOutDate, actualCheckInTime, actualCheckOutTime, ReservationStatus | Core booking entity; holds full lifecycle |
| **Notification** | notificationId, guestId, type, message, timestamp | Tracks events (RESERVATION_CONFIRMED, CANCELLED) — no actual sending in v1 |

**Key decisions:**
- `Reservation` owns check-in/check-out timestamps (v1 simplicity)
- `Payment` deferred to v2
- `Stay` entity not needed — Reservation handles full lifecycle

---

## ✅ SERVICES (LOCKED)

### HotelBookingService
```java
List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, RoomType type)
Reservation reserve(Guest guest, List<String> roomIds, LocalDate checkIn, LocalDate checkOut)
Reservation cancelReservation(String reservationId)
Reservation modifyReservation(String reservationId, LocalDate newCheckIn, LocalDate newCheckOut)
Reservation viewReservation(String reservationId)
```

### CheckInService
```java
Reservation checkIn(String reservationId)
Reservation checkOut(String reservationId)
```

### NotificationService
```java
Notification trackNotification(Reservation reservation, NotificationType type)
```

---

## ✅ ENUMS / VALUE OBJECTS (LOCKED)

| Enum | Values |
|------|--------|
| **ReservationStatus** | RESERVED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW |
| **RoomStatus** | AVAILABLE, BOOKED, OCCUPIED, UNDER_MAINTENANCE |
| **RoomType** | DELUXE, SUPER_DELUXE, PREMIUM |
| **NotificationType** | RESERVED, MODIFIED, CANCELLED |

---

## ✅ API CONTRACTS (LOCKED)

```java
/**
 * Creates a reservation for the guest for requested rooms and dates.
 * @param guest       - guest making the reservation (must not be null)
 * @param roomIds     - list of room IDs to book (must not be null/empty)
 * @param checkIn     - check-in date (must not be null, must be future date)
 * @param checkOut    - check-out date (must not be null, must be after checkIn)
 * @return Reservation with status RESERVED
 * @throws IllegalArgumentException  if any input param is null/empty/invalid
 * @throws HotelBookingException     if any requested room is unavailable for given dates
 */
Reservation reserve(Guest guest, List<String> roomIds, LocalDate checkIn, LocalDate checkOut)

/**
 * Checks in a guest against an existing reservation.
 * @param reservationId - ID of the reservation (must not be null/empty)
 * @return Reservation with status updated to CHECKED_IN and actualCheckInTime set
 * @throws IllegalArgumentException  if reservationId is null/empty
 * @throws HotelBookingException     if reservation not found, or status is not RESERVED
 * Pre-condition: Reservation must be in RESERVED status
 */
Reservation checkIn(String reservationId)
```

---

## ✅ EXCEPTION STRATEGY (LOCKED)

| Scenario | Exception | Examples |
|----------|-----------|---------|
| **Input validation** | `IllegalArgumentException` | null guest, empty roomIds, null dates, null reservationId |
| **Business rule violation** | `HotelBookingException` | room not available, reservation not found, already checked-in, cancelling a checked-out reservation |

**Rule:**
- `IllegalArgumentException` → contract violation (bad inputs)
- `HotelBookingException` → business logic failure (valid inputs, invalid state)

---

## ✅ CONCURRENCY DESIGN (LOCKED)

### Problem Statement
Multiple guests simultaneously call `reserve()`, both see the same room as AVAILABLE, and both try to book it — only one should succeed.

---

### V1: Global ReentrantLock (Basic — Implemented)

**Applied to:** `searchAvailableRooms`, `reserve`, `cancelReservation`, `modifyReservation`, `viewReservation`

**What it achieves:**
- ✅ Prevents double-booking — only one thread executes any operation at a time
- ✅ Simple to implement and reason about
- ✅ No race conditions on `reservations` map or `room.status` mutations
- ✅ Guard checks inside lock → eliminates TOCTOU (Time-Of-Check-Time-Of-Use) race condition

**Trade-off:**
- ❌ Single global lock serializes ALL operations
- ❌ Thread A booking Room 101 blocks Thread B booking Room 202 — even though unrelated
- ❌ Poor throughput under high load (500 rooms, only 1 thread active at a time)

**Interview narration:**
> "Global ReentrantLock is correct for v1 — simple, safe, no race conditions.
> Trade-off: coarse-grained lock serializes all booking operations.
> Under high load this becomes a bottleneck. For v2 I'd use per-room locking."

---

### V2: Per-Room ReentrantLock (Advanced — Implemented)

**Data structure added:**
```java
// In HotelDataStore:
ConcurrentHashMap<String, ReentrantLock> roomLocks  // one lock per roomId
```

**reserve() flow — 7 steps:**
```
Step 1: searchAvailableRooms()        → snapshot read (uses global lock internally)
Step 2: pick candidate rooms          → subList of available rooms
Step 3: sort by roomId                → DEADLOCK PREVENTION (consistent order)
Step 4: computeIfAbsent + lock.lock() → atomic lock creation + per-room acquisition
Step 5: re-verify room.status==AVAIL  → DOUBLE-CHECK LOCKING PATTERN
Step 6: mark BOOKED + create Reservation + put in reservations map
Step 7: finally → unlock ALL acquired locks (always, even on exception)
```

**What it achieves:**
- ✅ Thread A booking Room 101 does NOT block Thread B booking Room 202
- ✅ True parallelism — N rooms = N independent lock domains
- ✅ Only conflict when two threads want the EXACT same room

**Deadlock Prevention — Why sorted acquisition works:**
```
WITHOUT sorting (DEADLOCK):
  Thread A: acquires lock(R1), waiting for lock(R2)
  Thread B: acquires lock(R2), waiting for lock(R1)
  → Circular wait → DEADLOCK

WITH sorting (SAFE):
  Thread A: wants [R2,R1] → sorts → [R1,R2] → acquires lock(R1) then lock(R2)
  Thread B: wants [R1,R2] → sorts → [R1,R2] → waits for lock(R1)
  → No circular wait → No deadlock
```

**Double-Check Locking — Why re-verify is critical:**
```
WITHOUT re-verify (BUG):
  Thread A: searches → R1 is AVAILABLE
  Thread B: searches → R1 is AVAILABLE
  Thread A: acquires lock(R1) → marks R1 BOOKED
  Thread B: acquires lock(R1) → also marks R1 BOOKED ← DOUBLE BOOKING BUG!

WITH re-verify (SAFE):
  Thread A: acquires lock(R1) → re-checks → AVAILABLE → books R1
  Thread B: acquires lock(R1) → re-checks → BOOKED → throws HotelBookingException ✅
```

**Trade-off:**
- ❌ More complex to implement
- ✅ `cancelReservation` and `modifyReservation` now use per-room locks (implemented in v2)
- ⚠️ `modifyReservation` releases old room locks BEFORE calling `reserve()` for new dates — intentional design to avoid holding two sets of room locks simultaneously

**Key design decision in `modifyReservation`:**
```
WRONG — hold old locks while acquiring new locks (risk of deadlock):
  lock(oldR1) + lock(oldR2) → call reserve() → lock(newR1) + lock(newR2)
  ← holding 4 locks simultaneously, circular wait possible

CORRECT — release old locks first, then acquire new locks:
  lock(oldR1) + lock(oldR2) → free old rooms → unlock(oldR1, oldR2)
  → call reserve() → lock(newR1) + lock(newR2)  ← only 2 locks at a time
```

**Interview narration:**
> "Per-room locking with sorted acquisition eliminates the global bottleneck.
> Threads booking different rooms run fully in parallel.
> Sorted lock order prevents deadlock — all threads acquire in same order, no circular wait.
> Double-check locking handles the window between search and lock acquisition.
> Trade-off: more complex; cancel/modify still use global lock — v3 would extend per-room locking there too."

---

### Comparison Table

| Aspect | V1 Global Lock | V2 Per-Room Lock |
|--------|---------------|-----------------|
| Correctness | ✅ Safe | ✅ Safe |
| Double-booking prevention | ✅ Yes | ✅ Yes |
| Deadlock risk | None (single lock) | Prevented by sorted acquisition |
| Throughput | Low (fully serialized) | High (parallel per room) |
| Complexity | Simple | Medium |
| Double-check needed | No | Yes — critical |
| Suitable for | v1 / interviews | v2 / production |


---

## 🎓 DESIGN PATTERN GUIDE — Identification & Application

### HOW TO IDENTIFY PATTERNS IN AN INTERVIEW (The 7 Lenses)

> Before coding, scan your system through each lens. If a lens "fits", that pattern applies.

| Lens (What you see in the system) | Pattern to use | Interview trigger phrase |
|----------------------------------|---------------|------------------------|
| **Multiple algorithms for same task** (search, pricing, sorting) | Strategy | "I want the algorithm to be pluggable / swappable at runtime" |
| **Object changes behaviour based on its state** (Reservation: RESERVED → CHECKED_IN → CANCELLED) | State | "Each state has different rules for what's allowed" |
| **One event should notify multiple listeners** (booking confirmed → email + SMS + push) | Observer | "When X happens, N things need to react" |
| **Creating objects of related types** (rooms: DELUXE, PREMIUM, SUITE) | Factory / Abstract Factory | "I want to decouple object creation from usage" |
| **Add features without changing existing class** (add logging to NotificationService) | Decorator | "Open/Closed principle — extend without modifying" |
| **Chain of checks / handlers in sequence** (validation: null check → business check → fraud check) | Chain of Responsibility | "Each handler decides to process or pass to next" |
| **Single shared global instance** (HotelDataStore, configuration) | Singleton | "There should only ever be ONE instance" |

---

### HOTEL RESERVATION SYSTEM — PATTERN ANALYSIS

#### Step 1: Scan through each lens

| Lens | What we see in Hotel Reservation | Pattern? |
|------|----------------------------------|---------|
| Multiple algorithms? | How rooms are selected (first available, cheapest, by type) | ✅ **Strategy** |
| State-based behaviour? | Reservation: RESERVED→CHECKED_IN→CHECKED_OUT / CANCELLED | ✅ **State** |
| One-to-many notification? | Booking confirmed → Email, SMS, Push all need to be notified | ✅ **Observer** |
| Creating related objects? | Room types: DELUXE, SUPER_DELUXE, PREMIUM | ✅ **Factory** (v3) |
| Add features without change? | Add retry / logging to NotificationService | ✅ **Decorator** (v3) |
| Chain of checks? | reserve() validates: null → dates → availability → capacity | ✅ **Chain of Responsibility** (v3) |
| Single shared instance? | HotelDataStore shared across all services | ✅ **Singleton** (already partial) |

#### Step 2: Priority for interview (45-min window)

**Implement NOW (v2 — most impactful for Staff level):**

| # | Pattern | Why it's important here |
|---|---------|------------------------|
| 1 | **Observer** | NotificationService is currently tightly coupled — hardcoded in every service method. Observer decouples it properly |
| 2 | **State** | Reservation lifecycle has invalid transitions (can't cancel a CHECKED_OUT reservation). State pattern enforces this |
| 3 | **Strategy** | Room selection is hardcoded (`ceil(guests/2)` picks first N). Strategy makes it pluggable |

**Defer to v3 (mention, don't implement):**
- Factory: room type creation
- Decorator: notification retry/logging
- Chain of Responsibility: validation chain

---

### PATTERN 1: Observer — NotificationService Decoupling

#### Problem (current code):
```java
// In HotelReservationServiceImpl.reserve():
notificationService.trackNotification(reservation, NotificationTypeStatus.EMAIL, "Reservation Successful");

// In cancelReservation():
notificationService.trackNotification(reservation, NotificationTypeStatus.EMAIL, "Reservation Cancelled");

// In CheckInServiceImpl.checkIn():
notificationService.trackNotification(reservation, NotificationTypeStatus.EMAIL, "Checked In");
```
**Issue:** Every service class is directly calling NotificationService. Adding SMS/Push requires changing EVERY service class. Violates Open/Closed Principle.

#### Solution (Observer):
```
ReservationEventPublisher  ←── publishes events
        │
        ├── EmailNotificationObserver  → sends email
        ├── SmsNotificationObserver    → sends SMS  (v3)
        └── PushNotificationObserver   → sends push (v3)
```

#### Interview narration:
> "Currently notification is tightly coupled — every service directly calls NotificationService.
> If I add SMS, I'd have to touch 5 files. Using Observer pattern, I publish a ReservationEvent,
> and any observer (Email, SMS, Push) subscribes independently.
> Adding a new channel = add one Observer class, zero changes to existing services."

#### What to implement:
```java
// 1. ReservationEvent (the event object)
// 2. ReservationObserver (interface: void onEvent(ReservationEvent))
// 3. ReservationEventPublisher (holds List<ReservationObserver>, publishes to all)
// 4. EmailNotificationObserver implements ReservationObserver
// 5. Inject publisher into services, replace direct trackNotification calls
```

---

### PATTERN 2: State — Reservation Lifecycle Enforcement

#### Problem (current code):
```java
// Currently no guard on invalid transitions:
reservation.setReservationStatus(ReservationStatus.CANCELLED); // works even if CHECKED_OUT!
reservation.setReservationStatus(ReservationStatus.CHECKED_IN); // works even if already CHECKED_IN!
```
**Issue:** Any code can set any status. Invalid transitions like CHECKED_OUT → CANCELLED are allowed.

#### Solution (State):
```
ReservationState (interface)
    ├── ReservedState     → allows: checkIn(), cancel(), modify()   | blocks: checkOut()
    ├── CheckedInState    → allows: checkOut()                      | blocks: cancel(), checkIn()
    ├── CheckedOutState   → allows: nothing (terminal)              | blocks: everything
    └── CancelledState    → allows: nothing (terminal)              | blocks: everything
```

#### Valid transitions:
```
RESERVED ──────→ CHECKED_IN ──→ CHECKED_OUT (terminal)
    │
    └──────────→ CANCELLED (terminal)
```

#### Interview narration:
> "Currently any code can set any reservation status — there's no enforcement of valid transitions.
> State pattern gives each status its own class with explicit allowed operations.
> Trying to cancel a checked-out reservation throws a domain exception immediately.
> Interviewers love this because it shows you think about invariants, not just happy path."

#### What to implement:
```java
// 1. ReservationState (interface: checkIn(), checkOut(), cancel(), modify())
// 2. ReservedState, CheckedInState, CheckedOutState, CancelledState (4 classes)
// 3. Reservation holds currentState, delegates to it
// 4. Each state throws HotelBookingException for invalid transitions
```

---

### PATTERN 3: Strategy — Pluggable Room Selection

#### Problem (current code):
```java
// Hardcoded in reserve():
int numberOfRooms = (int) Math.ceil(guest.size() / 2.0);
List<Room> bookedRoom = availableRooms.subList(0, numberOfRooms);
```
**Issue:** Room selection is hardcoded. Can't switch to "cheapest room" or "by room type" without changing service logic.

#### Solution (Strategy):
```
RoomSelectionStrategy (interface: List<Room> select(List<Room> available, List<Guest> guests))
    ├── DefaultSelectionStrategy    → ceil(guests/2) rooms, first available
    ├── CheapestRoomStrategy        → sort by pricePerNight, pick cheapest
    └── RoomTypePreferenceStrategy  → pick rooms matching requested type
```

#### Interview narration:
> "Currently room selection is hardcoded — always picks first N available rooms.
> Strategy pattern makes the algorithm a first-class citizen.
> We can inject CheapestRoomStrategy or RoomTypePreferenceStrategy at runtime.
> Trade-off: adds abstraction — justified when selection logic is likely to vary."

#### What to implement:
```java
// 1. RoomSelectionStrategy (interface: List<Room> select(List<Room>, List<Guest>))
// 2. DefaultSelectionStrategy (current logic extracted)
// 3. CheapestRoomStrategy (sort by price, take N)
// 4. Inject strategy into HotelReservationServiceImpl constructor
```

---

### IMPLEMENTATION ORDER (follow this sequence)

```
Step 1: Observer Pattern   → ✅ IMPLEMENTED
Step 2: State Pattern      → you implement → I review  
Step 3: Strategy Pattern   → you implement → I review
```

---

## ✅ PATTERN 1 IMPLEMENTED: Observer — Complete Implementation Log

### Files Created:
```
observer/
    ReservationEventType.java        → enum: RESERVED, CANCELLED, MODIFIED, CHECKED_IN, CHECKED_OUT
    ReservationEvent.java            → immutable event object (Reservation + EventType + timestamp)
    ReservationObserver.java         → interface: void onEvent(ReservationEvent event)
    ReservationEventPublisher.java   → subscribe / unsubscribe / publish to all observers
    impl/
        EmailNotificationObserver.java → prints email message per event type (simulates sending)
```

### Wired Into:
| Service | Events Published |
|---------|-----------------|
| `HotelReservationServiceImpl.reserve()` | `RESERVED` |
| `HotelReservationServiceImpl.cancelReservation()` | `CANCELLED` |
| `HotelReservationServiceImpl.modifyReservation()` | `MODIFIED` |
| `CheckInServiceImpl.checkIn()` | `CHECKED_IN` |
| `CheckInServiceImpl.checkOut()` | `CHECKED_OUT` |

### Key Design Decisions:
- `ReservationEvent` is **immutable** — `occurredAt` auto-captured in constructor, no setters
- `ReservationEventPublisher` holds `List<ReservationObserver>` — no dependency on specific observer
- Services receive `ReservationEventPublisher` via **constructor injection** — not created internally
- `NotificationService` and `NotificationTypeStatus` fully **removed** from both service implementations

### What Changed vs Before:
```
BEFORE (tightly coupled):
  HotelReservationServiceImpl → directly calls NotificationService
  CheckInServiceImpl → directly calls NotificationService
  Adding SMS = modify 5 files ❌

AFTER (Observer pattern):
  HotelReservationServiceImpl → publishes ReservationEvent
  CheckInServiceImpl → publishes ReservationEvent
  Adding SMS = create SmsNotificationObserver + register it = 1 new file ✅
```

### Interview Narration (memorize this):
> "Previously every service directly called NotificationService — adding SMS would require
> changing 5 files. With Observer, I publish one event and any number of subscribers react
> independently. Adding SMS = create SmsNotificationObserver, register it —
> zero changes to existing services. This follows the Open/Closed Principle."

### Business Rules Added During Implementation:
- `checkIn()` — guards that reservation must be in `RESERVED` state before allowing check-in
- `checkOut()` — guards that reservation must be in `CHECKED_IN` state before allowing check-out
- Both throw `HotelBookingException` with current status in the message for debuggability

---

### DESIGN PATTERN INTERVIEW CHECKLIST

Before starting to code a pattern, always state:

```
1. PROBLEM:    "Currently the code does X, which causes Y problem"
2. PATTERN:    "I'll use [Pattern Name] to solve this"  
3. STRUCTURE:  "The structure will be: Interface + ConcreteA + ConcreteB + ..."
4. WIRING:     "Service X will use it by ..."
5. TRADE-OFF:  "This adds complexity, justified because ..."
```

**Never jump to code without stating the problem first. Interviewers want to see your reasoning.

---

## ✅ PATTERN 2 IMPLEMENTED: State — Complete Implementation Log

### Files Created:
```
state/
    ReservationState.java            → interface: checkIn(), checkOut(), cancel(), modify(newCheckIn, newCheckOut)
    impl/
        ReservedState.java           → allows checkIn(), cancel(), modify() | blocks checkOut()
        CheckedInState.java          → allows checkOut() only               | blocks everything else
        CheckedOutState.java         → terminal — blocks ALL operations
        CancelledState.java          → terminal — blocks ALL operations
```

### Wired Into:
| Class | How State Is Used |
|-------|------------------|
| `Reservation.java` | Holds `ReservationState currentState` field; constructed with `new ReservedState()` |
| `HotelReservationServiceImpl.cancelReservation()` | Calls `reservation.getReservationState().cancel(reservation)` as guard BEFORE room lock acquisition |
| `HotelReservationServiceImpl.modifyReservation()` | Calls `oldReservation.getReservationState().modify(reservation, newCheckIn, newCheckOut)` as guard BEFORE room lock acquisition |
| `CheckInServiceImpl.checkIn()` | Calls `reservation.getReservationState().checkIn(reservation)` — delegates transition entirely to state |
| `CheckInServiceImpl.checkOut()` | Calls `reservation.getReservationState().checkOut(reservation)` — delegates transition entirely to state |

### State Transition Table:
| Current State | checkIn() | checkOut() | cancel() | modify() |
|--------------|-----------|------------|----------|----------|
| **RESERVED** | ✅ → CHECKED_IN | ❌ throws | ✅ → CANCELLED | ✅ (dates updated) |
| **CHECKED_IN** | ❌ throws | ✅ → CHECKED_OUT | ❌ throws | ❌ throws |
| **CHECKED_OUT** | ❌ throws | ❌ throws | ❌ throws | ❌ throws |
| **CANCELLED** | ❌ throws | ❌ throws | ❌ throws | ❌ throws |

### Key Design Decisions:
- Each state class is **responsible for transitioning itself**: `reservation.setReservationStatus(...)` + `reservation.setReservationState(new NextState())` both called inside the state
- Terminal states (`CheckedOutState`, `CancelledState`) throw `HotelBookingException` for ALL operations — no "if" checks scattered across service layer
- `Reservation` constructor takes `ReservationState` — always initialized with `new ReservedState()`
- Guard calls happen **BEFORE** acquiring room locks — fast-fail, no wasted lock acquisition on invalid transitions

### What Changed vs Before:
```
BEFORE (scattered if-checks in service layer):
  CheckInServiceImpl.checkIn() → if (status != RESERVED) throw HotelBookingException
  CheckInServiceImpl.checkOut() → if (status != CHECKED_IN) throw HotelBookingException
  cancelReservation() → no guard, CHECKED_OUT reservation could be cancelled!

AFTER (State pattern):
  CheckInServiceImpl.checkIn() → reservation.getReservationState().checkIn(reservation)
  CheckInServiceImpl.checkOut() → reservation.getReservationState().checkOut(reservation)
  cancelReservation() → reservation.getReservationState().cancel(reservation)
  Adding new state = add one class, zero changes to service layer ✅
```

### Interview Narration (memorize this):
> "Previously transition guards were scattered as if-checks across every service method.
> If I add a NO_SHOW state, I'd have to find and update every relevant check.
> State pattern encapsulates all rules per state in one class.
> ReservedState knows what it allows; CheckedOutState throws for everything — no hunting for guards.
> This follows Single Responsibility: each state class owns its own invariants."

---

## ✅ PATTERN 3 IMPLEMENTED: Strategy — Complete Implementation Log

### Files Created:
```
strategy/
    RoomSelectionStrategy.java       → interface: List<Room> selectRooms(List<Room> available, List<Guest> guests)
    impl/
        DefaultSelectionStrategy.java  → ceil(guests/2) rooms, first N from available list
        CheapestRoomStrategy.java      → sort available rooms by pricePerNight ASC, pick cheapest N
```

### Wired Into:
| Class | How Strategy Is Used |
|-------|---------------------|
| `HotelReservationServiceImpl` | Constructor receives `RoomSelectionStrategy roomSelectionStrategy` (injected) |
| `HotelReservationServiceImpl.reserve()` | Calls `roomSelectionStrategy.selectRooms(availableRooms, guests)` in Step 2 — replaces hardcoded logic |
| `HotelConcurrencyTestDriver.buildBookingService()` | Injects `new DefaultSelectionStrategy()` when constructing service |

### Strategy Comparison:
| Strategy | Algorithm | When to Use |
|----------|-----------|-------------|
| `DefaultSelectionStrategy` | `ceil(guests/2)` rooms, first available | Default — no preference from guest |
| `CheapestRoomStrategy` | Sort by `pricePerNight` ASC, pick N cheapest | Budget-conscious guests |
| `RoomTypePreferenceStrategy` *(v3)* | Filter by requested `RoomType`, then pick N | Guest specifies room type preference |

### Key Design Decisions:
- `selectRooms()` returns a **mutable `ArrayList`** (not `subList` or `List.of`) — required because `reserve()` sorts the result for deadlock prevention
- Strategy is injected at **construction time** — swappable without touching any service logic
- `HotelReservationServiceImpl` has zero knowledge of which strategy is active — pure polymorphism
- Strategy throws `HotelBookingException` if `availableRooms` is empty or fewer rooms available than needed

### What Changed vs Before:
```
BEFORE (hardcoded in reserve()):
  int numberOfRooms = (int) Math.ceil(guest.size() / 2.0);
  List<Room> candidateRooms = new ArrayList<>(availableRooms.subList(0, numberOfRooms));
  // Can't change algorithm without editing service class

AFTER (Strategy pattern):
  List<Room> candidateRooms = roomSelectionStrategy.selectRooms(availableRooms, guest);
  // Swap to CheapestRoomStrategy at construction → zero changes to service class ✅
```

### Swapping Strategy at Runtime (Driver example):
```java
// Use default (first available):
HotelReservationService service = new HotelReservationServiceImpl(store, publisher, new DefaultSelectionStrategy());

// OR use cheapest — zero changes to HotelReservationServiceImpl:
HotelReservationService service = new HotelReservationServiceImpl(store, publisher, new CheapestRoomStrategy());
```

### Interview Narration (memorize this):
> "Previously room selection was hardcoded — always picks first N available rooms.
> If a guest wants the cheapest room or a specific type, I'd have to add if-branches inside reserve().
> Strategy pattern extracts the algorithm into its own class.
> I inject the strategy — service has no idea which algorithm runs.
> Adding 'prefer premium rooms' = one new class, zero service changes.
> Trade-off: adds abstraction, justified because room selection policy varies by business need."

---

## 📋 DESIGN PATTERNS — FINAL IMPLEMENTATION SUMMARY

| Pattern | Status | Files | Problem Solved |
|---------|--------|-------|---------------|
| **Observer** | ✅ Done | `observer/` (5 files) | Decoupled notification — add channels without touching services |
| **State** | ✅ Done | `state/` (5 files) | Enforced reservation lifecycle — invalid transitions throw immediately |
| **Strategy** | ✅ Done | `strategy/` (3 files) | Pluggable room selection — swap algorithm at construction, zero service changes |
| **Factory** | 🔲 v3 | — | Room type creation decoupled from service |
| **Decorator** | 🔲 v3 | — | Add retry/logging to notification without modifying observer classes |
| **Chain of Responsibility** | 🔲 v3 | — | Validation pipeline: null → dates → availability → capacity |


