# Pillar 1 Practice Log — Scope Drills

> Personal practice log: each drill captures your version, the polished/fixed version, and the lesson learned.
> Use this file as a daily reference before any LLD interview.

---

## How to read each drill

- **Problem** — the 1-line statement
- **Your version** — what you submitted
- **Score** — out of 4 (one point per gate)
- **Polished version** — the strong version for each gate
- **Lesson learned** — what to internalize for next time

---

## Drill #1 — Food Delivery Order Tracker

**Problem:** *"Design a food delivery order tracker."*

### Your version
1. Who are the users for food delivery order tracker — user, restaurant, delivery guy
2. What are the operations expected other than order food, track delivery time
3. Where does it run either single user with in-memory or persistence is required
4. What is out of scope

### Score: 2.5 / 4

### Polished version
```
Q1 [WHO]:   Are we designing for multiple actors — customer, restaurant,
            delivery agent — or only one of them?
Q2 [WHAT]:  Beyond placing an order and tracking delivery, do we need
            cancel, refund, rate the order, or order history?
Q3 [WHERE]: Single in-memory app for one user, or shared backend with
            persistence?
Q4 [NOT]:   Is anything explicitly out of scope — auth, payment,
            SMS/notifications, UI, analytics?
```

### Lesson learned
- **Don't answer your own questions.** List options to anchor the interviewer.
- **NOT-scope must list candidates.** "What is out of scope?" alone is too vague.

---

## Drill #2 — URL Shortener

**Problem:** *"Design a URL shortener."*

### Your version
1. What kind of users are we expecting to use this feature?
2. What are operations other than accepting long URLs and processing the URLs and returning the hashed URLs to the end user
3. Will this be for single user in-memory persistent or multi-users with persistence
4. Is anything explicitly out of scope — auth, payment gateway, notification on expiry, analytics

### Score: 3.5 / 4

### Polished version
```
Q1 [WHO]:   Who consumes this — end-users via UI, app services via API,
            or both?
Q2 [WHAT]:  Beyond shorten and redirect, do we need delete a URL,
            set expiry, custom alias, view click count?
Q3 [WHERE]: Should this be a single in-memory app (lost on restart), or
            a multi-user system with persistence (DB-backed)?
Q4 [NOT]:   Out of scope — auth, payment gateway, notifications, analytics?
```

### Lesson learned
- **In-memory ≠ persistent.** Fix vocabulary: in-memory = RAM only, persistent = saved to disk/DB.
- **Always seed examples in WHAT.** Don't just say "any other operations" — list them.

---

## Drill #3 — Vending Machine

**Problem:** *"Design a vending machine."*

### Your version
1. Who are the users other than the buyers, the admin who loads the item on the UI, the vendors who physically load the item in the machine?
2. What are the operations expected beyond scan barcode, choose the item, pay for the item and collect the item — scheduled collections, add items, delete items, check status of items (count how many left)
3. Are we designing this for single user in-memory system or multi-user system with persistence in DB?
4. Is anything explicitly out of scope — authentication, analytics, notification?

### Score: 3.25 / 4

### Polished version
```
Q1 [WHO]:   Who are the actors — is it just the buyer, or also a service
            person who restocks, and an admin who configures items/pricing?
Q2 [WHAT]:  Beyond scan, choose, pay, collect — do we also need refund,
            restock, low-stock alerts, view inventory, change pricing?
Q3 [WHERE]: Single user in-memory, or multi-user with persistence in DB?
Q4 [NOT]:   Out of scope — authentication, payment gateway integration,
            analytics, notifications, remote monitoring?
```

### Lesson learned
- **Money problems = always include payment in NOT-scope.** If money flows, ask about payment gateway upfront.
- **Don't pre-fill answers in WHO.** Say "is it just X" — let interviewer add Y, Z.

---

## Drill #4 — Hotel Room Reservation

**Problem:** *"Design a hotel room reservation system."*

### Your version
1. Who are the actors interacting with the system — is it just for customer trying to do the reservation
2. What are the functionalities we are looking into beyond showing availability, reserving rooms — cancel reservation, add rooms, delete rooms
3. Is this system designed for single user in-memory or multi-user system with persistence
4. Is anything explicitly out of scope — authentication, payment integration, notification, analytics

