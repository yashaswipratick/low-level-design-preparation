package com.lld.practice.tutor_sessions.hotel_reservation.state;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;

import java.time.LocalDate;

public interface ReservationState {
    
    void checkIn(Reservation reservation);
    void checkOut(Reservation reservation);
    void cancel(Reservation reservation);
    void modify(Reservation reservation, LocalDate newCheckIn, LocalDate newCheckOut);
}
