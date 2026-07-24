# Design Patterns — Beginner Teaching Guide
# Applied to: Hotel Reservation System

**Date:** July 24, 2026
**Goal:** Learn to identify + implement the 3 most common LLD interview patterns

---

## 🧠 WHAT IS A DESIGN PATTERN? (Simple explanation)

A design pattern is a **named solution to a recurring problem** in software design.

Think of it like cooking recipes:
- You don't invent a new way to make pasta every time.
- You follow a recipe that works.
- The recipe has a name ("Carbonara") and everyone understands what you mean.

Same in code — when you say "I'll use Observer pattern here", every engineer immediately knows the structure.

---

## 🎯 THE ONE QUESTION THAT IDENTIFIES EVERY PATTERN

Before learning all patterns, learn this **one diagnostic question** for each:

| When you see this in your system... | Ask yourself... | Pattern |
|-------------------------------------|----------------|---------|
| "Multiple ways to do the same thing" | "Can I swap the algorithm without changing the caller?" | **Strategy** |
| "Object behaves differently in different states" | "Does each state need its own rules?" | **State** |
| "One thing happens → many things need to react" | "Should the sender know who's listening?" | **Observer** |
| "Creating objects of different types" | "Is the caller too coupled to the concrete class?" | **Factory** |
| "I want to add features without editing the class" | "Can I wrap it instead of changing it?" | **Decorator** |

---

## PATTERN 1: OBSERVER — "The Newsletter Subscription"

### 🌍 Real-World Analogy

Think of a **YouTube channel**:
- YouTube doesn't know who all the subscribers are
- When a video is uploaded (event), YouTube just notifies all subscribers
- New subscriber joins → no change to YouTube's upload logic
- Subscriber leaves → no change to YouTube's upload logic

**The YouTube channel = Publisher (Subject)**
**Subscribers = Observers**
**New video = Event**

---

### 🏨 How it appears in Hotel Reservation

**Current situation (WRONG — tightly coupled):**
```
Guest books room
    → HotelReservationService directly calls NotificationService.sendEmail()
    → HotelReservationService directly calls NotificationService.sendSMS()  ← you'd add this here
    → HotelReservationService directly calls NotificationService.sendPush() ← and here too
```

**Problem:** Every new notification channel = edit HotelReservationService. 
Violates Open/Closed Principle ("open for extension, closed for modification").

**After Observer pattern (CORRECT — loosely coupled):**
```
Guest books room
    → HotelReservationService publishes ONE event: "Reservation Created"
         ↓
    ReservationEventPublisher notifies all registered observers:
         ├── EmailObserver.onEvent() → sends email
         ├── SmsObserver.onEvent()   → sends SMS   (add without touching service)
         └── PushObserver.onEvent()  → sends push  (add without touching service)
```

---

### 📐 Structure (always the same 4 parts)

```
┌─────────────────────────────────────────────────────────────┐
│  OBSERVER PATTERN — 4 Parts                                 │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1. EVENT object         → what happened (the data)        │
│     ReservationEvent                                        │
│                                                             │
│  2. OBSERVER interface   → who can listen                  │
│     ReservationObserver  → void onEvent(ReservationEvent)  │
│                                                             │
│  3. PUBLISHER            → manages observers, fires events │
│     ReservationEventPublisher                               │
│     - subscribe(observer)                                   │
│     - unsubscribe(observer)                                 │
│     - publish(event) → loops through all observers         │
│                                                             │
│  4. CONCRETE OBSERVERS   → actual handlers                 │
│     EmailNotificationObserver implements ReservationObserver│
│     SmsNotificationObserver   implements ReservationObserver│
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

### 🔄 Before vs After — What changes in services

**BEFORE (current code):**
```java
// In HotelReservationServiceImpl:
public HotelReservationServiceImpl(HotelDataStore dataStore, NotificationService notificationService) {
    this.notificationService = notificationService;  // ← direct dependency
}

public Reservation reserve(...) {
    // ... booking logic ...
    notificationService.trackNotification(reservation, EMAIL, "Reservation Successful"); // ← tight coupling
    return reservation;
}
```

**AFTER (with Observer):**
```java
// In HotelReservationServiceImpl:
public HotelReservationServiceImpl(HotelDataStore dataStore, ReservationEventPublisher publisher) {
    this.publisher = publisher;  // ← only knows about publisher, not email/sms/push
}

public Reservation reserve(...) {
    // ... booking logic ...
    publisher.publish(new ReservationEvent(reservation, ReservationEventType.RESERVED)); // ← just publish
    return reservation;
    // Who handles email, SMS, push? → the observers. Service doesn't know and doesn't care.
}
```

---

### 📦 Files you will create

```
hotel_reservation/
    observer/
        ReservationEvent.java            ← the event data object
        ReservationEventType.java        ← enum: RESERVED, CANCELLED, MODIFIED, CHECKED_IN, CHECKED_OUT
        ReservationObserver.java         ← interface with onEvent(ReservationEvent)
        ReservationEventPublisher.java   ← holds List<ReservationObserver>, has subscribe/publish
        impl/
            EmailNotificationObserver.java  ← implements ReservationObserver, prints/logs email