### Score: 3.25 / 4

### Polished version
```
Q1 [WHO]:   Who are the actors — is it just the customer who books, or
            also a receptionist who confirms walk-ins, and a hotel admin
            who configures rooms and pricing?
Q2 [WHAT]:  Beyond search availability and reserve — do we also need
            cancel, modify (dates/room), check-in/check-out, view booking
            history, or admin room configuration?
Q3 [WHERE]: Single user in-memory or multi-user with persistence?
Q4 [NOT]:   Out of scope — authentication, payment integration,
            notifications, analytics?
```

### Lesson learned
- **Booking/reservation systems live by lifecycle methods.** Always include: create, modify, cancel, check-in, check-out, view history.
- **"Add/delete rooms" is admin-side**, not customer-side. Distinguish actor-specific operations.

---

## Drill #5 — In-Memory Key-Value Store

**Problem:** *"Design an in-memory key-value store (like a mini Redis)."*

### Your version
1. What kind of users are we expecting to use the system — application with high availability?
2. What are the operations beyond — add(), remove(), update() — eviction should be decided based on LRU or LFU policy, what will be the % score for eviction
3. Is this designed for single user in-memory or multi user system with persistence
4. Is explicitly out of scope — authentication, analytics, server liveness check

### Score: 2.0 / 4 ⚠️ (worst drill)

### Polished version
```
Q1 [WHO]:   Who consumes this KV store — application services via API,
            developers via a CLI, or both?
Q2 [WHAT]:  Beyond put / get / delete, do we need update, list keys,
            TTL/expiry, batch get/put, or clear all?
Q3 [WHERE]: Single user in-memory or multi-user system with persistence?
Q4 [NOT]:   Out of scope — authentication, persistence to disk (snapshots),
            replication, networking/RPC, sharding, analytics?
```

### Lesson learned (CRITICAL)
- **Stay disciplined per pillar.** Eviction (LRU/LFU) and "% score" belong to Pillar 4 (Rules), NOT Gate 2 (Operations).
- **Don't miss obvious verbs.** `get()` is the #1 KV operation — never forget it.
- **Pillar 1 is JUST scope.** No pricing, no algorithms, no policy decisions.

---

## Drill #6 — Tic-Tac-Toe Game

**Problem:** *"Design a tic-tac-toe game."*

### Your version
1. Is this design for single or multi-players or both?
2. What are the operations beyond — calculating score for both players, deciding winners, running timers — add player, delete player, match player
3. Is this design for single user in-memory or multi-user system with persistence
4. Is explicitly out of scope — authentication, analytics, notification

### Score: 2.5 / 4

### Polished version
```
Q1 [WHO]:   Who are the actors — two human players, human vs AI, or also
            spectators?
Q2 [WHAT]:  Beyond making a move and detecting winner — do we need start
            new game, restart, undo last move, show board, or save/replay
            game history?
Q3 [WHERE]: Single user in-memory or multi-user with persistence?
Q4 [NOT]:   Out of scope — UI/rendering, AI difficulty levels, save/replay
            history, multiplayer matchmaking, tournament tracking,
            authentication?
```

### Lesson learned
- **Games use game-mechanic verbs**, not CRUD verbs.
- Game verbs: `start`, `move`, `win/draw`, `restart`, `undo`, `show board`.
- Tic-tac-toe has **no score**, just outcome. No timers by default.
- Don't apply "add/delete/match player" to games unless it's a tournament system.

---

## Drill #7 — ATM System

**Problem:** *"Design an ATM system."*

### Your version
1. Is this designed for users to withdraw money, bank users to add money, admin to monitor the transaction flow
2. What kind of operations are we supporting beyond withdrawing money, adding money to ATM, debit account and credit account — deposit machine
3. Is this design for single user in-memory or multi-user system with persistence
4. Is explicitly out of scope — authentication, analytics, notification

### Score: 3.0 / 4

### Polished version
```
Q1 [WHO]:   Customers withdraw/deposit, bank staff refills cash, admin
            monitors transactions — are all three in scope?
Q2 [WHAT]:  Beyond cash withdraw and deposit — do we need balance inquiry,
            transfer between accounts, change PIN, mini-statement, or
            print receipt?
Q3 [WHERE]: Single user in-memory or multi-user with persistence?
Q4 [NOT]:   Out of scope — hardware (cash dispenser, card reader, printer),
            bank-backend API integration, multi-currency support, fraud
            detection, analytics?
```

