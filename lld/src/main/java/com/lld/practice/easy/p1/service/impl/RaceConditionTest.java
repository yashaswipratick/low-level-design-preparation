package com.lld.practice.easy.p1.service.impl;

import com.lld.practice.easy.p1.model.Book;
import com.lld.practice.easy.p1.model.User;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class RaceConditionTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Race Condition Test ===");
        System.out.println();

        runUnsafeSimulation();
        System.out.println();
        runCurrentSynchronizedImplementation();
    }

    private static void runUnsafeSimulation() throws InterruptedException {
        System.out.println("--- Scenario 1: Old UNSAFE flow (without synchronized lock) ---");

        LibrarySystemImpl system = createSystemWithOneBookAndUsers(1, 12);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(12);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 1; i <= 12; i++) {
            String userId = "U" + i;
            Thread thread = new Thread(() -> issueWithoutLock(system, "B1", userId, ready, start, successCount, failureCount));
            thread.start();
        }

        ready.await();
        start.countDown();
        Thread.sleep(500);

        printState(system, successCount, failureCount);
        System.out.println("Expected: invariants may break here because flow is not atomic.");
        System.out.println("Observed invariant broken? " + isInvariantBroken(system, successCount.get()));
    }

    private static void runCurrentSynchronizedImplementation() throws InterruptedException {
        System.out.println("--- Scenario 2: Current SAFE flow (with synchronized(getBookLock(bookId))) ---");

        LibrarySystemImpl system = createSystemWithOneBookAndUsers(1, 12);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(12);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 1; i <= 12; i++) {
            String userId = "U" + i;
            Thread thread = new Thread(() -> {
                try {
                    ready.countDown();
                    start.await();
                    system.issueBook("B1", userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
            thread.start();
        }

        ready.await();
        start.countDown();
        Thread.sleep(500);

        printState(system, successCount, failureCount);
        System.out.println("Expected: only 1 success, remaining attempts fail, invariants stay intact.");
        System.out.println("Observed invariant broken? " + isInvariantBroken(system, successCount.get()));
    }

    private static LibrarySystemImpl createSystemWithOneBookAndUsers(int totalCopies, int userCount) {
        LibrarySystemImpl system = new LibrarySystemImpl();
        system.bookCatalog.put("B1", new Book("B1", "Distributed Systems", "Tanenbaum", totalCopies, 0));

        for (int i = 1; i <= userCount; i++) {
            String userId = "U" + i;
            system.userCatalog.put(userId, new User(userId, "User" + i, "u" + i + "@x.com", 9000000000L + i));
        }
        return system;
    }

    private static void issueWithoutLock(LibrarySystemImpl system,
                                         String bookId,
                                         String userId,
                                         CountDownLatch ready,
                                         CountDownLatch start,
                                         AtomicInteger successCount,
                                         AtomicInteger failureCount) {
        try {
            if (!system.bookCatalog.containsKey(bookId) || !system.userCatalog.containsKey(userId)) {
                failureCount.incrementAndGet();
                return;
            }

            Book existingBookDetails = system.bookCatalog.get(bookId);
            int availableCopies = existingBookDetails.getTotalCopies() - existingBookDetails.getIssuedCopies();

            ready.countDown();
            start.await();

            if (availableCopies <= 0) {
                failureCount.incrementAndGet();
                return;
            }

            Set<String> issuedUsers = system.bookToUser.computeIfAbsent(bookId, key -> ConcurrentHashMap.newKeySet());
            if (!issuedUsers.add(userId)) {
                failureCount.incrementAndGet();
                return;
            }

            Thread.sleep(20);
            existingBookDetails.setIssuedCopies(existingBookDetails.getIssuedCopies() + 1);
            system.bookCatalog.put(existingBookDetails.getId(), existingBookDetails);
            successCount.incrementAndGet();
        } catch (Exception e) {
            failureCount.incrementAndGet();
        }
    }

    private static void printState(LibrarySystemImpl system, AtomicInteger successCount, AtomicInteger failureCount) {
        Book finalBook = system.bookCatalog.get("B1");
        Set<String> users = system.bookToUser.get("B1");
        int issuedUserCount = users == null ? 0 : users.size();

        System.out.println("totalCopies      = " + finalBook.getTotalCopies());
        System.out.println("successCount     = " + successCount.get());
        System.out.println("failureCount     = " + failureCount.get());
        System.out.println("issuedCopies     = " + finalBook.getIssuedCopies());
        System.out.println("issuedUserCount  = " + issuedUserCount);
        System.out.println("issuedUsers      = " + users);
    }

    private static boolean isInvariantBroken(LibrarySystemImpl system, int successCount) {
        Book finalBook = system.bookCatalog.get("B1");
        Set<String> users = system.bookToUser.get("B1");
        int issuedUserCount = users == null ? 0 : users.size();

        return successCount > finalBook.getTotalCopies()
                || finalBook.getIssuedCopies() > finalBook.getTotalCopies()
                || issuedUserCount > finalBook.getTotalCopies();
    }
}

