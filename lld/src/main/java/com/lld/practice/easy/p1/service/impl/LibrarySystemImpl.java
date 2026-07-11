package com.lld.practice.easy.p1.service.impl;

import com.lld.practice.easy.p1.model.Book;
import com.lld.practice.easy.p1.model.User;
import com.lld.practice.easy.p1.service.LibraryOperationException;
import com.lld.practice.easy.p1.service.LibrarySystem;
import com.lld.practice.easy.p1.service.StatusCode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LibrarySystemImpl implements LibrarySystem {

    Map<String, Book> bookCatalog = new ConcurrentHashMap<>();
    Map<String, User> userCatalog = new ConcurrentHashMap<>();
    Map<String, Set<String>> bookToUser = new ConcurrentHashMap<>();
    Map<String, Object> bookLocks = new ConcurrentHashMap<>();

    private Object getBookLock(String bookId) {
        return bookLocks.computeIfAbsent(bookId, key -> new Object());
    }

    @Override
    public void addBook(Book book) {
        if (book == null || book.getId() == null) {
            throw new LibraryOperationException(StatusCode.INVALID_INPUT, "Either book object or book id is null");
        }
        if (!bookCatalog.containsKey(book.getId())) {
            bookCatalog.put(book.getId(), book);
        } else {
            Book exisitingBook = bookCatalog.get(book.getId());
            exisitingBook.setTotalCopies(exisitingBook.getTotalCopies() + book.getTotalCopies());
            bookCatalog.put(book.getId(), exisitingBook);
        }
    }

    @Override
    public void addUser(User user) {
        if (user == null || user.getId() == null) {
            throw new LibraryOperationException(StatusCode.INVALID_INPUT, "Either user object or user id is null");
        }

        if (!userCatalog.containsKey(user.getId())) {
            userCatalog.put(user.getId(), user);
        } else {
            throw new LibraryOperationException(StatusCode.DUPLICATE_USER, "User already exists");
        }
    }

    @Override
    public void issueBook(String bookId, String userId) {
        if (bookId == null || userId == null) {
            throw new LibraryOperationException(StatusCode.INVALID_INPUT, "Either book id or user id is null");
        }

        synchronized (getBookLock(bookId)) {
            if (!bookCatalog.containsKey(bookId) || !userCatalog.containsKey(userId)) {
                throw new LibraryOperationException(StatusCode.BOOK_OR_USER_NOT_FOUND, "Either book or User does not exists");
            }

            Book existingBookDetails = bookCatalog.get(bookId);
            int availableCopies = existingBookDetails.getTotalCopies() - existingBookDetails.getIssuedCopies();

            if (availableCopies <= 0) {
                throw new LibraryOperationException(StatusCode.COPIES_NOT_AVAILABLE, "Copies not available to issue");
            }

            Set<String> issuedUsers = bookToUser.computeIfAbsent(bookId, key -> ConcurrentHashMap.newKeySet());
            if (!issuedUsers.add(userId)) {
                throw new LibraryOperationException(StatusCode.DUPLICATE_ISSUE, "One copy is already issued to the user");
            }

            existingBookDetails.setIssuedCopies(existingBookDetails.getIssuedCopies() + 1);
            bookCatalog.put(existingBookDetails.getId(), existingBookDetails);
        }
    }

    @Override
    public void returnBook(String userId, String bookId) {
        if (bookId == null || userId == null) {
            throw new LibraryOperationException(StatusCode.INVALID_INPUT, "Either book id or user id is null");
        }

        synchronized (getBookLock(bookId)) {
            if (!bookCatalog.containsKey(bookId) || !userCatalog.containsKey(userId)) {
                throw new LibraryOperationException(StatusCode.BOOK_OR_USER_NOT_FOUND, "Either book or User does not exists");
            }

            Set<String> issuedUsers = bookToUser.get(bookId);
            if (issuedUsers == null || !issuedUsers.remove(userId)) {
                throw new LibraryOperationException(StatusCode.RETURN_NOT_ISSUED, "book was not issued to the user.");
            }

            Book book = bookCatalog.get(bookId);
            if (book.getIssuedCopies() > 0) {
                book.setIssuedCopies(book.getIssuedCopies() - 1);
                bookCatalog.put(bookId, book);
            } else {
                throw new LibraryOperationException(StatusCode.DATA_CORRUPTION, "courrupted data");
            }

            if (issuedUsers.isEmpty()) {
                bookToUser.remove(bookId, issuedUsers);
            }
        }
    }

    @Override
    public List<Book> search(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return bookCatalog.values().stream()
                .filter(book ->
                        book.getAuthor().equalsIgnoreCase(query) || book.getTitle().equalsIgnoreCase(query))
                .toList();
    }
}
