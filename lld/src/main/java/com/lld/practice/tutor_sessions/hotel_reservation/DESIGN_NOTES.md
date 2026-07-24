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

