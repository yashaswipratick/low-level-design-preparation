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

