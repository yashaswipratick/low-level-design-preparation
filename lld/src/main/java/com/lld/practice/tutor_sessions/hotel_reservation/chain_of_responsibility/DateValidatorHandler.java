package com.lld.practice.tutor_sessions.hotel_reservation.chain_of_responsibility;

import java.time.LocalDate;

public class DateValidatorHandler extends ReservationHandler {

    @Override
    public void handle(ReservationRequest request) {

        LocalDate checkIn = request.getCheckIn();
        LocalDate checkOut = request.getCheckOut();

        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past");
        }

        if (checkIn.isEqual(checkOut)) {
            throw new IllegalArgumentException("Check-in and check-out cannot be the same day");
        }

        if (checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("Check-in date cannot be after check-out date");
        }

        if (checkOut.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-out date cannot be in the past");
        }
        handleNext(request);
    }
}
