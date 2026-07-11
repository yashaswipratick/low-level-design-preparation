package com.lld.practice.easy.p1.service.impl;

import com.lld.practice.easy.p1.model.Book;
import com.lld.practice.easy.p1.model.User;
import com.lld.practice.easy.p1.service.LibraryOperationException;
import com.lld.practice.easy.p1.service.StatusCode;

public class ErrorContractTest {
    public static void main(String[] args) {
        LibrarySystemImpl system = new LibrarySystemImpl();
        system.addUser(new User("U1", "User1", "u1@x.com", 9000000001L));
        system.addUser(new User("U2", "User2", "u2@x.com", 9000000002L));
        system.addBook(new Book("B1", "Clean Code", "Robert Martin", 2, 0));

        System.out.println("=== Error Contract Test ===");
        boolean allPassed = true;

        allPassed &= validateStatusCode(
                "Issue with missing user",
                StatusCode.BOOK_OR_USER_NOT_FOUND,
                () -> system.issueBook("B1", "U404")
        );

        allPassed &= validateStatusCode(
                "Duplicate issue",
                StatusCode.DUPLICATE_ISSUE,
                () -> {
                    system.issueBook("B1", "U1");
                    system.issueBook("B1", "U1");
                }
        );

        allPassed &= validateStatusCode(
                "Return not issued",
                StatusCode.RETURN_NOT_ISSUED,
                () -> system.returnBook("U2", "B1")
        );

        allPassed &= validateStatusCode(
                "Invalid input",
                StatusCode.INVALID_INPUT,
                () -> system.issueBook(null, "U1")
        );

        System.out.println("Point-8 contract satisfied? " + allPassed);
    }

    private static boolean validateStatusCode(String scenario, StatusCode expected, Runnable action) {
        try {
            action.run();
            System.out.println(scenario + " -> expected " + expected + ", actual: NO_EXCEPTION");
            return false;
        } catch (LibraryOperationException ex) {
            StatusCode actual = ex.getStatusCode();
            System.out.println(scenario + " -> expected " + expected + ", actual: " + actual);
            return actual == expected;
        } catch (Exception ex) {
            System.out.println(scenario + " -> expected " + expected + ", actual: WRONG_EXCEPTION " + ex.getClass().getSimpleName());
            return false;
        }
    }
}
