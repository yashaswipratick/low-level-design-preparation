# Pillar 2: ENTITIES & CARDINALITY

> **Goal:** For any LLD problem, identify the right entities, their relationships, cardinality, and identity — in under 60 seconds — using one repeatable 6-step pattern.

---

## The ONE pattern (memorize this, ignore everything else)

For every problem, walk these **6 steps in order**:

```
┌─────────────────────────────────────────────────────────────────┐
│ Step 1  TIER 1 — STATED      Visible: nouns the interviewer    │
│                              literally said.                   │
│                              Hidden: nouns you infer           │
│                              (transactional / policy / audit)  │
│                                                                 │
│ Step 2  TIER 2 — CONTAINER   What physical/logical space has   │
│                              capacity / position / state?      │
│                              (Slot, Room, Seat, Cell, Bucket…) │
│                                                                 │
│ Step 3  TIER 3 — TRANSACTION What event has timestamp /        │
│                              status / amount / money?          │
│                              (Ticket, Loan, Booking, Order…)   │
│                                                                 │
│ Step 4  RELATIONSHIPS (4 Q's)                                  │
│   Q1  Actor → Transaction       (ownership, transactional)     │
│   Q2  Transaction → Container   (assignment, temporary)        │
│   Q3  Transaction → Terminal    (Payment/Invoice/Result, 1-1)  │
│   Q4  Container composition     (Parent → Child, permanent)    │
│                                                                 │
│ Step 5  CARDINALITY                                            │
│   For EVERY entity pair from Steps 1-4: 1-1 / 1-N / N-N        │
│   ⚠️ Always ask: "1-1 ACTIVE vs 1-N HISTORICAL?"               │
│                                                                 │
│ Step 6  IDENTITY                                               │
│   For EVERY entity from Steps 1-3:                             │
│   Assigned (system-gen, A) or Derived (natural key, D)?        │
└─────────────────────────────────────────────────────────────────┘
```

### The 4 relationship flavors (vocabulary)

| Flavor | Meaning | Lifetime |
|---|---|---|
| **Composition** | Parent contains child (Floor → Slot) | Permanent |
| **Ownership** | Actor creates record (User → Loan) | Transactional |
| **Assignment** | Transaction occupies container (Loan → Copy) | Temporary (1-1 active) |
| **Terminal** | Transaction settles into final (Booking → Payment) | 1-1, fires on close |

### Cardinality → Java data structure

| Cardinality | Java choice |
|---|---|
| 1-1 | direct field |
| 1-N | `List<X>` / `Set<X>` |
| N-1 | foreign-key field |
| N-N (no metadata) | `Map<A, Set<B>>` |
| N-N (with metadata) | join entity (`Loan`, `Enrollment`, `Split`) |

### Identity rules

- **Derived** ID: stable natural key exists (ISBN, license plate, ticker, SKU).
- **Assigned** ID: no natural key, or it can change (UUID for Loan, Order, Trip).
- **Composite** key: appears naturally in join entities (`studentId+courseId`).

> **Consistency rule:** Every entity that appears in Step 1/2/3 MUST appear in Step 6 (identity), and every meaningful pair MUST appear in Step 5 (cardinality). If it's missing — you haven't fully designed it.

---

## How to use the per-domain blocks below

Each domain has **one block** with all 6 steps filled in:

```
DOMAIN
  Step 1 — T1
    Visible: …
    Hidden:  …
  Step 2 — T2 (Container): …
  Step 3 — T3 (Transaction): …
  Step 4 — Relationships (Q1/Q2/Q3/Q4): …
  Step 5 — Cardinality (every pair): …
  Step 6 — Identity (every entity): …
  ⚠️ Trap: …
```

---

## DOMAIN BLOCKS

### 1. Library

- **Step 1 — T1:**
  - Visible: `User`, `Book`, `Copy`
  - Hidden: `Loan`, `Reservation`, `Fine`
- **Step 2 — T2:** `Copy` (1 user at a time on the shelf)
- **Step 3 — T3:** `Loan` (issuedAt, dueDate, returnedAt); terminal: `Fine` if late; queue: `Reservation`
- **Step 4 — Relationships:**
  - Q1 `User → Loan` (owns, transactional)
  - Q2 `Loan → Copy` (assigns, temporary)
  - Q3 `Loan → Fine` (terminal, optional)
  - Q4 `Book → Copy` (composition)
