package com.lld.practice.tutor_sessions.hotel_reservation.chain_of_responsibility;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;

public abstract class ReservationHandler {

    private ReservationHandler nextHandler;

    public ReservationHandler setNextHandler(ReservationHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }

    public abstract void handle(ReservationRequest request);

    protected void handleNext(ReservationRequest request) {
        if (nextHandler != null) {
            nextHandler.handle(request);
        }
    }
}
