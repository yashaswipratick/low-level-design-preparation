# Parking Lot Stage Status

## Progress

- [x] Stage 1 - MVP
- [x] Stage 2 - Types + State Machine ✅ (22/22 tests pass)
- [ ] Stage 3 - Policies (Strategy) ← current
- [ ] Stage 4 - Concurrency
- [ ] Stage 5 - Failure + Extensibility

## Stage 2 Completed

- `VehicleType` (CAR, BIKE, TRUCK) + `SlotType` (CAR, BIKE, TRUCK)
- Compatibility map: `CAR → CAR_SLOT`, `BIKE → BIKE_SLOT`, `TRUCK → TRUCK_SLOT`
- Multi-floor: `ParkingLot → Floor → Slot` composition tree
- Per-type hourly rates: BIKE ₹10, CAR ₹20, TRUCK ₹40 (`FlatHourlyPricingStrategy`)
- `LOST` state: `reportLost()` + `payLostTicketPenalty()` (₹500 flat fee)
- Factories: `VehicleFactory`, `TicketFactory`
- All state guards + slot release side-effects working

## Stage 3 Focus

- Pluggable pricing policies (Strategy pattern already stubbed in)
- Slot allocator strategy (e.g. nearest-floor, EV-priority)
- Per-type rate configuration at construction time

