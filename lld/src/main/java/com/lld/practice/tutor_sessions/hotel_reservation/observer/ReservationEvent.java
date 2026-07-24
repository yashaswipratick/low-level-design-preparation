package com.lld.practice.tutor_sessions.hotel_reservation.observer;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;

import java.time.LocalDateTime;

public class ReservationEvent {

    private final Reservation reservation;
    private final ReservationEventType eventType;
    private final LocalDateTime occurredAt;

    public ReservationEvent(Reservation reservation, ReservationEventType eventType) {
        this.reservation = reservation;
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now(); // auto-captured — events are immutable facts
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ReservationEventType getEventType() {
        return eventType;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
