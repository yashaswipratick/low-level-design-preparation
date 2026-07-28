package com.lld.practice.tutor_sessions.hotel_reservation.decorator;

import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEvent;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationObserver;

public class RetryObserverDecorator implements ReservationObserver {

    private final ReservationObserver reservationObserverWrapper;
    private final int maxRetries;

    public RetryObserverDecorator(ReservationObserver reservationObserverWrapper, int maxRetries) {
        this.reservationObserverWrapper = reservationObserverWrapper;
        this.maxRetries = maxRetries;
    }

    @Override
    public void onEvent(ReservationEvent event) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                reservationObserverWrapper.onEvent(event);
                return;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw new HotelBookingException("Notification Failed after " + maxRetries + " retries");
                }
                System.out.println("Retry attempt " + attempt);
            }
        }
    }
}
