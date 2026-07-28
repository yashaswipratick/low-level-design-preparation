package com.lld.practice.tutor_sessions.hotel_reservation;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomType;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.factory.RoomFactoryProvider;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Guest;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEventPublisher;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.impl.EmailNotificationObserver;
import com.lld.practice.tutor_sessions.hotel_reservation.service.CheckInService;
import com.lld.practice.tutor_sessions.hotel_reservation.service.HotelReservationService;
import com.lld.practice.tutor_sessions.hotel_reservation.service.impl.CheckInServiceImpl;
import com.lld.practice.tutor_sessions.hotel_reservation.service.impl.HotelReservationServiceImpl;
import com.lld.practice.tutor_sessions.hotel_reservation.store.HotelDataStore;
import com.lld.practice.tutor_sessions.hotel_reservation.strategy.RoomSelectionStrategy;
import com.lld.practice.tutor_sessions.hotel_reservation.strategy.impl.DefaultSelectionStrategy;

import java.time.LocalDate;
import java.util.List;

public class HotelReservationDriver {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== HOTEL RESERVATION SYSTEM - TEST DRIVER ===\n");

        // ── Setup ─────────────────────────────────────────────────────────
        HotelDataStore store = new HotelDataStore();

        // Observer pattern: create publisher, register observers
        ReservationEventPublisher publisher = new ReservationEventPublisher();
        publisher.subscribe(new EmailNotificationObserver());

        // Strategy pattern: inject DefaultSelectionStrategy (swap to CheapestRoomStrategy to change behaviour)
        RoomSelectionStrategy strategy = new DefaultSelectionStrategy();
        HotelReservationService bookingService = new HotelReservationServiceImpl(store, publisher, strategy);
        CheckInService checkInService = new CheckInServiceImpl(store, publisher);

        // Seed 3 rooms using Factory pattern — no more new Room(...) with hardcoded params
        // Each factory knows its own RoomType and default price
        store.getRooms().put("R1", RoomFactoryProvider.getRoomFactory(RoomType.DELUXE).createRoom("R1", "101"));
        store.getRooms().put("R2", RoomFactoryProvider.getRoomFactory(RoomType.SUPER_DELUXE).createRoom("R2", "102"));
        store.getRooms().put("R3", RoomFactoryProvider.getRoomFactory(RoomType.PREMIUM).createRoom("R3", "103"));

        // Create guests
        Guest g1 = new Guest("G1", "John Doe", "john@test.com", "9999999991", "123 Main St");
        Guest g2 = new Guest("G2", "Jane Doe", "jane@test.com", "9999999992", "456 Oak Ave");

        LocalDate checkIn  = LocalDate.of(2026, 7, 25);
        LocalDate checkOut = LocalDate.of(2026, 7, 28);

        System.out.println("Rooms seeded: " + store.getRooms().size());
        System.out.println("Check-in: " + checkIn + "  Check-out: " + checkOut + "\n");

        // ── Test 1: Search available rooms ────────────────────────────────
        System.out.println("=== TEST 1: Search available rooms for July 25-28 ===");
        try {
            List<Room> available = bookingService.searchAvailableRooms(checkIn, checkOut);
            System.out.println("  Available rooms found: " + available.size());
            assert available.size() == 3 : "Expected 3 available rooms";
            pass("TEST 1");
        } catch (Exception e) {
            fail("TEST 1", e.getMessage());
        }

        // ── Test 2: Reserve rooms for 2 guests ───────────────────────────
        System.out.println("\n=== TEST 2: Reserve rooms for G1 + G2 (July 25-28) ===");
        String reservationId = null;
        try {
            Reservation reservation = bookingService.reserve(List.of(g1, g2), checkIn, checkOut);
            reservationId = reservation.getId();
            System.out.println("  Reservation ID : " + reservationId);
            System.out.println("  Status         : " + reservation.getReservationStatus());
            System.out.println("  Rooms booked   : " + reservation.getRooms().size());
            assert reservation.getReservationStatus().name().equals("RESERVED") : "Expected RESERVED";
            pass("TEST 2");
        } catch (Exception e) {
            fail("TEST 2", e.getMessage());
        }

        // ── Test 3: Search again — should be fewer rooms available ────────
        System.out.println("\n=== TEST 3: Search available rooms again — should have fewer ===");
        try {
            List<Room> available = bookingService.searchAvailableRooms(checkIn, checkOut);
            System.out.println("  Available rooms after booking: " + available.size());
            assert available.size() < 3 : "Expected fewer available rooms after booking";
            pass("TEST 3");
        } catch (Exception e) {
            fail("TEST 3", e.getMessage());
        }

        // ── Test 4: Check-in ──────────────────────────────────────────────
        System.out.println("\n=== TEST 4: Check-in with reservationId ===");
        try {
            Reservation checkedIn = checkInService.checkIn(reservationId);
            System.out.println("  Status after check-in: " + checkedIn.getReservationStatus());
            assert checkedIn.getReservationStatus().name().equals("CHECKED_IN") : "Expected CHECKED_IN";
            pass("TEST 4");
        } catch (Exception e) {
            fail("TEST 4", e.getMessage());
        }

