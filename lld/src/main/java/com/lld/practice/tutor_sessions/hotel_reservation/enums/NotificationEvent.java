package com.lld.practice.tutor_sessions.hotel_reservation.enums;

import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;

public enum NotificationEvent {
    RESERVED,
    MODIFIED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED;

    public static NotificationEvent fromReservationStatus(ReservationStatus status) {
        switch (status) {
            case RESERVED:    return NotificationEvent.RESERVED;
            case CHECKED_IN:  return NotificationEvent.CHECKED_IN;
            case CHECKED_OUT: return NotificationEvent.CHECKED_OUT;
            case CANCELLED:   return NotificationEvent.CANCELLED;
            default: throw new HotelBookingException("No matching NotificationEvent for: " + status.name());
        }
    }
}