```

---

### 🧩 Step-by-Step Implementation Plan

We build it in this exact order (each step is small and clear):

```
Step 1: Create ReservationEventType enum      (2 min)
Step 2: Create ReservationEvent class         (3 min)
Step 3: Create ReservationObserver interface  (1 min)
Step 4: Create ReservationEventPublisher      (5 min)
Step 5: Create EmailNotificationObserver      (3 min)
Step 6: Wire into HotelReservationServiceImpl (5 min)
Step 7: Wire into CheckInServiceImpl          (3 min)
```

---

### 🚦 Let's Start — Step 1

**Your task:** Create `ReservationEventType.java` enum inside `observer/` package.

Values it needs:
- `RESERVED` — when a reservation is successfully created
- `CANCELLED` — when a reservation is cancelled
- `MODIFIED` — when dates are changed
- `CHECKED_IN` — when guest checks in
- `CHECKED_OUT` — when guest checks out

Package: `com.lld.practice.tutor_sessions.hotel_reservation.observer`

Once you create it, share and I'll review. Then we move to Step 2.

---

## PATTERN 2: STATE — "The Traffic Light"

*(We implement this AFTER Observer is complete)*

### 🌍 Real-World Analogy

A traffic light changes colour:
- **RED** → only allows: turn right, wait. Blocks: go straight.
- **GREEN** → only allows: go. Blocks: turning right on red.
- **YELLOW** → only allows: stop if safe. Blocks: accelerating.

Each state has **its own rules** for what's allowed.

**Without State pattern:** You'd write `if status == RED then... else if status == GREEN then...` everywhere.

**With State pattern:** Each light colour is its own class with its own `allowedActions()`. No if/else needed.

---

### 🏨 How it appears in Hotel Reservation

```
Reservation Lifecycle:
RESERVED → CHECKED_IN → CHECKED_OUT (terminal — nothing more allowed)
    ↓
CANCELLED (terminal — nothing more allowed)
```

**Current problem — no enforcement:**
```java
// This currently works — WRONG! Can't cancel after checkout!
reservation.setReservationStatus(ReservationStatus.CANCELLED); // even if CHECKED_OUT
```

**With State pattern:**
```java
// CheckedOutState.cancel() throws HotelBookingException("Cannot cancel a checked-out reservation")
// This is enforced by the state class itself, not scattered if/else in services
```

---

## PATTERN 3: STRATEGY — "The GPS Navigation"

*(We implement this AFTER State is complete)*

### 🌍 Real-World Analogy

Google Maps lets you pick your route type:
- **Fastest route** — minimize time
- **Shortest route** — minimize distance
- **Scenic route** — avoid highways

Each is a different **algorithm** for the same task (find a route). You can swap them without changing the map itself.

---

### 🏨 How it appears in Hotel Reservation

**Current problem — hardcoded selection:**
```java
// Always picks ceil(guests/2) rooms starting from first available
int numberOfRooms = (int) Math.ceil(guest.size() / 2.0);
List<Room> bookedRoom = availableRooms.subList(0, numberOfRooms);
```

**With Strategy pattern:**
```java
// Inject the strategy at construction time
RoomSelectionStrategy strategy; // could be Default, Cheapest, ByType

List<Room> bookedRoom = strategy.select(availableRooms, guests); // strategy decides how
```

---

## 📋 LEARNING CHECKLIST

- [ ] **Observer** — ReservationEventType.java (Step 1)
- [ ] **Observer** — ReservationEvent.java (Step 2)
- [ ] **Observer** — ReservationObserver.java (Step 3)
- [ ] **Observer** — ReservationEventPublisher.java (Step 4)
- [ ] **Observer** — EmailNotificationObserver.java (Step 5)
- [ ] **Observer** — Wire into HotelReservationServiceImpl (Step 6)
- [ ] **Observer** — Wire into CheckInServiceImpl (Step 7)
- [ ] **State** — ReservationState interface (4 methods)
- [ ] **State** — ReservedState (allows checkIn, cancel, modify)
- [ ] **State** — CheckedInState (allows only checkOut)
- [ ] **State** — CheckedOutState (terminal — blocks all)
- [ ] **State** — CancelledState (terminal — blocks all)
- [ ] **State** — Wire into Reservation model
- [ ] **Strategy** — RoomSelectionStrategy interface
- [ ] **Strategy** — DefaultSelectionStrategy
- [ ] **Strategy** — CheapestRoomStrategy
- [ ] **Strategy** — Wire into HotelReservationServiceImpl

---

**Start now → create `ReservationEventType.java` and share it.**