- **Step 5 — Cardinality:**
  - `User ↔ Loan` = 1-N
  - `Book ↔ Copy` = 1-N
  - `Loan ↔ Copy` = **1-1 active / N-1 historical**
  - `User ↔ Reservation` = 1-N
  - `Copy ↔ Reservation` = 1-N (queue)
  - `Loan ↔ Fine` = 1-1 (optional)
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Book = isbn (D)`
  - `Copy = bookId+copyNum (D)`
  - `Loan = loanId (A)`
  - `Reservation = reservationId (A)`
  - `Fine = fineId (A)` or `loanId (D, 1-1)`
- **⚠️ Trap:** A copy has *many* loans over its lifetime, but only **one active loan** at a time.

---

### 2. Parking Lot

- **Step 1 — T1:**
  - Visible: `Vehicle`, `ParkingLot`, `Floor`
  - Hidden: `Slot`, `Ticket`, `Payment`, `RateCard`
- **Step 2 — T2:** `Slot` on `Floor` (holds 1 vehicle)
- **Step 3 — T3:** `Ticket` (entryTime, exitTime); terminal: `Payment`
- **Step 4 — Relationships:**
  - Q1 `Vehicle → Ticket` (owns)
  - Q2 `Ticket → Slot` (assigns)
  - Q3 `Ticket → Payment` (terminal, 1-1)
  - Q4 `ParkingLot → Floor → Slot` (composition)
- **Step 5 — Cardinality:**
  - `ParkingLot ↔ Floor` = 1-N
  - `Floor ↔ Slot` = 1-N
  - `Vehicle ↔ Ticket` = **1-1 active / 1-N historical**
  - `Slot ↔ Vehicle` = **1-1 active / 1-N historical**
  - `Ticket ↔ Payment` = 1-1
  - `RateCard ↔ Slot` = 1-N (type-based pricing)
- **Step 6 — Identity:**
  - `ParkingLot = lotId (A)` or `buildingName (D)`
  - `Floor = lotId+floorNum (D)`
  - `Slot = floorId+slotNum (D)`
  - `Vehicle = licensePlate (D)`
  - `Ticket = ticketId (A)`
  - `Payment = paymentId (A)` or `txnId+ticketId (D)`
  - `RateCard = rateCardId (A)`
- **⚠️ Trap:** "1 vehicle / 1 ticket" only holds *while parked*. Same vehicle returns daily → many tickets historically.

---

### 3. Hotel

- **Step 1 — T1:**
  - Visible: `Guest`, `Hotel`, `Room`
  - Hidden: `Reservation`, `RoomType`, `Invoice`, `Payment`, `Floor`
- **Step 2 — T2:** `Room` (of `RoomType`); holds 1 reservation per date range
- **Step 3 — T3:** `Reservation` (checkIn, checkOut, status); terminal: `Invoice`, `Payment`
- **Step 4 — Relationships:**
  - Q1 `Guest → Reservation` (owns)
  - Q2 `Reservation → Room` (assigns)
  - Q3 `Reservation → Invoice → Payment` (terminal, 1-1 each)
  - Q4 `Hotel → Floor → Room`; `RoomType → Room` (1-N)
- **Step 5 — Cardinality:**
  - `Hotel ↔ Floor` = 1-N
  - `Floor ↔ Room` = 1-N
  - `RoomType ↔ Room` = 1-N
  - `Guest ↔ Reservation` = 1-N
  - `Room ↔ Reservation` = **1-N over time, 1-1 per date range**
  - `Reservation ↔ Invoice` = 1-1
  - `Invoice ↔ Payment` = 1-1
- **Step 6 — Identity:**
  - `Hotel = hotelId (A)`
  - `Floor = hotelId+floorNum (D)`
  - `Room = hotelId+roomNum (D)`
  - `RoomType = roomTypeId (A)` or `name (D)`
  - `Guest = guestId (A)`
  - `Reservation = reservationId (A)`
  - `Invoice = invoiceId (A)`
  - `Payment = paymentId (A)`
- **⚠️ Trap:** Overlap detection — must enforce 1-1 *per overlapping date range*, not globally.

---

### 4. Ride-Sharing (Uber)

- **Step 1 — T1:**
  - Visible: `Rider`, `Driver`
  - Hidden: `Vehicle`, `RideRequest`, `Trip`, `Payment`, `Rating`
- **Step 2 — T2:** `Vehicle` (1 driver active at a time)
- **Step 3 — T3:** `RideRequest` → `Trip` (state evolution); terminal: `Payment`, `Rating`
- **Step 4 — Relationships:**
  - Q1 `Rider → RideRequest → Trip` (owns)
  - Q2 `Trip → Vehicle` (assigns)
  - Q3 `Trip → Payment + Rating` (terminal)
  - Q4 `Driver → Vehicle` (1 active)
- **Step 5 — Cardinality:**
  - `Rider ↔ RideRequest` = 1-N
  - `Rider ↔ Trip` = **1-N historical, 1-1 active**
  - `Driver ↔ Trip` = **1-N historical, 1-1 active**
  - `Driver ↔ Vehicle` = 1-1 active (or 1-N owned)
  - `Trip ↔ Vehicle` = N-1
  - `Trip ↔ Payment` = 1-1
  - `Trip ↔ Rating` = 1-2 (rider rates driver + driver rates rider)
- **Step 6 — Identity:**
  - `Rider = riderId (A)`
  - `Driver = driverId (A)`
  - `Vehicle = vehicleId (A)` or `licensePlate (D)`
  - `RideRequest = requestId (A)`
  - `Trip = tripId (A)`
  - `Payment = paymentId (A)`
  - `Rating = tripId+raterId (D)`
- **⚠️ Trap:** `RideRequest` and `Trip` are often *two* entities (request → accepted → trip).

---

### 5. Movie Booking (BookMyShow)

- **Step 1 — T1:**
  - Visible: `User`, `Movie`, `Theatre`, `Booking`, `Payment`
  - Hidden: `Screen`, `Show`, `Seat`, `Hold`
- **Step 2 — T2:** `Seat` (in `Screen`, per `Show`) — 1 booking per show
- **Step 3 — T3:** `Hold` → `Booking`; terminal: `Payment`
- **Step 4 — Relationships:**
  - Q1 `User → Booking` (owns)
  - Q2 `Booking → Seat(s)` (assigns, group of N)
  - Q3 `Booking → Payment` (terminal via Hold)
  - Q4 `Theatre → Screen → Seat`; `Show = Movie + Screen + Time`
- **Step 5 — Cardinality:**
  - `Theatre ↔ Screen` = 1-N
  - `Screen ↔ Seat` = 1-N
  - `Movie ↔ Show` = 1-N
  - `Screen ↔ Show` = 1-N
  - `User ↔ Booking` = 1-N
  - `Booking ↔ Seat` = 1-N (group booking)
  - `Seat ↔ Show` = N-1 (same physical seat reused across shows)
  - `Seat+Show ↔ Booking` = **1-1 per show**
  - `Booking ↔ Payment` = 1-1
  - `Booking ↔ Hold` = 1-1 (transient pre-payment)
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Movie = movieId (A)`
  - `Theatre = theatreId (A)` or `name (D)`
  - `Screen = theatreId+screenNum (D)`
  - `Show = movieId+screenId+startTime (D)`
  - `Seat = screenId+row+col (D)` (physical) or `showId+row+col (D)` (per-show)
  - `Booking = bookingId (A)`
  - `Payment = txnId (A)` or `txnId+bookingId (D)`
  - `Hold = holdId (A)` (expires)
- **⚠️ Trap:** A single physical seat is reused across many shows → key is *per-show*, not global.

---

### 6. ATM

- **Step 1 — T1:**
  - Visible: `Customer`, `ATM`, `Money`
  - Hidden: `Account`, `Card`, `Session`, `Transaction`, `CashInventory`
