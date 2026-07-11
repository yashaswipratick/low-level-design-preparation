package com.lld.practice.tutor_sessions.parking_lot.implementation;

import com.lld.practice.tutor_sessions.parking_lot.design_patterns.decorator.SurgePricingDecorator;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.allocator.impl.FirstAvailableAllocator;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.allocator.impl.NearestEntranceAllocator;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.strategy.pricing.impl.FlatHourlyPricingStrategy;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.*;
import com.lld.practice.tutor_sessions.parking_lot.implementation.service.ParkingLotService;
import com.lld.practice.tutor_sessions.parking_lot.implementation.service.impl.ParkingLotServiceImpl;

import java.util.Arrays;
import java.util.List;

/**
 * Stage 2 demo — multi-floor parking lot with vehicle types and lost-ticket flow.
 * Run: main() should print all PASS lines and exit 0.
 */
public class ParkingLotDemo {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        happyPath_car();
        happyPath_bike();
        happyPath_slotReusedAfterExit();
        entry_throws_whenLotFullForType();
        entry_throws_wrongVehicleTypeForSlot();
        pay_throws_whenAlreadyPaid();
        exit_throws_whenNotPaid();
        exit_throws_forUnknownTicket();
        lostTicket_fullFlow();
        lostTicket_cannotPayNormallyAfterReportLost();
        surge_pricing_chargesMultiplier();
        nearestEntrance_picksLowestSlotNumber();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }

    // ---------- helpers ----------

    /**
     * Builds a 2-floor lot:
     *   Floor 1: carSlots CAR slots + bikeSlots BIKE slots
     *   Floor 2: truckSlots TRUCK slots
     */
    static ParkingLotService buildLot(int carSlots, int bikeSlots, int truckSlots) {
        // Floor 1 — car + bike slots
        List<Slot> f1Slots = new java.util.ArrayList<>();
        for (int i = 1; i <= carSlots;  i++) f1Slots.add(new Slot(i,               SlotStatus.FREE, SlotType.CAR));
        for (int i = 1; i <= bikeSlots; i++) f1Slots.add(new Slot(100 + i,          SlotStatus.FREE, SlotType.BIKE));
        Floor floor1 = new Floor(1, f1Slots);

        // Floor 2 — truck slots
        List<Slot> f2Slots = new java.util.ArrayList<>();
        for (int i = 1; i <= truckSlots; i++) f2Slots.add(new Slot(200 + i,         SlotStatus.FREE, SlotType.TRUCK));
        Floor floor2 = new Floor(2, f2Slots);

        ParkingLot lot = new ParkingLot("LOT-1", Arrays.asList(floor1, floor2));
        return new ParkingLotServiceImpl(lot);
    }

    // ---------- tests ----------

    static void happyPath_car() {
        ParkingLotService svc = buildLot(2, 2, 1);
        Ticket t = svc.entry("KA-01-AB-1234", VehicleType.CAR);
        expect("car entry issues ticket",   t != null && t.getStatus() == TicketStatus.ISSUED);
        expect("car entry sets entryTime",  t.getEntry() != null);
        expect("car entry assigns slot",    t.getSlotNumber() != null);

        Ticket paid = svc.pay(t.getTicketId());
        expect("car pay -> PAID",           paid.getStatus() == TicketStatus.PAID);
        expect("car pay computes fee",      paid.getFee() >= 20.0);   // min 1 hour × ₹20

        svc.exit(t.getTicketId());
        expect("car exit -> EXITED",        t.getStatus() == TicketStatus.EXITED);
    }

    static void happyPath_bike() {
        ParkingLotService svc = buildLot(1, 2, 1);
        Ticket t = svc.entry("BIKE-001", VehicleType.BIKE);
        expect("bike entry issues ticket",  t != null && t.getStatus() == TicketStatus.ISSUED);

        svc.pay(t.getTicketId());
        expect("bike pay -> PAID",          t.getStatus() == TicketStatus.PAID);
        expect("bike fee < car fee",        t.getFee() < 20.0);       // ₹10/hr

        svc.exit(t.getTicketId());
        expect("bike exit -> EXITED",       t.getStatus() == TicketStatus.EXITED);
    }

    static void happyPath_slotReusedAfterExit() {
        ParkingLotService svc = buildLot(1, 0, 0);   // 1 CAR slot only
        Ticket t1 = svc.entry("V1", VehicleType.CAR);
        svc.pay(t1.getTicketId());
        svc.exit(t1.getTicketId());
        Ticket t2 = svc.entry("V2", VehicleType.CAR);   // should succeed — slot freed on exit
        expect("slot reused after exit",    t2 != null && t2.getSlotNumber().equals(t1.getSlotNumber()));
    }

    static void entry_throws_whenLotFullForType() {
        ParkingLotService svc = buildLot(1, 0, 0);   // only 1 CAR slot
        svc.entry("V1", VehicleType.CAR);
        expectThrows("entry throws when CAR lot full", IllegalStateException.class,
                () -> svc.entry("V2", VehicleType.CAR));
    }

    static void entry_throws_wrongVehicleTypeForSlot() {
        ParkingLotService svc = buildLot(0, 1, 0);   // only BIKE slot, no CAR
        expectThrows("entry throws when no CAR slot available", IllegalStateException.class,
                () -> svc.entry("CAR-001", VehicleType.CAR));
    }

    static void pay_throws_whenAlreadyPaid() {
        ParkingLotService svc = buildLot(1, 0, 0);
        Ticket t = svc.entry("V1", VehicleType.CAR);
        svc.pay(t.getTicketId());
        expectThrows("pay throws when already PAID", IllegalStateException.class,
                () -> svc.pay(t.getTicketId()));
    }

    static void exit_throws_whenNotPaid() {
        ParkingLotService svc = buildLot(1, 0, 0);
        Ticket t = svc.entry("V1", VehicleType.CAR);
        expectThrows("exit throws when ISSUED (not paid)", IllegalStateException.class,
                () -> svc.exit(t.getTicketId()));
    }

    static void exit_throws_forUnknownTicket() {
        ParkingLotService svc = buildLot(1, 0, 0);
        expectThrows("exit throws on unknown ticketId", IllegalArgumentException.class,
                () -> svc.exit("bogus-id"));
    }

    static void lostTicket_fullFlow() {
        ParkingLotService svc = buildLot(1, 0, 0);
        Ticket t = svc.entry("V1", VehicleType.CAR);

        svc.reportLost(t.getTicketId());
        expect("reportLost -> LOST",             t.getStatus() == TicketStatus.LOST);

        Ticket paid = svc.payLostTicketPenalty(t.getTicketId());
        expect("payLostPenalty -> PAID",          paid.getStatus() == TicketStatus.PAID);
        expect("payLostPenalty sets penalty fee", paid.getFee() == 500.0);

        svc.exit(t.getTicketId());
        expect("exit after lost flow -> EXITED",  t.getStatus() == TicketStatus.EXITED);

        // slot should now be free — park another car
        Ticket t2 = svc.entry("V2", VehicleType.CAR);
        expect("slot freed after lost exit",      t2 != null);
    }

    static void lostTicket_cannotPayNormallyAfterReportLost() {
        ParkingLotService svc = buildLot(1, 0, 0);
        Ticket t = svc.entry("V1", VehicleType.CAR);
        svc.reportLost(t.getTicketId());
        expectThrows("pay() rejects LOST ticket (must use payLostPenalty)", IllegalStateException.class,
                () -> svc.pay(t.getTicketId()));
    }

    // ---------- tiny assert helpers ----------

    static void surge_pricing_chargesMultiplier() {
        List<Slot> f1Slots = new java.util.ArrayList<>();
        for (int i = 1; i <= 2; i++) f1Slots.add(new Slot(i, SlotStatus.FREE, SlotType.CAR));
        ParkingLot lot = new ParkingLot("LOT-SURGE", Arrays.asList(new Floor(1, f1Slots)));

        ParkingLotService svc = new ParkingLotServiceImpl(lot,
                new SurgePricingDecorator(new FlatHourlyPricingStrategy(), 1.5),
                new FirstAvailableAllocator());

        Ticket t = svc.entry("SURGE-CAR", VehicleType.CAR);
        Ticket paid = svc.pay(t.getTicketId());
        // min 1 hr × ₹20 × 1.5 = ₹30
        expect("surge pricing charges 1.5x multiplier", paid.getFee() >= 30.0);
    }

    static void nearestEntrance_picksLowestSlotNumber() {
        List<Slot> f1Slots = new java.util.ArrayList<>();
        // Add slots in reverse order to ensure allocator sorts correctly
        f1Slots.add(new Slot(5, SlotStatus.FREE, SlotType.CAR));
        f1Slots.add(new Slot(3, SlotStatus.FREE, SlotType.CAR));
        f1Slots.add(new Slot(1, SlotStatus.FREE, SlotType.CAR));
        ParkingLot lot = new ParkingLot("LOT-NEAREST", Arrays.asList(new Floor(1, f1Slots)));

        ParkingLotService svc = new ParkingLotServiceImpl(lot,
                new FlatHourlyPricingStrategy(),
                new NearestEntranceAllocator());

        Ticket t = svc.entry("NEAR-CAR", VehicleType.CAR);
        expect("nearest entrance picks slot 1", t.getSlotNumber() == 1);
    }


    static void expect(String name, boolean cond) {
        if (cond) { System.out.println("PASS: " + name); passed++; }
        else      { System.out.println("FAIL: " + name); failed++; }
    }

    static void expectThrows(String name, Class<? extends Throwable> expected, Runnable r) {
        try {
            r.run();
            System.out.println("FAIL: " + name + " (no exception thrown)");
            failed++;
        } catch (Throwable ex) {
            if (expected.isInstance(ex)) { System.out.println("PASS: " + name); passed++; }
            else { System.out.println("FAIL: " + name + " (got " + ex.getClass().getSimpleName() + ": " + ex.getMessage() + ")"); failed++; }
        }
    }
}
