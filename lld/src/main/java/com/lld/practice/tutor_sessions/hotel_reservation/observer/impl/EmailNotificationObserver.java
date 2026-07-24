package com.lld.practice.tutor_sessions.hotel_reservation.observer.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEvent;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationObserver;

public class EmailNotificationObserver implements ReservationObserver {

    @Override
    public void onEvent(ReservationEvent event) {
        switch (event.getEventType()) {
            case RESERVED -> System.out.println("Email sent: Reservation confirmed for guest " + event.getReservation().getGuests().get(0).getName());
            case CANCELLED -> System.out.println("Email sent: Reservation cancelled for guest " + event.getReservation().getGuests().get(0).getName());
            case MODIFIED -> System.out.println("Email sent: Reservation has been modified for guest " + event.getReservation().getGuests().get(0).getName());
            case CHECKED_IN -> System.out.println("Email sent: checked in confirmed for reservation Id " + event.getReservation().getId());
            case CHECKED_OUT -> System.out.println("Email sent: checked out confirmed for reservation Id " + event.getReservation().getId());
        }

    }
}