- **Step 2 — T2:** `Account`, `Card`, `CashInventory`
- **Step 3 — T3:** `Session` (card-insert → eject); `Transaction` (withdraw/deposit/balance)
- **Step 4 — Relationships:**
  - Q1 `Customer → Session → Transaction` (owns)
  - Q2 `Transaction → Account` (debits/credits)
  - Q3 `Transaction → Receipt`
  - Q4 `Customer → Account → Card`; `ATM → CashInventory`
- **Step 5 — Cardinality:**
  - `Customer ↔ Account` = 1-N (or N-N for joint)
  - `Account ↔ Card` = 1-N
  - `Customer ↔ Session` = 1-N historical, 1-1 active
  - `Session ↔ Transaction` = 1-N
  - `Account ↔ Transaction` = 1-N (ledger)
  - `ATM ↔ CashInventory` = 1-1
  - `Card ↔ Session` = 1-1 active
- **Step 6 — Identity:**
  - `Customer = customerId (A)`
  - `ATM = atmId (A)`
  - `Account = accountNumber (A)`
  - `Card = cardNumber (A)`
  - `Session = sessionId (A)`
  - `Transaction = txnId (A)`
  - `CashInventory = atmId (D, 1-1)`
- **⚠️ Trap:** Card vs Account is N-N over time but 1-1 per swipe.

---

### 7. KV Store / Cache

- **Step 1 — T1:**
  - Visible: `Key`, `Value`
  - Hidden: `Entry`, `Cache`, `EvictionPolicy`, `ExpiryPolicy`
- **Step 2 — T2:** `Entry` (in `Cache`) — holds 1 value per key
- **Step 3 — T3:** `ExpiryEvent`, `EvictionEvent` (no money)
- **Step 4 — Relationships:**
  - Q1 `Client → put/get` (no actor record)
  - Q2 `Entry → Key` (1-1)
  - Q3 `Entry → ExpiryEvent / EvictionEvent`
  - Q4 `Cache → Entry`; `Cache → EvictionPolicy` (strategy)
- **Step 5 — Cardinality:**
  - `Cache ↔ Entry` = 1-N (`Map<K,V>`)
  - `Entry ↔ Key` = 1-1
  - `Entry ↔ Value` = 1-1
  - `Cache ↔ EvictionPolicy` = 1-1
  - `Entry ↔ ExpiryPolicy` = N-1
- **Step 6 — Identity:**
  - `Cache = singleton` (or `cacheName (D)`)
  - `Entry = key (D, user-supplied)`
  - `EvictionPolicy = policyName (D)` (LRU/LFU/TTL)
  - `ExpiryPolicy = policyId (A)` or `ttlSeconds (D)`

---

### 8. URL Shortener

- **Step 1 — T1:**
  - Visible: `LongURL`, `ShortURL`
  - Hidden: `ShortLink`, `Owner`, `ClickEvent`, `ExpiryPolicy`, `Alias`
- **Step 2 — T2:** `ShortLink` (1 alias slot per shortCode)
- **Step 3 — T3:** `ClickEvent`; policy: `ExpiryPolicy`
- **Step 4 — Relationships:**
  - Q1 `Owner → ShortLink` (creates)
  - Q2 `ShortLink → shortCode slot`
  - Q3 `ShortLink → ClickEvent(s)`
  - Q4 (none — flat)
- **Step 5 — Cardinality:**
  - `Owner ↔ ShortLink` = 1-N
  - `LongURL ↔ ShortLink` = 1-1 OR 1-N (if custom aliases allowed)
  - `ShortLink ↔ ClickEvent` = 1-N
  - `ShortLink ↔ ExpiryPolicy` = N-1
- **Step 6 — Identity:**
  - `Owner = userId (A)`
  - `LongURL = url string (D)`
  - `ShortLink = shortCode (A, or D for custom alias)`
  - `ClickEvent = eventId (A)`
  - `ExpiryPolicy = policyId (A)`

---

### 9. Tic-Tac-Toe

- **Step 1 — T1:**
  - Visible: `Player`, `Board`
  - Hidden: `Game`, `Move`, `Cell`, `GameResult`, `Symbol`
- **Step 2 — T2:** `Cell` on `Board` (3×3, 1 symbol)
- **Step 3 — T3:** `Move`; terminal: `GameResult`
- **Step 4 — Relationships:**
  - Q1 `Player → Move` (owns)
  - Q2 `Move → Cell` (assigns, permanent within game)
  - Q3 `Game → GameResult` (terminal)
  - Q4 `Board → Cell` (composition, 3×3)
- **Step 5 — Cardinality:**
  - `Game ↔ Player` = 1-2 (exactly)
  - `Game ↔ Board` = 1-1
  - `Board ↔ Cell` = 1-9
  - `Game ↔ Move` = 1-N ordered
  - `Player ↔ Move` = 1-N
  - `Move ↔ Cell` = 1-1 per game
  - `Cell ↔ Symbol` = 0..1 per game
  - `Game ↔ GameResult` = 1-1
- **Step 6 — Identity:**
  - `Game = gameId (A)`
  - `Player = playerId (A)`
  - `Board = gameId (D, 1-1)`
  - `Cell = row+col (D)`
  - `Move = gameId+sequenceNum (D)`
  - `GameResult = gameId (D, 1-1)`
  - `Symbol = enum {X, O}`

---

### 10. Online Learning

- **Step 1 — T1:**
  - Visible: `Student`, `Course`, `Instructor`
  - Hidden: `Lesson`, `Enrollment`, `Progress`, `Quiz`, `Certificate`, `Payment`
- **Step 2 — T2:** `Lesson` (slot in course)
- **Step 3 — T3:** `Enrollment`, `Progress`; terminal: `Certificate`, `Payment`
- **Step 4 — Relationships:**
  - Q1 `Student → Enrollment` (owns)
  - Q2 `Enrollment → Course/Lesson` (assigns)
  - Q3 `Enrollment → Certificate/Payment`
  - Q4 `Course → Lesson` (composition, ordered)
- **Step 5 — Cardinality:**
  - `Instructor ↔ Course` = 1-N
  - `Course ↔ Lesson` = 1-N ordered
  - `Student ↔ Course` = N-N (via `Enrollment`)
  - `Student ↔ Enrollment` = 1-N
  - `Enrollment ↔ Progress` = 1-N (one per lesson)
  - `Lesson ↔ Progress` = 1-N (one per student)
  - `Enrollment ↔ Certificate` = 1-1 (on completion)
  - `Enrollment ↔ Payment` = 1-1
  - `Course ↔ Quiz` = 1-N
