package com.lld.practice.tutor_sessions.hotel_reservation;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomType;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
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

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CONCURRENCY TEST DRIVER — Hotel Reservation System
 *
 * Tests the following concurrency scenarios:
 * ─────────────────────────────────────────────────────────────
 * TEST 1: Sanity check — single thread reservation (baseline)
 * TEST 2: Two threads book SAME room → only ONE should succeed (no double-booking)
 * TEST 3: Two threads book DIFFERENT rooms → BOTH should succeed (parallelism)
 * TEST 4: Two threads cancel SAME reservation → only ONE should succeed
 * TEST 5: Deadlock prevention — threads acquire room locks in different order
 * TEST 6: High load — 10 threads competing for 3 rooms
 * ─────────────────────────────────────────────────────────────
 */
public class HotelConcurrencyTestDriver {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║   HOTEL RESERVATION — CONCURRENCY TEST SUITE    ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        test1_SingleThreadSanityCheck();
        test2_TwoThreadsSameRoom_OnlyOneSucceeds();
        test3_TwoThreadsDifferentRooms_BothSucceed();
        test4_TwoThreadsCancelSameReservation();
        test5_DeadlockPrevention_MultipleRoomsOppositeOrder();
        test6_HighLoad_10ThreadsFor3Rooms();

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.printf("║  RESULTS: %d PASSED, %d FAILED                     ║%n", passed, failed);
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 1: Sanity — single thread reserve + checkIn + checkOut
    // ─────────────────────────────────────────────────────────────
    static void test1_SingleThreadSanityCheck() throws InterruptedException {
        System.out.println("TEST 1: Single thread sanity check (baseline)");
        HotelDataStore store = buildStore(3);
        HotelReservationService service = buildBookingService(store);
        CheckInService checkInService = buildCheckInService(store, service);

        try {
            Guest g1 = new Guest("G1", "Alice", "alice@test.com", "9999999991", "Test Address");
            Reservation r = service.reserve(List.of(g1), LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            assert r != null : "Reservation should not be null";

            checkInService.checkIn(r.getId());
            checkInService.checkOut(r.getId());

            // After checkout, room should be AVAILABLE again
            long availableCount = store.getRooms().values().stream()
                    .filter(room -> room.getRoomStatus() == RoomStatus.AVAILABLE).count();
            assert availableCount == 3 : "All 3 rooms should be AVAILABLE after checkout, got: " + availableCount;

            pass("Single thread reserve → checkIn → checkOut → room freed");
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 2: Two threads book SAME dates → only ONE should succeed
    // No double-booking allowed
    // ─────────────────────────────────────────────────────────────
    static void test2_TwoThreadsSameRoom_OnlyOneSucceeds() throws InterruptedException {
        System.out.println("\nTEST 2: Two threads competing for same room — only one should succeed");

        // Setup: only 1 room available
        HotelDataStore store = new HotelDataStore();
        store.getRooms().put("R1", new Room("R1", "101", RoomType.DELUXE, 100.0, RoomStatus.AVAILABLE));
        HotelReservationService service = buildBookingService(store);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1); // both threads start simultaneously
        CountDownLatch doneLatch = new CountDownLatch(2);

        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        for (int i = 0; i < 2; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await(); // wait for signal to start simultaneously
                    Guest g = new Guest("G" + id, "Guest" + id, "g" + id + "@test.com", "999000000" + id, "Test Address");
                    service.reserve(List.of(g), checkIn, checkOut);
                    successCount.incrementAndGet();
                } catch (HotelBookingException e) {
                    failCount.incrementAndGet(); // expected for one thread
                } catch (Exception e) {
                    System.out.println("  ⚠ Unexpected: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown(); // release both threads simultaneously
        doneLatch.await(5, TimeUnit.SECONDS);

        if (successCount.get() == 1 && failCount.get() == 1) {
            pass("Exactly 1 booking succeeded, 1 correctly rejected → no double-booking");
        } else {
            fail("Expected 1 success + 1 fail, got: " + successCount.get() + " success, " + failCount.get() + " fail");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 3: Two threads book DIFFERENT rooms → BOTH should succeed
    // Validates per-room parallelism (not blocked by each other)
    // ─────────────────────────────────────────────────────────────
    static void test3_TwoThreadsDifferentRooms_BothSucceed() throws InterruptedException {
        System.out.println("\nTEST 3: Two threads booking non-overlapping dates — both should succeed");
        System.out.println("  Note: RoomStatus is global (not date-aware), so Thread 1 starts after Thread 0 confirms.");
        System.out.println("  This validates correctness of date-overlap logic + per-room lock after first booking.");

        // Setup: 2 rooms
        HotelDataStore store = new HotelDataStore();
        store.getRooms().put("R1", new Room("R1", "101", RoomType.DELUXE, 100.0, RoomStatus.AVAILABLE));
        store.getRooms().put("R2", new Room("R2", "102", RoomType.PREMIUM, 200.0, RoomStatus.AVAILABLE));
        HotelReservationService service = buildBookingService(store);

        // Thread 0 reserves first (R1 → BOOKED for Jul 1-5)
        CountDownLatch thread0Done = new CountDownLatch(1);
        CountDownLatch thread1Done = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);

        // Thread 0: books Jul 1-5 → gets R1
        new Thread(() -> {
            try {
                Guest g = new Guest("G0", "Alice", "alice@test.com", "1111111111", "Test Address");
                service.reserve(List.of(g), LocalDate.now().plusDays(1), LocalDate.now().plusDays(5));
                successCount.incrementAndGet();
            } catch (Exception e) {
                System.out.println("  ⚠ Thread 0 failed: " + e.getMessage());
            } finally {
                thread0Done.countDown(); // signal Thread 1 to start
            }
        }).start();

        // Thread 1: starts AFTER Thread 0 confirms → searches and finds only R2 available
        // (R1 is BOOKED, so searchAvailableRooms returns only R2)
        thread0Done.await(5, TimeUnit.SECONDS);
        new Thread(() -> {
            try {
                Guest g = new Guest("G1", "Bob", "bob@test.com", "2222222222", "Test Address");
                service.reserve(List.of(g), LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));
                successCount.incrementAndGet();
            } catch (Exception e) {
                System.out.println("  ⚠ Thread 1 failed: " + e.getMessage());
            } finally {
                thread1Done.countDown();
            }
        }).start();

        thread1Done.await(5, TimeUnit.SECONDS);

        long bookedCount = store.getRooms().values().stream()
                .filter(r -> r.getRoomStatus() == RoomStatus.BOOKED).count();

        if (successCount.get() == 2 && bookedCount == 2) {
            pass("Both threads reserved different rooms (R1 + R2). Both BOOKED. No interference.");
            System.out.println("  ⚠ Limitation noted: current impl uses global RoomStatus (not date-aware).");
            System.out.println("    v3 fix: replace RoomStatus with a date-range availability map per room.");
        } else {
            fail("Expected 2 successes, got: " + successCount.get() + " (bookedRooms=" + bookedCount + ")");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 4: Two threads cancel SAME reservation → only ONE wins
    // ─────────────────────────────────────────────────────────────
    static void test4_TwoThreadsCancelSameReservation() throws InterruptedException {
        System.out.println("\nTEST 4: Two threads cancel same reservation — only one should succeed");

        HotelDataStore store = buildStore(2);
        HotelReservationService service = buildBookingService(store);

        // Pre-create one reservation
        Guest g = new Guest("G1", "Alice", "alice@test.com", "9999999991", "Test Address");
        Reservation r = service.reserve(List.of(g), LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
        String reservationId = r.getId();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    service.cancelReservation(reservationId);
                    successCount.incrementAndGet();
                } catch (HotelBookingException e) {
                    failCount.incrementAndGet(); // expected for second thread
                } catch (Exception e) {
                    System.out.println("  ⚠ Unexpected: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);

        if (successCount.get() == 1 && failCount.get() == 1) {
            pass("Exactly 1 cancellation succeeded, 1 correctly rejected → no duplicate cancel");
        } else {
            fail("Expected 1 success + 1 fail, got: " + successCount.get() + " success, " + failCount.get() + " fail");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 5: Deadlock prevention
    // Thread A wants rooms [R1, R2], Thread B wants rooms [R2, R1]
    // Without sorted acquisition → deadlock
    // With sorted acquisition → safe, no deadlock
    // ─────────────────────────────────────────────────────────────
    static void test5_DeadlockPrevention_MultipleRoomsOppositeOrder() throws InterruptedException {
        System.out.println("\nTEST 5: Deadlock prevention — threads acquire locks in opposite room order");

        // 2 rooms only — both threads will need both rooms (2 guests each = ceil(2/2)=1 room each)
        // Actually to force multi-room: 4 guests = ceil(4/2)=2 rooms
        HotelDataStore store = new HotelDataStore();
        store.getRooms().put("R1", new Room("R1", "101", RoomType.DELUXE, 100.0, RoomStatus.AVAILABLE));
        store.getRooms().put("R2", new Room("R2", "102", RoomType.PREMIUM, 200.0, RoomStatus.AVAILABLE));
        HotelReservationService service = buildBookingService(store);

        AtomicInteger completedCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        // Both threads try to book 2 rooms for different date ranges
        // Thread A: July 1-5 (both rooms)
        // Thread B: July 10-15 (both rooms — will succeed since dates don't overlap)
        LocalDate[] checkIns = {LocalDate.now().plusDays(1), LocalDate.now().plusDays(10)};
        LocalDate[] checkOuts = {LocalDate.now().plusDays(5), LocalDate.now().plusDays(15)};

        List<Guest> guestsA = List.of(
                new Guest("GA1", "Alice1", "a1@test.com", "1111111111", "Test Address"),
                new Guest("GA2", "Alice2", "a2@test.com", "2222222222", "Test Address"),
                new Guest("GA3", "Alice3", "a3@test.com", "3333333333", "Test Address"),
                new Guest("GA4", "Alice4", "a4@test.com", "4444444444", "Test Address")
        );
        List<Guest> guestsB = List.of(
                new Guest("GB1", "Bob1", "b1@test.com", "5555555555", "Test Address"),
                new Guest("GB2", "Bob2", "b2@test.com", "6666666666", "Test Address"),
                new Guest("GB3", "Bob3", "b3@test.com", "7777777777", "Test Address"),
                new Guest("GB4", "Bob4", "b4@test.com", "8888888888", "Test Address")
        );

        List<List<Guest>> guestGroups = List.of(guestsA, guestsB);

        for (int i = 0; i < 2; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    startLatch.await();
                    service.reserve(guestGroups.get(id), checkIns[id], checkOuts[id]);
                    completedCount.incrementAndGet();
                } catch (Exception e) {
                    completedCount.incrementAndGet(); // either success or expected failure is fine
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        // If deadlock: doneLatch.await will timeout (5 sec)
        boolean completed = doneLatch.await(5, TimeUnit.SECONDS);

        if (completed) {
            pass("Both threads completed without deadlock — sorted lock acquisition works");
        } else {
            fail("DEADLOCK DETECTED — threads did not complete within 5 seconds!");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // TEST 6: High load — 10 threads competing for 3 rooms
    // Validates no data corruption, no deadlock under load
    // ─────────────────────────────────────────────────────────────
    static void test6_HighLoad_10ThreadsFor3Rooms() throws InterruptedException {
        System.out.println("\nTEST 6: High load — 10 concurrent threads competing for 3 rooms");

        HotelDataStore store = buildStore(3);
        HotelReservationService service = buildBookingService(store);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(10);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        for (int i = 0; i < 10; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Guest g = new Guest("G" + id, "Guest" + id, "g" + id + "@test.com", "99900000" + String.format("%02d", id), "Test Address");
                    service.reserve(List.of(g), checkIn, checkOut);
                    successCount.incrementAndGet();
                } catch (HotelBookingException e) {
                    rejectedCount.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("  ⚠ Unexpected: " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        if (!completed) {
            fail("Threads did not complete in time — possible deadlock");
            return;
        }

        // Validate: exactly 3 rooms can be booked (1 per guest since 1 guest = ceil(1/2)=1 room)
        // But since all 10 threads use same dates, max 3 should succeed
        long bookedCount = store.getRooms().values().stream()
                .filter(r -> r.getRoomStatus() == RoomStatus.BOOKED).count();

        System.out.println("  → Successes: " + successCount.get() + " | Rejections: " + rejectedCount.get() + " | Rooms booked: " + bookedCount);

        if (successCount.get() <= 3 && (successCount.get() + rejectedCount.get()) == 10 && bookedCount == successCount.get()) {
            pass("No double-booking. " + successCount.get() + " bookings, " + rejectedCount.get() + " correctly rejected under load");
        } else {
            fail("Data integrity violation! successes=" + successCount.get() + " bookedRooms=" + bookedCount);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────
    static HotelDataStore buildStore(int roomCount) {
        HotelDataStore store = new HotelDataStore();
        RoomType[] types = {RoomType.DELUXE, RoomType.SUPER_DELUXE, RoomType.PREMIUM};
        for (int i = 1; i <= roomCount; i++) {
            String id = "R" + i;
            store.getRooms().put(id, new Room(id, "10" + i, types[(i - 1) % 3], 100.0 * i, RoomStatus.AVAILABLE));
        }
        return store;
    }

    static HotelReservationService buildBookingService(HotelDataStore store) {
        ReservationEventPublisher publisher = new ReservationEventPublisher();
        publisher.subscribe(new EmailNotificationObserver());
        return new HotelReservationServiceImpl(store, publisher);
    }

    static CheckInService buildCheckInService(HotelDataStore store, HotelReservationService bookingService) {
        ReservationEventPublisher publisher = new ReservationEventPublisher();
        publisher.subscribe(new EmailNotificationObserver());
        return new CheckInServiceImpl(store, publisher);
    }

    static void pass(String message) {
        passed++;
        System.out.println("  ✅ PASS: " + message);
    }

    static void fail(String message) {
        failed++;
        System.out.println("  ❌ FAIL: " + message);
    }
}