### Lesson learned
- **For banking/ATM, auth (PIN) is CORE, not out-of-scope.** Don't blindly add auth to NOT-scope.
- **User-facing verbs ≠ internal mechanism.** Customer says "withdraw"; system does "debit". Ask in customer-facing terms.
- **ATM has hardware boundary** — clarify if card reader, dispenser, printer are mocked or real.

---

## Drill #8 — Online Learning Platform (mini Coursera)

**Problem:** *"Design an online learning platform (like a mini Coursera)."*

### Your version
1. Who are we designing for the students, instructors or admin part of the system?
2. What are the operations beyond buy course, track progress, video streaming — upload course, delete course, update course
3. Is this design for single user in-memory or multi-user system with persistence
4. Is explicitly out of scope — authentication, analytics, notification, payment integration

### Score: 3.0 / 4

### Polished version
```
Q1 [WHO]:   Are we designing for students, instructors, and admin — or
            only one of them?
Q2 [WHAT]:  Core verbs: enroll, watch, complete lesson. Beyond these:
            - For students: search/browse courses, take quiz, submit
              assignment, get certificate, rate course?
            - For instructors: upload, update, delete course content,
              view course stats?
Q3 [WHERE]: Single user in-memory or multi-user with persistence?
Q4 [NOT]:   Out of scope — authentication, payment integration, video
            streaming/CDN infrastructure, DRM, live classes/webinars,
            recommendation engine, mobile sync, analytics?
```

### Lesson learned (CRITICAL for multi-actor systems)
- **Organize WHAT verbs by actor.** When 3+ actors exist, list verbs per actor.
  - Student verbs: enroll, browse, learn, quiz, certificate
  - Instructor verbs: create, upload, update, delete, view stats
  - Admin verbs: approve, suspend, configure platform
- **Don't mix actor-specific verbs.** "Upload course" is instructor-only; "buy course" is student-only.
- **NOT-scope for content platforms:** always probe video infra, DRM, recommendations.

---

## Drill #9 — Twitter/X-Like Social Feed

**Problem:** *"Design a Twitter/X-like social feed."*

### Your version
1. Who are we designing the system for are users of X, Admin
2. What are the operation beyond follower relationships, public/private feeds, content ops
3. Is this design for single user in-memory or multi-user system with persistence
4. Is explicitly out of scope — authentication, analytics, notification

### Score: 2.5 / 4 ⚠️ (regression)

### Polished version
```
Q1 [WHO]:   Who are the actors — anonymous viewers, logged-in users
            (who post and read), content moderators, platform admins,
            advertisers? Or just one role?
Q2 [WHAT]:  Core verbs: post, follow, view feed. Beyond these:
            - Engagement: like, repost/retweet, reply, bookmark?
            - Relationship: unfollow, block, mute?
            - Content: edit/delete post, attach media?
            - Discovery: search hashtags, trending, view profile?
            - Messaging: send DMs?
Q3 [WHERE]: Single user in-memory or multi-user with persistence?
Q4 [NOT]:   Out of scope — authentication, image/video upload infra,
            trending/recommendation algorithm, real-time push,
            spam/abuse detection, moderation pipeline, ad serving,
            analytics?
```

### Lesson learned (CRITICAL — discipline issue)
- **Always run verb sweep**, even when tired. Don't rephrase hint categories — list actual verbs.
- **Social platforms = engagement verb explosion.** Categorize verbs into:
  - Content (post, edit, delete)
  - Engagement (like, share, comment, bookmark)
  - Relationship (follow, block, mute)
  - Discovery (search, feed, trending)
  - Messaging (DM)
- **Never skip the obvious.** Twitter without `like` / `retweet` = ATM without `withdraw`.
- **Same 3 NOT-scope items every drill = lazy.** Tailor candidates per domain.

---

## Overall Performance Summary

