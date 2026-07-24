package com.lld.practice.tutor_sessions.hotel_reservation.state.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.state.ReservationState;

import java.time.LocalDate;

public class CancelledState implements ReservationState {

    @Override
    public void checkIn(Reservation reservation) {
        throw new HotelBookingException("Cannot check-in. Reservation is CANCELLED");
    }

    @Override
    public void checkOut(Reservation reservation) {
        throw new HotelBookingException("Cannot check-out. Reservation is CANCELLED");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new HotelBookingException("Reservation is already CANCELLED");
    }

    @Override
    public void modify(Reservation reservation, LocalDate newCheckIn, LocalDate newCheckOut) {
        throw new HotelBookingException("Cannot modify. Reservation is CANCELLED");
    }
}
