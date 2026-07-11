package com.lld.practice.easy.p1.service;

import com.lld.practice.easy.p1.model.Book;
import com.lld.practice.easy.p1.model.User;

import java.util.List;

public interface LibrarySystem {

    void addBook(Book book);

    void addUser(User user);

    void issueBook(String bookId, String userId);

    void returnBook(String userId, String bookId);

    List<Book> search(String query);
}