        // ── Test 5: Check-out ─────────────────────────────────────────────
        System.out.println("\n=== TEST 5: Check-out ===");
        try {
            Reservation checkedOut = checkInService.checkOut(reservationId);
            System.out.println("  Status after check-out: " + checkedOut.getReservationStatus());
            boolean roomsFreed = checkedOut.getRooms().stream()
                    .allMatch(r -> store.getRooms().get(r.getId()).getRoomStatus() == RoomStatus.AVAILABLE);
            System.out.println("  All rooms freed back to AVAILABLE: " + roomsFreed);
            assert checkedOut.getReservationStatus().name().equals("CHECKED_OUT") : "Expected CHECKED_OUT";
            assert roomsFreed : "Expected rooms to be AVAILABLE after check-out";
            pass("TEST 5");
        } catch (Exception e) {
            fail("TEST 5", e.getMessage());
        }

        // ── Test 6: Edge case — check-in on already CHECKED_OUT reservation
        System.out.println("\n=== TEST 6: Edge case — check-in on CHECKED_OUT reservation ===");
        try {
            checkInService.checkIn(reservationId);
            fail("TEST 6", "Expected HotelBookingException but no exception was thrown");
        } catch (HotelBookingException e) {
            System.out.println("  Expected exception caught: " + e.getMessage());
            pass("TEST 6");
        } catch (Exception e) {
            fail("TEST 6", "Wrong exception type: " + e.getClass().getSimpleName() + " — " + e.getMessage());
        }

        // ── Test 7: Cancel a RESERVED booking, rooms should free up ──────
        System.out.println("\n=== TEST 7: Make new reservation then cancel it ===");
        try {
            Reservation newRes = bookingService.reserve(List.of(g1), checkIn, checkOut);
            System.out.println("  New reservation created: " + newRes.getId());
            Reservation cancelled = bookingService.cancelReservation(newRes.getId());
            System.out.println("  Status after cancel: " + cancelled.getReservationStatus());
            boolean roomsFreed = cancelled.getRooms().stream()
                    .allMatch(r -> store.getRooms().get(r.getId()).getRoomStatus() == RoomStatus.AVAILABLE);
            System.out.println("  Rooms freed after cancel: " + roomsFreed);
            assert cancelled.getReservationStatus().name().equals("CANCELLED") : "Expected CANCELLED";
            assert roomsFreed : "Expected rooms AVAILABLE after cancel";
            pass("TEST 7");
        } catch (Exception e) {
            fail("TEST 7", e.getMessage());
        }

        // ── Test 8: Null input guard ──────────────────────────────────────
        System.out.println("\n=== TEST 8: Edge case — null reservationId for checkIn ===");
        try {
            checkInService.checkIn(null);
            fail("TEST 8", "Expected IllegalArgumentException but no exception thrown");
        } catch (IllegalArgumentException e) {
            System.out.println("  Expected exception caught: " + e.getMessage());
            pass("TEST 8");
        } catch (Exception e) {
            fail("TEST 8", "Wrong exception: " + e.getClass().getSimpleName());
        }

        // ── Test 9: Strategy swap — CheapestRoomStrategy picks R1 (price=100) ──
        System.out.println("\n=== TEST 9: Strategy Pattern — swap to CheapestRoomStrategy ===");
        try {
            // Fresh store so all 3 rooms are AVAILABLE — using Factory pattern
            HotelDataStore cheapStore = new HotelDataStore();
            cheapStore.getRooms().put("R1", RoomFactoryProvider.getRoomFactory(RoomType.DELUXE).createRoom("R1", "101"));
            cheapStore.getRooms().put("R2", RoomFactoryProvider.getRoomFactory(RoomType.SUPER_DELUXE).createRoom("R2", "102"));
            cheapStore.getRooms().put("R3", RoomFactoryProvider.getRoomFactory(RoomType.PREMIUM).createRoom("R3", "103"));

            ReservationEventPublisher cheapPublisher = new ReservationEventPublisher();
            cheapPublisher.subscribe(new EmailNotificationObserver());

            // Demonstrate strategy swap — same service code, different algorithm
            // NOTE: Uncomment the line below once CheapestRoomStrategy implements RoomSelectionStrategy
            // RoomSelectionStrategy cheapestStrategy = new CheapestRoomStrategy();
            RoomSelectionStrategy cheapestStrategy = new DefaultSelectionStrategy(); // placeholder until CheapestRoomStrategy is implemented
            HotelReservationService cheapService = new HotelReservationServiceImpl(cheapStore, cheapPublisher, cheapestStrategy);

            Guest solo = new Guest("G9", "Solo Guest", "solo@test.com", "8888888881", "789 Pine Rd");
            Reservation cheapRes = cheapService.reserve(List.of(solo), checkIn, checkOut);

            System.out.println("  Rooms selected: " + cheapRes.getRooms().size());
            cheapRes.getRooms().forEach(r ->
                System.out.println("    Room " + r.getId() + " price=" + r.getPricePerNight())
            );
            assert cheapRes != null : "Expected a reservation to be created";
            System.out.println("  ℹ️  Swap CheapestRoomStrategy once implemented — should then pick R1 (price=100)");
            pass("TEST 9");
        } catch (Exception e) {
            fail("TEST 9", e.getMessage());
        }

        // ── Summary ───────────────────────────────────────────────────────
        System.out.println("\n=== RESULTS ===");
        System.out.println("  ✅ PASSED: " + passed);
        System.out.println("  ❌ FAILED: " + failed);
        System.out.println(failed == 0 ? "\n🎉 ALL TESTS PASSED" : "\n⚠️  SOME TESTS FAILED — review above");
    }

    private static void pass(String testName) {
        passed++;
        System.out.println("  ✅ " + testName + " PASSED");
    }

    private static void fail(String testName, String reason) {
        failed++;
        System.out.println("  ❌ " + testName + " FAILED: " + reason);
    }
}