- **Step 6 — Identity:**
  - `Student = studentId (A)`
  - `Instructor = instructorId (A)`
  - `Course = courseId (A)`
  - `Lesson = courseId+lessonNum (D)`
  - `Enrollment = studentId+courseId (D)` or `enrollmentId (A)`
  - `Progress = enrollmentId+lessonId (D)`
  - `Quiz = quizId (A)`
  - `Certificate = certificateId (A)`
  - `Payment = paymentId (A)`

---

### 11. Twitter/X

- **Step 1 — T1:**
  - Visible: `User`, `Tweet`
  - Hidden: `Follow`, `Like`, `Retweet`, `Reply`, `Feed`/`Timeline`, `Notification`
- **Step 2 — T2:** `Feed`/`Timeline` (per user)
- **Step 3 — T3:** `Follow`, `Like`, `Retweet`, `Reply` (engagement events)
- **Step 4 — Relationships:**
  - Q1 `User → Tweet` (authors); `User → Follow` (creates edge)
  - Q2 `Tweet → Feed` (of followers)
  - Q3 `Tweet → Like/Reply/Retweet`
  - Q4 (none)
- **Step 5 — Cardinality:**
  - `User ↔ Follow ↔ User` = N-N self-referential
  - `User ↔ Tweet` = 1-N
  - `User ↔ Feed` = 1-1
  - `Tweet ↔ Like` = 1-N
  - `Tweet ↔ Reply` = 1-N
  - `Tweet ↔ Retweet` = 1-N
  - `Feed ↔ Tweet` = N-N (timeline)
- **Step 6 — Identity:**
  - `User = userId (A)` or `handle (D)`
  - `Tweet = tweetId (A)`
  - `Follow = followerId+followeeId (D)`
  - `Like = userId+tweetId (D)`
  - `Retweet = userId+tweetId (D)`
  - `Reply = replyTweetId (A)` (a Tweet with parentTweetId)
  - `Feed = userId (D, 1-1)`

---

### 12. Food Delivery (Swiggy/Zomato)

- **Step 1 — T1:**
  - Visible: `Customer`, `Restaurant`, `MenuItem`
  - Hidden: `Cart`, `Order`, `DeliveryAgent`, `Payment`, `OrderStatusEvent`, `Address`
- **Step 2 — T2:** `Cart` (1 active per customer)
- **Step 3 — T3:** `Order` (with `OrderStatusEvent`s); terminal: `Payment`
- **Step 4 — Relationships:**
  - Q1 `Customer → Cart → Order` (state evolution)
  - Q2 `Order → Restaurant + DeliveryAgent`
  - Q3 `Order → Payment + OrderStatusEvent(s)`
  - Q4 `Restaurant → MenuItem` (composition)
- **Step 5 — Cardinality:**
  - `Restaurant ↔ MenuItem` = 1-N
  - `Customer ↔ Cart` = 1-1 active
  - `Cart ↔ MenuItem` = N-N (via `LineItem`)
  - `Customer ↔ Order` = 1-N
  - `Order ↔ MenuItem` = N-N (via `LineItem`)
  - `Order ↔ DeliveryAgent` = N-1 active
  - `Order ↔ Payment` = 1-1
  - `Order ↔ OrderStatusEvent` = 1-N (audit trail)
  - `Customer ↔ Address` = 1-N
- **Step 6 — Identity:**
  - `Customer = customerId (A)`
  - `Restaurant = restaurantId (A)`
  - `MenuItem = restaurantId+itemId (D)`
  - `Cart = customerId (D, 1-1)`
  - `Order = orderId (A)`
  - `DeliveryAgent = agentId (A)`
  - `Payment = paymentId (A)`
  - `OrderStatusEvent = orderId+seqNum (D)`
  - `Address = addressId (A)`

---

### 13. E-commerce / Cart

- **Step 1 — T1:**
  - Visible: `User`, `Product`, `Cart`
  - Hidden: `Order`, `Inventory`, `Payment`, `Shipment`, `Discount`, `Warehouse`
- **Step 2 — T2:** `Cart` (1 active), `Inventory` (per warehouse)
- **Step 3 — T3:** `Order`; terminal: `Payment`, `Shipment`
- **Step 4 — Relationships:**
  - Q1 `User → Cart → Order` (state evolution)
  - Q2 `Order → Inventory` (decrements)
  - Q3 `Order → Payment + Shipment`
  - Q4 `Warehouse → Inventory → Product`
