# Parking Lot — Progressive LLD Workspace

> Following the 5-stage strategy in `../TUTOR_STRATEGY.md`.

## Layout

```
parking_lot/
├── README.md           ← this file
├── SESSION.md          ← Q&A log (tutor questions, your answers, ideal answers)
├── status.md           ← stage progress tracker (Stage 2 now added)
├── stage_1_mvp/        ← happy path: 1 lot, 1 vehicle type, no money
├── stage_2_types/      ← + vehicle/slot types, ticket state machine
├── stage_3_policies/   ← + pricing / allocator Strategy
├── stage_4_concurrency/← + locks, CAS, idempotency
└── stage_5_failure/    ← + saga / observer / extensibility (EV slots)
```

## Flow per stage

1. Check stage state in `status.md`.
2. Read tutor question in `SESSION.md` → write your answer.
3. I post the ideal answer + tradeoffs.
4. You code skeleton in `stage_N_*/` (Java).
5. Review → next stage.

## Patterns tracker (filled as we go)

- [ ] Stage 1 — (none, just procedural)
- [ ] Stage 2 — enum, Factory, State machine
- [ ] Stage 3 — Strategy, Decorator
- [ ] Stage 4 — CAS / per-key lock, idempotency dedup, Singleton registry
- [ ] Stage 5 — Observer, Saga / sweeper, Open-Closed extension
