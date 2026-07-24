package com.lld.practice.tutor_sessions.hotel_reservation.observer;

import java.util.ArrayList;
import java.util.List;

public class ReservationEventPublisher {

    private final List<ReservationObserver>  observers = new ArrayList<>();

    public void subscribe(ReservationObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(ReservationObserver observer) {
        observers.remove(observer);
    }

    public void publish(ReservationEvent event) {
        observers.forEach(observer -> observer.onEvent(event));
    }
}
