package com.lld.practice.easy.p1.service;

public class LibraryOperationException extends RuntimeException {
    private final StatusCode statusCode;

    public LibraryOperationException(StatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }
}