- **Step 5 — Cardinality:**
  - `User ↔ Cart` = 1-1 active
  - `Cart ↔ Product` = N-N (via `CartItem`)
  - `User ↔ Order` = 1-N
  - `Order ↔ Product` = N-N (via `OrderLineItem`)
  - `Warehouse ↔ Inventory` = 1-N
  - `Product ↔ Inventory` = 1-N (per warehouse)
  - `Order ↔ Payment` = 1-1
  - `Order ↔ Shipment` = 1-N (split shipments)
  - `Order ↔ Discount` = N-N
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Product = productId (A)` or `SKU (D)`
  - `Cart = userId (D, 1-1)`
  - `Order = orderId (A)`
  - `Warehouse = warehouseId (A)`
  - `Inventory = warehouseId+productId (D)`
  - `Payment = paymentId (A)`
  - `Shipment = shipmentId (A)`
  - `Discount = discountCode (D)` or `discountId (A)`

---

### 14. Chess

- **Step 1 — T1:**
  - Visible: `Player`, `Board`, `Piece`
  - Hidden: `Game`, `Move`, `MoveValidator`, `GameResult`, `Clock`, `Cell`
- **Step 2 — T2:** `Cell` on `Board` (8×8)
- **Step 3 — T3:** `Move` (from→to, with timestamp); terminal: `GameResult`
- **Step 4 — Relationships:**
  - Q1 `Player → Move` (owns)
  - Q2 `Move → Cell` (from→to)
  - Q3 `Game → GameResult` (terminal); `Clock` per player
  - Q4 `Board → Cell` (8×8)
- **Step 5 — Cardinality:**
  - `Game ↔ Player` = 1-2
  - `Game ↔ Board` = 1-1
  - `Board ↔ Cell` = 1-64
  - `Game ↔ Move` = 1-N ordered ledger
  - `Board ↔ Piece` = 1-N (max 32)
  - `Piece ↔ Cell` = 1-1 (current position)
  - `Player ↔ Clock` = 1-1 per game
  - `Game ↔ GameResult` = 1-1
- **Step 6 — Identity:**
  - `Game = gameId (A)`
  - `Player = playerId (A)`
  - `Board = gameId (D, 1-1)`
  - `Cell = row+col (D)`
  - `Piece = gameId+pieceId (D)`
  - `Move = gameId+moveNum (D)`
  - `Clock = gameId+playerId (D)`
  - `GameResult = gameId (D, 1-1)`

---

### 15. Snake & Ladder

- **Step 1 — T1:**
  - Visible: `Player`, `Board`, `Dice`
  - Hidden: `Game`, `Snake`, `Ladder`, `Cell`, `MoveResult`, `GameResult`
- **Step 2 — T2:** `Cell` on `Board` (1-100)
- **Step 3 — T3:** `MoveResult`; terminal: `GameResult`
- **Step 4 — Relationships:**
  - Q1 `Player → MoveResult` (via Dice roll)
  - Q2 `MoveResult → Cell`
  - Q3 `Game → GameResult`
  - Q4 `Board → Cell` + `Snake/Ladder` mappings
- **Step 5 — Cardinality:**
  - `Game ↔ Player` = 1-N (2-4 typical)
  - `Game ↔ Board` = 1-1
  - `Board ↔ Cell` = 1-100
  - `Board ↔ Snake` = 1-N
  - `Board ↔ Ladder` = 1-N
  - `Game ↔ MoveResult` = 1-N ordered
  - `Player ↔ Cell` = 1-1 (current position)
  - `Game ↔ Dice` = 1-1
  - `Game ↔ GameResult` = 1-1
- **Step 6 — Identity:**
  - `Game = gameId (A)`
  - `Player = playerId (A)`
  - `Board = gameId (D, 1-1)`
  - `Cell = cellNumber (D, 1-100)`
  - `Snake = headCell+tailCell (D)`
  - `Ladder = bottomCell+topCell (D)`
  - `MoveResult = gameId+seqNum (D)`
  - `Dice = gameId (D, 1-1)`
  - `GameResult = gameId (D, 1-1)`

---

### 16. Elevator

- **Step 1 — T1:**
  - Visible: `Elevator`, `Floor`, `Building`
  - Hidden: `Person`, `Request`, `Direction`, `DispatchPolicy`, `ElevatorState`
- **Step 2 — T2:** `Elevator` (cabin) at `Floor`
- **Step 3 — T3:** `Request` (button press, queued)
- **Step 4 — Relationships:**
  - Q1 `Person → Request` (owns)
  - Q2 `Request → Elevator → Floor`
  - Q3 `Request → completed event`
  - Q4 `Building → Elevator(s) + Floor(s)`; `Building → DispatchPolicy` (strategy)
- **Step 5 — Cardinality:**
  - `Building ↔ Elevator` = 1-N
  - `Building ↔ Floor` = 1-N
  - `Elevator ↔ Request` = 1-N pending queue
  - `Elevator ↔ Floor` = 1-1 current position
  - `Elevator ↔ ElevatorState` = 1-1
  - `Building ↔ DispatchPolicy` = 1-1 (strategy)
  - `Request ↔ Floor` = N-1 (source + destination)
- **Step 6 — Identity:**
  - `Building = buildingId (A)`
  - `Elevator = elevatorId (A)`
  - `Floor = buildingId+floorNum (D)`
  - `Request = requestId (A)`
  - `Person = personId (A)` (often anonymous)
  - `DispatchPolicy = policyName (D)` (SCAN, LOOK, nearest)
  - `ElevatorState = enum {IDLE, UP, DOWN, MAINTENANCE}`

---

### 17. Vending Machine

- **Step 1 — T1:**
  - Visible: `Item`, `Machine`, `User`
  - Hidden: `Slot`, `Inventory`, `Coin`/`Note`, `Transaction`, `Refund`
- **Step 2 — T2:** `Slot` (A1, B2 — holds N units of 1 item)
- **Step 3 — T3:** `Transaction`; terminal: `Refund` (optional)
- **Step 4 — Relationships:**
  - Q1 `User → Transaction` (owns)
  - Q2 `Transaction → Slot` (decrements count)
  - Q3 `Transaction → Refund?`
  - Q4 `Machine → Slot` (composition)
- **Step 5 — Cardinality:**
  - `Machine ↔ Slot` = 1-N
  - `Slot ↔ Item` = 1-1 + count
  - `User ↔ Transaction` = 1-N
  - `Transaction ↔ Slot` = N-1
  - `Transaction ↔ Refund` = 1-0..1
  - `Machine ↔ CashInventory (Coin/Note)` = 1-N
- **Step 6 — Identity:**
  - `Machine = machineId (A)`
  - `Slot = machineId+slotCode (D, e.g., A1)`
  - `Item = itemId (A)` or `barcode (D)`
  - `User = userId (A)` (often anonymous)
  - `Transaction = txnId (A)`
  - `Refund = txnId (D, 1-1)` or `refundId (A)`
  - `Coin/Note = denomination (D)`

---

### 18. Splitwise

- **Step 1 — T1:**
  - Visible: `User`, `Group`, `Expense`
  - Hidden: `Split`, `Settlement`, `Balance`
- **Step 2 — T2:** `Group` (logical container); `Balance` ledger per (user, user) pair
- **Step 3 — T3:** `Split` (per-participant share); terminal: `Settlement` (clears Balance)
- **Step 4 — Relationships:**
  - Q1 `User → Expense` (in `Group`) (owns)
  - Q2 `Expense → Split(s)` (per user)
  - Q3 `Expense → Settlement` (clears Balance pair)
  - Q4 `Group → User(s)` (N-N); `Expense → Split` (composition)
- **Step 5 — Cardinality:**
  - `Group ↔ User` = N-N
  - `Group ↔ Expense` = 1-N
  - `User ↔ Expense` = 1-N (paid by)
  - `Expense ↔ Split` = 1-N (one per participant)
  - `User ↔ Split` = 1-N
  - `User ↔ User Balance` = N-N pair-wise
  - `User ↔ Settlement` = 1-N
  - `Balance ↔ Settlement` = 1-N (clears history)
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Group = groupId (A)`
  - `Expense = expenseId (A)`
  - `Split = expenseId+userId (D)`
  - `Settlement = settlementId (A)`
  - `Balance = fromUserId+toUserId (D)`
