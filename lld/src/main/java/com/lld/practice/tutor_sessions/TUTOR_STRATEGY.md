# Tutor Strategy — Progressive LLD (HLD-style staging applied to LLD)

> **Core idea:** Same as HLD ("design for 1 user → 100 → 10K → 1M"), but for LLD we stage by **complexity dimensions**, not user count. Each stage forces a new pattern, algorithm, or concurrency concern.

---

## The 5 Stages (per domain)

```
┌────────────────────────────────────────────────────────────────────┐
│ STAGE 1  MVP — Happy Path                                          │
│   - 1 actor, 1 op, no edge cases                                   │
│   - Procedural code, in-memory, single-threaded                    │
│   - Just entities + 1 service method                               │
│   - Goal: prove the domain model works                             │
│                                                                     │
│ STAGE 2  REAL DOMAIN — Multiple types + state machine              │
│   - Add sub-types (vehicle types, room types, …)                   │
│   - Introduce enum + state transitions                             │
│   - Patterns enter: Factory (sub-types), Strategy (1 policy)       │
│                                                                     │
│ STAGE 3  POLICIES VARY — Pluggable behavior                        │
│   - "What if pricing is per-min vs flat vs surge?"                 │
│   - "What if allocation is nearest vs best-fit?"                   │
│   - Patterns: Strategy (multiple), Decorator (chained policies)    │
│                                                                     │
│ STAGE 4  CONCURRENCY — Multiple actors simultaneously              │
│   - "Two cars at gate, last slot — who wins?"                      │
│   - Locks, CAS, atomic counters, per-key locks                     │
│   - Idempotency keys, dedup tables                                 │
│   - Patterns: Singleton (registry), thread-safe collections        │
│                                                                     │
│ STAGE 5  FAILURE + EXTENSIBILITY — Production-grade                │
│   - Partial failure (crash mid-op) → saga / sweeper / reconcile    │
│   - Cross-cutting: notifications, audit, metrics → Observer        │
│   - New requirement: "add EV charging slots" → prove extensibility │
│   - Patterns: Observer, Chain of Responsibility, Template Method   │
└────────────────────────────────────────────────────────────────────┘
```

---

## Session Format (per stage)

For every stage, we run this loop:

1. **Tutor asks 1 focused question** (Pillar-based).
2. **You answer** in the session file.
3. **I give the ideal answer + tradeoffs.**
4. **You code it.** (skeleton only — enums, entities, 1–2 service methods)
5. **I review** — call out missing pieces / better naming / pattern fit.
6. **Tutor drops the next "what if…"** → forces refactor → next stage.

Each stage = ~20–30 min. Full domain = ~2 hours over multiple sittings.

---

## Stage → Pattern / Concept Mapping (cheat-sheet)

| Stage | Triggered by question | Pattern / concept introduced |
|---|---|---|
| 1 → 2 | "What if there are 3 vehicle types?" | **enum + Factory**, polymorphism |
| 1 → 2 | "What's the ticket's lifecycle?" | **State machine** (enum + transition table) |
| 2 → 3 | "What if pricing varies — flat / hourly / surge?" | **Strategy** + interface |
| 2 → 3 | "What if there are 5 ways to pick a slot?" | **Strategy** (allocator) |
| 3 → 3 | "What if we stack discount + tax + surge?" | **Decorator** chain |
| 3 → 3 | "What if validation has 5 rules in order?" | **Chain of Responsibility** |
| 3 → 4 | "Two cars at the gate for the last slot?" | **Atomic CAS / row lock / per-key lock** |
| 3 → 4 | "Same payment retried twice?" | **Idempotency key + dedup table** |
| 3 → 4 | "10K cars/sec across 100 floors?" | **Sharded locks**, ConcurrentHashMap, striped locks |
| 4 → 5 | "Crash between payment and ticket update?" | **Saga / compensating txn / reconciliation sweeper** |
| 4 → 5 | "Send SMS + email + push on exit?" | **Observer / event bus** |
| 4 → 5 | "Now add EV slots without touching old code?" | **Open-Closed**, plug new Strategy/Factory |
| any | "Avoid memory leaks under load?" | bounded queue, weak refs, eviction policy |

---

## Concrete example: Parking Lot, all 5 stages

| Stage | Requirement added | What you build |
|---|---|---|
| 1 | "1 lot, 1 type, park & leave, no money" | `Slot`, `Vehicle`, `ParkingLot.park()` / `leave()` |
| 2 | "Add Car / Bike / Truck; ticket has states" | Enum `VehicleType`, `Slot` typed, `Ticket` + state enum, `VehicleFactory` |
| 3 | "Pricing differs: flat / hourly / weekend surge. Allocator: nearest / best-fit." | `PricingStrategy`, `SlotAllocator` interfaces; inject via constructor |
| 4 | "Multi-threaded entries; idempotent payments; avoid double-park" | per-slot lock OR CAS; `requestId` dedup table; `AtomicReference<Status>` on Slot |
| 5 | "Crash after payment, no exit recorded; also notify SMS+email; also add EV slots" | Reconciliation sweeper job; `ExitObserver` interface w/ Sms+Email impls; new `EVSlot` + `EVPricingStrategy` — zero changes to old code |

You'll see: **same domain, code grows by composition, never by rewriting.** That's the whole point of LLD patterns.

---

## Rules for our sessions

1. **Don't skip stages.** Even if you know the final design, build Stage 1 first. Muscle memory matters in interviews.
2. **Code after every stage.** Words → code → review → next stage.
3. **One file per domain** in `tutor_sessions/`. We append as we progress, so revision later is linear.
4. **Track patterns introduced** at the bottom of each session file — you'll have a personal "patterns I've actually used" list, much stronger than memorizing GoF.
5. **End every domain with a 5-min recap** — what was Stage 1 vs final? Which "what if…" forced which pattern? This is the story you tell in interviews.

---

## Tomorrow's plan

- Start with **Parking Lot** (most common interview problem, all 5 stages naturally apply).
- I'll ask Stage-1 question → you answer in `01_parking_lot.md` → you code Stage-1 skeleton → review → Stage-2 question → repeat.
- Target: finish Stage 1+2+3 tomorrow (~90 min). Stage 4+5 next day.

Then we'll do **Movie Booking** (Stage 4 — concurrency — is the star here) and **Splitwise** (Stage 3 — strategies — is the star) to cover the other muscle groups.

Ready when you are.

