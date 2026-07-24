package com.lld.practice.tutor_sessions.hotel_reservation.state.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.state.ReservationState;

import java.time.LocalDate;

public class CheckedOutState implements ReservationState {
    @Override
    public void checkIn(Reservation reservation) {
        throw new HotelBookingException("Cannot check in the check out reservation");
    }

    @Override
    public void checkOut(Reservation reservation) {
        throw new HotelBookingException("Cannot check out the check out reservation");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new HotelBookingException("Cannot cancel the check out reservation");
    }

    @Override
    public void modify(Reservation reservation, LocalDate newCheckIn, LocalDate newCheckOut) {
        throw new HotelBookingException("Cannot modify the check out reservation");
    }
}
