package com.lld.practice.tutor_sessions.hotel_reservation.service.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.NotificationTypeStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;
import com.lld.practice.tutor_sessions.hotel_reservation.service.CheckInService;
import com.lld.practice.tutor_sessions.hotel_reservation.service.NotificationService;
import com.lld.practice.tutor_sessions.hotel_reservation.store.HotelDataStore;

import java.util.Map;

public class CheckInServiceImpl implements CheckInService {

    private final Map<String, Reservation> reservations;
    private final Map<String, Room> rooms;
    private final NotificationService notificationService;

    public CheckInServiceImpl(HotelDataStore dataStore, NotificationService notificationService) {
        this.reservations = dataStore.getReservations();
        this.rooms = dataStore.getRooms();
        this.notificationService = notificationService;
    }

    @Override
    public Reservation checkIn(String reservationId) {

        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is null");
        }

        if (reservations.isEmpty()) {
            throw new HotelBookingException("No reservations found");
        }

        if (!reservations.containsKey(reservationId)) {
            throw new IllegalArgumentException("reservationId not found");
        }

        Reservation reservation = reservations.get(reservationId);
        if (reservation.getReservationStatus() != ReservationStatus.RESERVED) {
            throw new HotelBookingException("Cannot check-in. Reservation status is: " + reservation.getReservationStatus());
        }
        reservation.setReservationStatus(ReservationStatus.CHECKED_IN);
        notificationService.trackNotification(reservation, NotificationTypeStatus.EMAIL, "Checked In Completed");
        return reservation;
    }

    @Override
    public Reservation checkOut(String reservationId) {
        if (reservations.isEmpty()) {
            throw new HotelBookingException("No reservations found");
        }

        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is null");
        }

        if (!reservations.containsKey(reservationId)) {
            throw new IllegalArgumentException("reservationId not found");
        }

        Reservation reservation = reservations.get(reservationId);
        reservation.setReservationStatus(ReservationStatus.CHECKED_OUT);
        reservations.put(reservationId, reservation);
        reservation.getRooms().forEach(room ->  {
            Room room1 = rooms.get(room.getId());
            room1.setRoomStatus(RoomStatus.AVAILABLE);
            rooms.put(room.getId(), room1);
        });
        notificationService.trackNotification(reservation, NotificationTypeStatus.EMAIL, "Checked out Completed");
        return reservation;
    }
}
