package com.lld.practice.tutor_sessions.elevator.exceptions;

public class ElevatorDispatchException extends RuntimeException {
    String message;

    public ElevatorDispatchException(String message) {
        super(message);
    }
}
