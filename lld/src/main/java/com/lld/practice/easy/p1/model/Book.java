package com.lld.practice.easy.p1.model;

public class Book {

    private String id;
    private String title;
    private String author;
    private Integer totalCopies;
    private Integer issuedCopies;

    public Book() {
    }

    public Book(String id, String title, String author, Integer totalCopies, Integer issuedCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.issuedCopies = issuedCopies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Integer getIssuedCopies() {
        return issuedCopies;
    }

    public void setIssuedCopies(Integer issuedCopies) {
        this.issuedCopies = issuedCopies;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", totalCopies=" + totalCopies +
                ", issuedCopies=" + issuedCopies +
                '}';
    }
}