- **⚠️ Trap:** Container isn't physical — it's the `Group`. The "real" container is the per-pair `Balance` ledger that splits accumulate into.

---

### 19. Notification System

- **Step 1 — T1:**
  - Visible: `User`, `Notification`
  - Hidden: `Channel` (email/SMS/push), `Template`, `Subscription`, `DeliveryAttempt`
- **Step 2 — T2:** `Channel` (email/SMS/push pipe)
- **Step 3 — T3:** `Notification`, `DeliveryAttempt` (retries)
- **Step 4 — Relationships:**
  - Q1 `System → Notification` (creates)
  - Q2 `Notification → Channel` (via `Subscription`)
  - Q3 `Notification → DeliveryAttempt(s)`
  - Q4 `User → Subscription(s)` (N-N to topic+channel)
- **Step 5 — Cardinality:**
  - `User ↔ Subscription` = 1-N
  - `Subscription ↔ Channel` = N-1
  - `User ↔ Channel` = N-N (via `Subscription`)
  - `Notification ↔ User` = 1-N (recipients)
  - `Notification ↔ Channel` = 1-N (fan-out)
  - `Notification ↔ DeliveryAttempt` = 1-N (retries)
  - `Notification ↔ Template` = N-1
  - `Channel ↔ Template` = 1-N
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Notification = notificationId (A)`
  - `Channel = channelType (D, e.g., EMAIL/SMS/PUSH)` or `channelId (A)`
  - `Template = templateId (A)`
  - `Subscription = userId+topic+channelType (D)`
  - `DeliveryAttempt = notificationId+userId+channel+attemptNum (D)`

---

### 20. Chat / Messaging

- **Step 1 — T1:**
  - Visible: `User`, `Message`
  - Hidden: `Conversation`, `Group`, `Participant`, `ReadReceipt`, `Attachment`
- **Step 2 — T2:** `Conversation` (holds participants + messages)
- **Step 3 — T3:** `Message`; terminal: `ReadReceipt(s)`, `Attachment` upload
- **Step 4 — Relationships:**
  - Q1 `User → Message` (owns)
  - Q2 `Message → Conversation`
  - Q3 `Message → ReadReceipt(s)`
  - Q4 `Conversation → Participant(s)` (N-N via join)
- **Step 5 — Cardinality:**
  - `Conversation ↔ User` = N-N (via `Participant`)
  - `Conversation ↔ Message` = 1-N ordered
  - `User ↔ Message` = 1-N (sender)
  - `Message ↔ ReadReceipt` = 1-N (per recipient)
  - `Message ↔ Attachment` = 1-N
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Conversation = conversationId (A)`
  - `Message = messageId (A)`
  - `Participant = conversationId+userId (D)`
  - `ReadReceipt = messageId+userId (D)`
  - `Attachment = attachmentId (A)`

---

### 21. File Storage (Dropbox)

- **Step 1 — T1:**
  - Visible: `User`, `File`, `Folder`
  - Hidden: `Version`, `ShareLink`, `Permission`, `SyncEvent`, `Quota`
- **Step 2 — T2:** `Folder` (tree), `Quota` (per user)
- **Step 3 — T3:** `Version`, `ShareLink`, `SyncEvent`
- **Step 4 — Relationships:**
  - Q1 `User → File/Version` (owns)
  - Q2 `File → Folder` (path)
  - Q3 `File → ShareLink + Permission`
  - Q4 `Folder → File(s)` (tree)
- **Step 5 — Cardinality:**
  - `User ↔ File` = 1-N owned, N-N shared
  - `User ↔ Folder` = 1-N
  - `Folder ↔ File` = 1-N (tree)
  - `Folder ↔ Folder` = 1-N (subfolder)
  - `File ↔ Version` = 1-N
  - `File ↔ ShareLink` = 1-N
  - `ShareLink ↔ Permission` = 1-1
  - `User ↔ Quota` = 1-1
  - `File ↔ SyncEvent` = 1-N
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `File = fileId (A)`
  - `Folder = folderId (A)`
  - `Version = fileId+versionNum (D)`
  - `ShareLink = token (A, opaque)`
  - `Permission = shareLinkId (D, 1-1)`
  - `Quota = userId (D, 1-1)`
  - `SyncEvent = eventId (A)`

---

### 22. Stock Exchange

- **Step 1 — T1:**
  - Visible: `User`, `Stock`, `Order`
  - Hidden: `OrderBook`, `Trade`, `Portfolio`, `Position`, `MatchingEngine`
- **Step 2 — T2:** `OrderBook` (buy + sell queues per stock)
- **Step 3 — T3:** `Order`; terminal: `Trade` (when matched), `Position` snapshot
- **Step 4 — Relationships:**
  - Q1 `User → Order` (owns)
  - Q2 `Order → OrderBook` (per Stock)
  - Q3 `Order ⊕ Order → Trade` (matching produces)
  - Q4 `Stock → OrderBook` (1-1)
