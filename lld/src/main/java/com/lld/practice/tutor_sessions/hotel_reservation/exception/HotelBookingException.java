package com.lld.practice.tutor_sessions.hotel_reservation.exception;

public class HotelBookingException extends RuntimeException {

    private String message;

    public HotelBookingException(String message) {
        super(message);
    }
}
