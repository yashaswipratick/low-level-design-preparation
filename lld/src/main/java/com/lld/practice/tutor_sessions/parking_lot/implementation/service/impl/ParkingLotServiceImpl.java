package com.lld.practice.tutor_sessions.parking_lot.implementation.service.impl;

import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.ticket.TicketFactory;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.Vehicle;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.VehicleFactory;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.allocator.SlotAllocator;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.allocator.impl.FirstAvailableAllocator;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.pricing.PricingStrategy;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.pricing.impl.FlatHourlyPricingStrategy;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.*;
import com.lld.practice.tutor_sessions.parking_lot.implementation.service.ParkingLotService;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

public class ParkingLotServiceImpl implements ParkingLotService {

    private static final double LOST_PENALTY = 500.0;

    private final ParkingLot parkingLot;                                   // full hierarchy
    private final Map<String, Ticket> ticketsById = new ConcurrentHashMap<>();
    private final Map<Integer, Slot> slotsByNumber = new HashMap<>();      // flat map for O(1) lookup in exit()
    private final Map<VehicleType, SlotType> vehicleToSlot = new EnumMap<>(VehicleType.class);

    private final PricingStrategy pricingStrategy;
    private final SlotAllocator slotAllocator;
    
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Convenience constructor: defaults to FlatHourlyPricingStrategy.
     */
    public ParkingLotServiceImpl(ParkingLot parkingLot) {
        this(parkingLot, new FlatHourlyPricingStrategy(), new FirstAvailableAllocator());
    }

    /**
     * Primary constructor: accepts a fully-built ParkingLot (with floors and typed slots).
     * The flat slotsByNumber map is built by flattening all floors — used for O(1) slot lookup.
     */
    public ParkingLotServiceImpl(ParkingLot parkingLot, PricingStrategy pricingStrategy, SlotAllocator slotAllocator) {
        this.parkingLot = parkingLot;
        this.pricingStrategy = pricingStrategy;
        this.slotAllocator = slotAllocator;
        // flatten ParkingLot → Floor → Slot into the lookup map
        parkingLot.getFloor().forEach(floor ->
                floor.getSlot().forEach(slot -> slotsByNumber.put(slot.getSlotNumber(), slot)));
        seedCompatibility();
    }

    private void seedCompatibility() {
        vehicleToSlot.put(VehicleType.CAR,   SlotType.CAR);
        vehicleToSlot.put(VehicleType.BIKE,  SlotType.BIKE);
        vehicleToSlot.put(VehicleType.TRUCK, SlotType.TRUCK);
    }


    // ---------- Ops ----------

    @Override
    public Ticket entry(String licensePlate, VehicleType vehicleType) {
        lock.lock();
        try {
            if (licensePlate == null) throw new IllegalArgumentException("licensePlate is null");
            if (vehicleType == null)  throw new IllegalArgumentException("vehicleType is null");

            Vehicle vehicle = VehicleFactory.create(vehicleType, licensePlate);
            SlotType needed = vehicleToSlot.get(vehicleType);
            if (needed == null) throw new IllegalArgumentException("Unsupported vehicleType: " + vehicleType);

            Slot slot = findFreeSlotOrThrow(needed);
            slot.setStatus(SlotStatus.OCCUPIED);        // 1 — claim the slot
            try {
                Ticket t = TicketFactory.issueTicket(vehicle, slot.getSlotNumber()); // 2
                ticketsById.put(t.getTicketId(), t);                                  // 3
                return t;
            } catch (RuntimeException e) {
                slot.setStatus(SlotStatus.FREE);         // rollback — release slot on any failure
                throw e;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Ticket pay(String ticketId) {
        lock.lock();
        try {
            Ticket t = requireTicket(ticketId);
            if (t.getStatus() != TicketStatus.ISSUED)
                throw new IllegalStateException("pay requires ISSUED, was: " + t.getStatus());

            LocalDateTime exitTime = now();                        // step 1 — local var, no mutation yet
            double fee = pricingStrategy.calculateFee(t);          // step 2 — risky, but ticket untouched
// ↑ if this throws, ticket is still ISSUED, exitTime=null, fee=0 → clean retry
            t.setExit(exitTime);                                   // step 3 — only mutate after calc succeeds
            t.setFee(fee);                                         // step 4
            t.setStatus(TicketStatus.PAID);
            return t;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void exit(String ticketId) {
        lock.lock();
        try {
            Ticket t = requireTicket(ticketId);
            if (t.getStatus() != TicketStatus.PAID)
                throw new IllegalStateException("exit requires PAID, was: " + t.getStatus());

            t.setStatus(TicketStatus.EXITED);
            slotsByNumber.get(t.getSlotNumber()).setStatus(SlotStatus.FREE); // release slot
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Ticket reportLost(String ticketId) {
        lock.lock();
        try {
            Ticket t = requireTicket(ticketId);
            if (t.getStatus() != TicketStatus.ISSUED)
                throw new IllegalStateException("reportLost requires ISSUED, was: " + t.getStatus());
            t.setStatus(TicketStatus.LOST);
            return t;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Ticket payLostTicketPenalty(String ticketId) {
        lock.lock();
        try {
            Ticket t = requireTicket(ticketId);
            if (t.getStatus() != TicketStatus.LOST)
                throw new IllegalStateException("payLostTicketPenalty requires LOST, was: " + t.getStatus());

            LocalDateTime exitTime = now();   // step 1 — local var, no mutation yet
            double fee = LOST_PENALTY;         // step 2 — flat penalty, always 500, no strategy involved
            t.setExit(exitTime);               // step 3 — only mutate after all locals computed
            t.setFee(fee);                     // step 4
            t.setStatus(TicketStatus.PAID);    // step 5 — point of no return
            return t;
        } finally {
            lock.unlock();
        }
    }

    // ---------- Helpers ----------

    private Slot findFreeSlotOrThrow(SlotType slotType) {
        List<Slot> slots = slotsByNumber.values().stream()
                .filter(s -> s.getStatus() == SlotStatus.FREE && s.getSlotType() == slotType)
                .collect(Collectors.toList());
        return slotAllocator.allocate(slots)
                .orElseThrow(() -> new IllegalStateException("Lot full for slotType: " + slotType));
    }

    private Ticket requireTicket(String id) {
        if (id == null) throw new IllegalArgumentException("ticketId is null");
        Ticket t = ticketsById.get(id);
        if (t == null) throw new IllegalArgumentException("Unknown ticket: " + id);
        return t;
    }
}