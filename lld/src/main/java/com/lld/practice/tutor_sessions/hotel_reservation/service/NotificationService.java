package com.lld.practice.tutor_sessions.hotel_reservation.service;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.NotificationTypeStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Notification;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;

public interface NotificationService {

    Notification trackNotification(Reservation reservation, NotificationTypeStatus type, String message);
}
