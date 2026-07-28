package com.lld.practice.tutor_sessions.hotel_reservation.chain_of_responsibility;

public class NullCheckHandler extends ReservationHandler {

    @Override
    public void handle(ReservationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Reservation request cannot be null");
        }

        if (request.getGuests().isEmpty()) {
            throw new IllegalArgumentException("Reservation request must have at least one guest");
        }

        if (request.getCheckIn() == null) {
            throw new IllegalArgumentException("Reservation request must have at least one check in");
        }

        if (request.getCheckOut() == null) {
            throw new IllegalArgumentException("Reservation request must have at least one check out");
        }

        handleNext(request);
    }
}
