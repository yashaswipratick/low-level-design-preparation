package com.lld.practice.tutor_sessions.hotel_reservation.state.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.state.ReservationState;

import java.time.LocalDate;

public class ReservedState implements ReservationState {


    @Override
    public void checkIn(Reservation reservation) {
        reservation.setReservationStatus(ReservationStatus.CHECKED_IN);
        reservation.setReservationState(new CheckedInState());
    }

    @Override
    public void checkOut(Reservation reservation) {
        throw new HotelBookingException("Cannot check-out. Reservation is not checked-in yet.");
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservation.setReservationState(new CancelledState());
    }

    @Override
    public void modify(Reservation reservation, LocalDate newCheckIn, LocalDate newCheckOut) {
        reservation.setCheckInDate(newCheckIn);
        reservation.setCheckOutDate(newCheckOut);
    }
}