| Drill | Problem | Score |
|---|---|---|
| 1 | Food Delivery Tracker | 2.5 / 4 |
| 2 | URL Shortener | 3.5 / 4 |
| 3 | Vending Machine | 3.25 / 4 |
| 4 | Hotel Reservation | 3.25 / 4 |
| 5 | KV Store | 2.0 / 4 ⚠️ |
| 6 | Tic-Tac-Toe | 2.5 / 4 |
| 7 | ATM System | 3.0 / 4 |
| 8 | Online Learning Platform | 3.0 / 4 |
| 9 | Twitter/X Social Feed | 2.5 / 4 ⚠️ |

**Average: 2.83 / 4 = 71%**

---

## Patterns To Internalize (memorize these)

### ✅ Strengths developed
1. Always firing all 4 gates in order
2. Tagging questions with `[WHO]`, `[WHAT]`, `[WHERE]`, `[NOT]`
3. Confirming in-memory vs persistence consistently
4. Listing options in NOT-scope (most of the time)

### ⚠️ Common pitfalls (still to fix)
1. **Pillar leakage** — pricing/policy/algorithms must NOT appear in Pillar 1 questions. Park them for Pillar 4.
2. **Domain-aware verbs** — game verbs ≠ CRUD verbs; banking includes auth as core, not extra.
3. **Skipping obvious verbs** — `get()` for KV, `balance inquiry` for ATM, `check-in` for hotel.
4. **Listing 1 actor in WHO** — always propose 2–3 candidates.
5. **Generic NOT-scope** — domain-specific candidates (hardware, replication, AI levels) are more impressive than generic auth/notification.

---

## Domain-Specific Cheats (quick lookup before any drill)

| Domain | Always include in NOT-scope | Auth status |
|---|---|---|
| Booking systems (hotel, flight, movie) | Payment, notifications | Optional |
| Library | Payment/fines gateway, OCR for ISBN, reservation queue policy | Optional |
| Parking lot | Hardware (sensors, gate motors), payment gateway, license-plate OCR | Optional |
| URL shortener | Custom domains, SSL termination, analytics dashboard, abuse/spam detection | Optional |
| ATM / Banking / Wallet | Hardware, bank API, fraud | **CORE — not optional** |
| Vending / POS | Payment gateway, hardware | Often core |
| KV store / Cache / DB | Persistence to disk, replication, RPC | Optional |
| Chat / Messaging | E2E encryption, voice/video | Optional |
| Games | UI/rendering, AI, replay | Optional |
| Tournament systems | Matchmaking, leaderboards | Often core |
| Learning platforms (Coursera-like) | Video CDN, DRM, recommendations | Optional |
| Social feeds (Twitter/X-like) | Trending algo, media upload, push, spam, ads | Optional |
| Food delivery | Maps/geo, payment gateway, push notifications, ETA prediction | Optional |
| E-commerce / Cart | Payment gateway, search ranking, recommendations, fraud | Optional |
| Chess | UI/rendering, AI engine, ELO/rating, replay/PGN export | Optional |
| Snake & Ladder | UI/rendering, AI opponent, network multiplayer | Optional |
| Elevator | Hardware (motors, sensors), emergency call, voice announce | Optional |
| Splitwise / Expense | Payment settlement (real money), currency conversion, OCR receipts | Optional |
| Notification System | Provider integrations (FCM/APNS/Twilio), templating engine, A/B test | Optional |
| File Storage (Dropbox) | File diff/delta sync, encryption, virus scan, preview generation | Optional |
| Stock Exchange | Market data feed, regulatory reporting, fraud, settlement clearing | **CORE auth** |
| Logger | Network sinks, log aggregation/search, retention policy, alerting | Optional |
| Rate Limiter | Distributed sync (Redis), monitoring/dashboard, dynamic config | Optional |
| Calendar | Email/SMS reminders, third-party sync (Google/Outlook), timezone DST edge | Optional |
| Music / Video streaming | CDN/encoding, DRM, recommendations, offline download, ad insertion | Optional (subscription = core) |
| Airbnb | Maps/geo, payment gateway, identity verification, messaging | Optional |
| Ride-sharing | Maps/geo, ETA/routing, payment gateway, surge pricing model | Optional |