- **Step 5 — Cardinality:**
  - `Stock ↔ OrderBook` = 1-1
  - `OrderBook ↔ Order` = 1-N (buy queue + sell queue)
  - `User ↔ Order` = 1-N
  - `Order ↔ Trade` = 1-N (partial fills)
  - `User ↔ Portfolio` = 1-1
  - `Portfolio ↔ Position` = 1-N (one per stock)
  - `Stock ↔ Position` = 1-N
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Stock = ticker (D, e.g., AAPL)`
  - `OrderBook = ticker (D, 1-1)`
  - `Order = orderId (A)`
  - `Trade = tradeId (A)`
  - `Portfolio = userId (D, 1-1)`
  - `Position = userId+ticker (D)`
  - `MatchingEngine = singleton`

---

### 23. Logger

- **Step 1 — T1:**
  - Visible: `Message`
  - Hidden: `LogEvent`, `LogLevel`, `Logger`, `Appender`/`Sink`, `Formatter`, `Filter`
- **Step 2 — T2:** `Appender`/`Sink` (output pipe — file/console/network)
- **Step 3 — T3:** `LogEvent` (level, message, timestamp, source)
- **Step 4 — Relationships:**
  - Q1 `Code → LogEvent`
  - Q2 `LogEvent → Appender` (via `Filter` pipeline)
  - Q3 `Appender → formatted output`
  - Q4 `Logger → Appender(s)` (1-N); `Appender → Formatter` (1-1)
- **Step 5 — Cardinality:**
  - `Logger ↔ Appender` = 1-N
  - `Logger ↔ Logger` = 1-N (hierarchical parent/child)
  - `Appender ↔ Filter` = 1-N
  - `Appender ↔ Formatter` = 1-1
  - `LogEvent ↔ LogLevel` = N-1
  - `Logger ↔ LogEvent` = 1-N
- **Step 6 — Identity:**
  - `LogEvent = eventId (A) or implicit (no ID needed)`
  - `Logger = loggerName (D, hierarchical, e.g., com.foo.Bar)`
  - `Appender = appenderName (D)`
  - `Formatter = formatterId (A)`
  - `Filter = filterId (A)`
  - `LogLevel = enum {TRACE/DEBUG/INFO/WARN/ERROR}`

---

### 24. Rate Limiter

- **Step 1 — T1:**
  - Visible: `Request`, `Client`
  - Hidden: `Bucket`, `Quota`, `Window`, `Rule`
- **Step 2 — T2:** `Bucket` (per client+rule, holds N tokens)
- **Step 3 — T3:** `AllowDecision` (with timestamp)
- **Step 4 — Relationships:**
  - Q1 `Client → Request`
  - Q2 `Request → Bucket` (per Client+Rule)
  - Q3 `Request → AllowDecision`
  - Q4 `Rule → Bucket(s)` (N-1 governance); `Rule → Window + Quota` (composite)
- **Step 5 — Cardinality:**
  - `Client ↔ Bucket` = 1-N (one per rule)
  - `Rule ↔ Bucket` = 1-N
  - `Client ↔ Request` = 1-N
  - `Request ↔ Bucket` = N-1
  - `Request ↔ AllowDecision` = 1-1
  - `API ↔ Rule` = N-1 (many APIs share rule)
  - `Rule ↔ Window` = 1-1
  - `Rule ↔ Quota` = 1-1
- **Step 6 — Identity:**
  - `Client = apiKey (D)` or `IP (D)`
  - `Request = requestId (A)`
  - `Bucket = clientId+ruleId (D)`
  - `Rule = ruleId (A)`
  - `Window = windowSeconds (D)`
  - `Quota = maxRequests (D)`
  - `AllowDecision = requestId (D, 1-1)`

---

### 25. Calendar

- **Step 1 — T1:**
  - Visible: `User`, `Event`
  - Hidden: `Calendar`, `Invitee`, `RSVP`, `Recurrence`, `Reminder`
- **Step 2 — T2:** `Calendar` (date×time grid)
- **Step 3 — T3:** `Invitee`+`RSVP`, `Reminder` fire
- **Step 4 — Relationships:**
  - Q1 `User → Event` (organizes)
  - Q2 `Event → Calendar slot` (date+time)
  - Q3 `Event → Reminder(s) + RSVP(s)`
  - Q4 `User → Calendar`; `Event → Recurrence` (1-1)
- **Step 5 — Cardinality:**
  - `User ↔ Calendar` = 1-N (multiple calendars)
  - `Calendar ↔ Event` = 1-N
  - `User ↔ Event` = 1-N organized
  - `Event ↔ Invitee` = 1-N
  - `Invitee ↔ RSVP` = 1-1
  - `User ↔ Event` = N-N attended (via `Invitee`)
  - `Event ↔ Recurrence` = 1-1 (optional)
  - `Event ↔ Reminder` = 1-N per user
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Calendar = calendarId (A)`
  - `Event = eventId (A)`
  - `Invitee = eventId+userId (D)`
  - `RSVP = eventId+userId (D)` or `enum status`
  - `Recurrence = eventId (D, 1-1)`
  - `Reminder = eventId+userId+offsetMin (D)`

---

### 26. Music Streaming (Spotify)

- **Step 1 — T1:**
  - Visible: `User`, `Song`, `Artist`
  - Hidden: `Playlist`, `PlayEvent`, `Subscription`, `Album`, `Recommendation`
- **Step 2 — T2:** `Playlist` (ordered slot list)
- **Step 3 — T3:** `PlayEvent`; subscription state: `Subscription`
- **Step 4 — Relationships:**
  - Q1 `User → PlayEvent` (owns)
  - Q2 `PlayEvent → Song` (in Playlist)
  - Q3 `PlayEvent → contributes to Recommendation`
  - Q4 `User → Playlist → Song(s)` (N-N via join); `Artist → Album → Song`
