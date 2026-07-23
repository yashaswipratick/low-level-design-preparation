package com.lld.practice.tutor_sessions.hotel_reservation.service.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.NotificationEvent;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.NotificationTypeStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Notification;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.service.NotificationService;

import com.lld.practice.tutor_sessions.hotel_reservation.store.HotelDataStore;

import java.time.LocalDateTime;
import java.util.Map;

public class NotificationServiceImpl implements NotificationService {
    private final Map<String, Notification> trackNotifications;

    public NotificationServiceImpl(HotelDataStore dataStore) {
        this.trackNotifications = dataStore.getNotifications();
    }

    @Override
    public Notification trackNotification(Reservation reservation, NotificationTypeStatus type, String message) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation is null");
        }

        if (type == null) {
            throw new IllegalArgumentException("Notification type is null");
        }
        if (trackNotifications.containsKey(reservation.getId())) {
            Notification notification = trackNotifications.get(reservation.getId());
            notification.setNotificationEvent(NotificationEvent.fromReservationStatus(reservation.getReservationStatus()));
            notification.setMessage(message);
            notification.setNotificationTypeStatus(type);
            notification.setModifiedDate(LocalDateTime.now());
            return notification;
        }

        Notification notification =
                new Notification(reservation.getId(), reservation.getGuests(),
                        NotificationEvent.fromReservationStatus(reservation.getReservationStatus()),
                        message, type, LocalDateTime.now(), LocalDateTime.now());
        trackNotifications.put(reservation.getId(), notification);
        return notification;
    }
}