| Domain | Visible verbs (obvious / mentioned) | Hidden verbs (interviewer expects you to surface) |
|---|---|---|
| CRUD systems | add, update, delete, list | search, pagination, filter/sort, soft-delete vs hard-delete, bulk ops |
| Library | add, search, mark issued/available | renew, reserve/hold, return, fines, history, late-fee |
| Parking lot | park, exit | find slot, calculate fee, **lost ticket**, monthly pass, slot allocation policy |
| Movie booking | book, view shows | hold seat (with timeout), cancel/refund, seat selection, view bookings, group bookings |
| Booking (hotel/flight) | reserve, cancel | **modify dates/room**, check-in / check-out, view history, no-show handling, waitlist |
| Games | start, move, check-winner | **restart, undo, show board**, save/replay history, draw detection, AI difficulty |
| KV store | put, delete, update | **get** ⚠️ (often missed), TTL/expiry, batch get/put, list keys, clear all, exists |
| ATM | withdraw, deposit | **balance inquiry** ⚠️, transfer, change PIN, mini-statement, print receipt, card eject |
| Vending | scan, choose, pay, dispense | **refund**, restock, low-stock alerts, change pricing, view inventory |
| Chat / Messaging | send, receive | **edit, delete**, read receipts, typing indicator, attachments, search, react |
| Learning platform | enroll, watch, complete lesson | quiz, **submit assignment**, get certificate, rate/review, upload/update/delete (instructor), view progress |
| Social feed | post, follow, view feed | **like, repost, reply, bookmark**, unfollow, block, mute, edit/delete post, search hashtags, trending, view profile, DM |
| URL shortener | shorten, redirect | **delete**, set expiry, custom alias, view click count, list user's URLs |
| Food delivery | place order, track | cancel, refund, rate, order history, reorder, schedule delivery |
| E-commerce / Cart | browse, add to cart, checkout | **wishlist**, apply coupon, save for later, order history, return/refund, track shipment, write review |
| Chess | start, move | **resign, draw offer, undo (takeback)**, save/replay (PGN), promote pawn, castling, en-passant, check/checkmate detection |
| Snake & Ladder | roll dice, move | start game, restart, **detect win**, multi-player turn, view leaderboard |
| Elevator | call, go to floor | **emergency stop, door open/close, overload alert**, maintenance mode, schedule (express/local) |
| Splitwise / Expense | add expense, view balance | **settle up**, edit/delete expense, group invite, currency convert, recurring expense, simplify debts |
| Notification System | send, deliver | **schedule, retry on failure, batch send**, subscribe/unsubscribe, mark read, multi-channel fan-out |
| File Storage (Dropbox) | upload, download | **share/unshare**, version history, restore, move/rename, search, sync conflict resolve, set permissions |
| Stock Exchange | place order, view price | **cancel order, modify order**, view order book, view portfolio, market vs limit order, view trade history |
| Logger | log message | **set log level, add appender/filter**, rotate log file, format message, async flush |
| Rate Limiter | allow/deny request | **refill bucket, get remaining quota**, configure rule per client, reset, view stats |
| Calendar | create event | **edit/cancel event**, invite attendees, RSVP, recurring event, set reminder, view by day/week/month, find free slot |
| Music streaming | play song | **pause/skip/seek, create playlist, add to playlist**, like, search, follow artist, download offline, shuffle, recommendations |
| Video streaming | play video | **pause/seek, resume from position, add to watchlist**, mark watched, search, browse by genre, download offline, multi-profile |
| Airbnb | search, book listing | **cancel booking, message host, leave review**, list a property (host side), view booking history, modify dates |
| Ride-sharing | request ride, track | **cancel ride, rate driver, tip**, schedule ride, choose ride type (pool/premium), view trip history, share ETA |

> **How to read this table:**
> - **Visible verbs** = mentioned in the problem statement, easy to spot.
> - **Hidden verbs** = NOT in the problem statement but real-world users expect them. Interviewer tests if you surface them.
> - **⚠️ marked verbs** = high-value verbs that candidates routinely forget. Always probe these.

---

## Next steps

- [x] Drill 1–8 complete
- [ ] Continue drills until consistent 3.5+ / 4
- [ ] Then move to **Pillar 2: Entities & Cardinality**
- [ ] Then Pillars 3 → 4 → 5 → 6 in sequence
- [ ] Final mock: full 6-pillar clarification in 5 minutes