- **Step 5 — Cardinality:**
  - `Artist ↔ Album` = 1-N
  - `Album ↔ Song` = 1-N
  - `Artist ↔ Song` = N-N (collaborations)
  - `User ↔ Playlist` = 1-N
  - `Playlist ↔ Song` = N-N ordered
  - `User ↔ PlayEvent` = 1-N
  - `Song ↔ PlayEvent` = 1-N
  - `User ↔ Subscription` = 1-1 active
  - `User ↔ Recommendation` = 1-N
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Song = songId (A)`
  - `Artist = artistId (A)`
  - `Album = albumId (A)`
  - `Playlist = playlistId (A)`
  - `PlayEvent = eventId (A)`
  - `Subscription = userId (D, 1-1)` or `subscriptionId (A)`
  - `Recommendation = userId+songId (D)`

---

### 27. Video Streaming (Netflix)

- **Step 1 — T1:**
  - Visible: `User`, `Video`
  - Hidden: `Show`, `Season`, `Episode`, `Subscription`, `WatchHistory`, `Profile`, `Recommendation`
- **Step 2 — T2:** `Episode` slot in `Season`/`Show`
- **Step 3 — T3:** `WatchHistory` (with resume position); state: `Subscription`
- **Step 4 — Relationships:**
  - Q1 `User → WatchHistory entry` (owns)
  - Q2 `WatchHistory → Episode` (in Season/Show)
  - Q3 `WatchHistory → resume position`
  - Q4 `Show → Season → Episode` (composition tree)
- **Step 5 — Cardinality:**
  - `Show ↔ Season` = 1-N
  - `Season ↔ Episode` = 1-N
  - `User ↔ Profile` = 1-N (multi-profile)
  - `Profile ↔ WatchHistory` = 1-N (one per video)
  - `Video ↔ WatchHistory` = 1-N
  - `User ↔ Subscription` = 1-1 active
  - `Profile ↔ Recommendation` = 1-N
- **Step 6 — Identity:**
  - `User = userId (A)`
  - `Profile = userId+profileNum (D)`
  - `Video = videoId (A)`
  - `Show = showId (A)`
  - `Season = showId+seasonNum (D)`
  - `Episode = showId+seasonNum+epNum (D)`
  - `WatchHistory = profileId+videoId (D)`
  - `Subscription = userId (D, 1-1)`
  - `Recommendation = profileId+videoId (D)`

---

### 28. Airbnb

- **Step 1 — T1:**
  - Visible: `Host`, `Guest`, `Listing`
  - Hidden: `Booking`, `Review`, `Pricing`, `Availability`, `Payment`
- **Step 2 — T2:** `Listing` per date (availability calendar)
- **Step 3 — T3:** `Booking`; terminal: `Payment`, `Review` (×2)
- **Step 4 — Relationships:**
  - Q1 `Guest → Booking` (owns)
  - Q2 `Booking → Listing` (per date range)
  - Q3 `Booking → Payment + Review(s)`
  - Q4 `Host → Listing(s)` (1-N)
- **Step 5 — Cardinality:**
  - `Host ↔ Listing` = 1-N
  - `Guest ↔ Booking` = 1-N
  - `Listing ↔ Booking` = **1-N over time, non-overlapping per date**
  - `Booking ↔ Payment` = 1-1
  - `Booking ↔ Review` = 1-2 (guest + host)
  - `Listing ↔ Pricing` = 1-N (date-based)
  - `Listing ↔ Availability` = 1-N (date-based calendar)
- **Step 6 — Identity:**
  - `Host = hostId (A)`
  - `Guest = guestId (A)`
  - `Listing = listingId (A)`
  - `Booking = bookingId (A)`
  - `Review = reviewId (A)`
  - `Payment = paymentId (A)`
  - `Pricing = listingId+date (D)`
  - `Availability = listingId+date (D)`

---

### 29. Banking

- **Step 1 — T1:**
  - Visible: `Customer`, `Account`
  - Hidden: `Transaction`, `Card`, `Loan`, `Beneficiary`, `Statement`
- **Step 2 — T2:** `Account`, `Card`
- **Step 3 — T3:** `Transaction` (debit/credit); terminal: `Statement` entry
- **Step 4 — Relationships:**
  - Q1 `Customer → Transaction` (owns)
  - Q2 `Transaction → Account` (debit/credit)
  - Q3 `Transaction → Statement entry`
  - Q4 `Customer → Account → Card` (composition chain)
- **Step 5 — Cardinality:**
  - `Customer ↔ Account` = 1-N (or N-N for joint)
  - `Account ↔ Card` = 1-N
  - `Account ↔ Transaction` = 1-N (ledger)
  - `Customer ↔ Loan` = 1-N
  - `Account ↔ Beneficiary` = 1-N
  - `Account ↔ Statement` = 1-N (periodic)
  - `Statement ↔ Transaction` = 1-N
- **Step 6 — Identity:**
  - `Customer = customerId (A)`
  - `Account = accountNumber (A)`
  - `Card = cardNumber (A)`
  - `Transaction = txnId (A)`
  - `Loan = loanId (A)`
  - `Beneficiary = customerId+accountNumber (D)`
  - `Statement = accountNumber+period (D)`

---

## Quick Reference Card

```
┌──────────────────────────────────────────────────────┐
│  PILLAR 2 — 6 STEPS (memorize)                       │
├──────────────────────────────────────────────────────┤
│  1. T1 STATED       → Visible + Hidden               │
│  2. T2 CONTAINER    → capacity / position / state    │
│  3. T3 TRANSACTION  → timestamp / amount / status    │
│  4. RELATIONSHIPS   → Q1 owns / Q2 occupies          │
│                       Q3 terminal / Q4 composition   │
│  5. CARDINALITY     → EVERY pair: 1-1 / 1-N / N-N    │
│                       ⚠️  active vs historical       │
│  6. IDENTITY        → EVERY entity: assigned (A)     │
│                                     vs derived (D)   │
└──────────────────────────────────────────────────────┘
```

## Universal probing pattern (for any new domain not listed)

```
Q1 (transaction): "When [actor] does X with [thing], do we
                  record a separate entity for that event?"
Q2 (metadata):    "Does it carry timestamps, status, amount,
                  expiry? → join entity."
Q3 (policy):      "Is there a rule (price, eviction, expiry,
                  allocation) that varies? → policy entity."
```

These 3 questions surface ~80% of hidden entities.

---

## Common traps (quick recap)

1. **Entity vs attribute** — if it has its own ID + lifecycle, it's an entity. Else it's an attribute.
2. **Missing join entity in N-N** — when the relationship carries metadata (issuedAt, amount, status), make it a join entity (`Loan`, `Enrollment`, `Split`).
3. **Snapshot vs history** — if audit/history matters, you need an event entity (`Move`, `OrderStatusEvent`, `ClickEvent`).
4. **1-1 active vs 1-N historical** — the #1 missed nuance. Always ask: "many at once, or many over time?"
5. **Container ≠ building** — it can be `Conversation`, `OrderBook`, `Bucket`, `Group`. Anything with capacity/state.
6. **Missing entities across steps** — if an entity appears in Step 1/2/3, it MUST appear in Step 6 (identity). Every meaningful pair MUST appear in Step 5 (cardinality). Cross-check before moving on.

