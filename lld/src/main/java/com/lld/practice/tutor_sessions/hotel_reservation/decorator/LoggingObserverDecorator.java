package com.lld.practice.tutor_sessions.hotel_reservation.decorator;

import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEvent;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationObserver;

public class LoggingObserverDecorator implements ReservationObserver {

    private final ReservationObserver reservationObserverWrapper;

    public LoggingObserverDecorator(ReservationObserver reservationObserverWrapper) {
        this.reservationObserverWrapper = reservationObserverWrapper;
    }

    @Override
    public void onEvent(ReservationEvent event) {
        System.out.println("Sending Notification: " + event.getEventType() + " for Reservation ID: " + event.getReservation().getId());
        try {
            reservationObserverWrapper.onEvent(event);
            System.out.println("Notification Sent successfully");
        } catch (Exception e) {
            System.out.println("Notification failed" + e.getCause());
            throw new RuntimeException("Notification failed" + e.getCause());
        }
    }
}
