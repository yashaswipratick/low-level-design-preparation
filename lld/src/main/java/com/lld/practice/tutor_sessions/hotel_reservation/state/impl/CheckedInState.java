package com.lld.practice.tutor_sessions.hotel_reservation.state.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.state.ReservationState;

import java.time.LocalDate;

public class CheckedInState implements ReservationState {
    @Override
    public void checkIn(Reservation reservation) {
        throw new HotelBookingException("Already Checked In");
    }

    @Override
    public void checkOut(Reservation reservation) {
        reservation.setReservationStatus(ReservationStatus.CHECKED_OUT);
        reservation.setReservationState(new CheckedOutState());
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new HotelBookingException("Cannot cancel — already checked in");
    }

    @Override
    public void modify(Reservation reservation, LocalDate newCheckIn, LocalDate newCheckOut) {
        throw new HotelBookingException("Cannot modify — already checked in");
    }
}
