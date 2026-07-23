package com.lld.practice.tutor_sessions.hotel_reservation.service;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;

public interface CheckInService {

    Reservation checkIn(String reservationId);

    Reservation checkOut(String reservationId);
}
