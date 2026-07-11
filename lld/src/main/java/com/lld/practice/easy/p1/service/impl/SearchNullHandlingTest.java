package com.lld.practice.easy.p1.service.impl;

import com.lld.practice.easy.p1.model.Book;

import java.util.List;

public class SearchNullHandlingTest {
    public static void main(String[] args) {
        LibrarySystemImpl system = new LibrarySystemImpl();
        system.addBook(new Book("B1", "Clean Code", "Robert Martin", 2, 0));

        System.out.println("=== Search Null Handling Test ===");
        try {
            List<Book> nullResult = system.search(null);
            System.out.println("search(null) -> expected [] without exception, actual size: " + nullResult.size());
        } catch (Exception e) {
            System.out.println("search(null) threw exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }

        try {
            List<Book> emptyResult = system.search("");
            System.out.println("search(\"\") -> expected [] without exception, actual size: " + emptyResult.size());
        } catch (Exception e) {
            System.out.println("search(\"\") threw exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}

