package com.lld.practice.easy.p1.service.impl;

import com.lld.practice.easy.p1.model.Book;

import java.lang.reflect.Method;
import java.util.List;

public class SearchContractTest {
    public static void main(String[] args) {
        LibrarySystemImpl system = new LibrarySystemImpl();

        system.addBook(new Book("B1", "Clean Code", "Robert Martin", 2, 0));
        system.addBook(new Book("B2", "Clean Architecture", "Robert Martin", 1, 0));
        system.addBook(new Book("B3", "Effective Java", "Joshua Bloch", 1, 0));

        System.out.println("=== Search Contract Test ===");
        try {
            Method method = LibrarySystemImpl.class.getMethod("search", String.class);
            List<Book> byAuthor = cast(method.invoke(system, "Robert Martin"));
            List<Book> byTitle = cast(method.invoke(system, "Clean Code"));
            List<Book> notFound = cast(method.invoke(system, "Unknown"));

            System.out.println("Query(author) 'Robert Martin' -> expected >= 2, actual: " + byAuthor.size());
            System.out.println("Query(title)  'Clean Code'    -> expected >= 1, actual: " + byTitle.size());
            System.out.println("Query(unknown) 'Unknown'      -> expected 0, actual: " + notFound.size());

            boolean contractOk = byAuthor.size() >= 2 && byTitle.size() >= 1 && notFound.isEmpty();
            System.out.println("Point-6 contract satisfied? " + contractOk);
        } catch (NoSuchMethodException e) {
            System.out.println("search(String query) method not found in LibrarySystemImpl");
            System.out.println("Point-6 contract satisfied? false");
        } catch (Exception e) {
            System.out.println("Search invocation failed: " + e.getMessage());
            System.out.println("Point-6 contract satisfied? false");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Book> cast(Object value) {
        return (List<Book>) value;
    }
}

