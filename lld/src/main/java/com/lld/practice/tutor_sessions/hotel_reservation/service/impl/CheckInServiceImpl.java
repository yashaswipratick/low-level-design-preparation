package com.lld.practice.tutor_sessions.hotel_reservation.service.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEvent;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEventPublisher;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEventType;
import com.lld.practice.tutor_sessions.hotel_reservation.service.CheckInService;
import com.lld.practice.tutor_sessions.hotel_reservation.store.HotelDataStore;

import java.util.Map;

public class CheckInServiceImpl implements CheckInService {

    private final Map<String, Reservation> reservations;
    private final Map<String, Room> rooms;
    private final ReservationEventPublisher reservationEventPublisher;

    public CheckInServiceImpl(HotelDataStore dataStore, ReservationEventPublisher reservationEventPublisher) {
        this.reservations = dataStore.getReservations();
        this.rooms = dataStore.getRooms();
        this.reservationEventPublisher = reservationEventPublisher;
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
        reservationEventPublisher.publish(new ReservationEvent(reservation, ReservationEventType.CHECKED_IN));
        return reservation;
    }

    @Override
    public Reservation checkOut(String reservationId) {
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
        if (reservation.getReservationStatus() != ReservationStatus.CHECKED_IN) {
            throw new HotelBookingException("Cannot check-out. Reservation status is: " + reservation.getReservationStatus());
        }
        reservation.setReservationStatus(ReservationStatus.CHECKED_OUT);
        reservation.getRooms().forEach(room ->  {
            Room room1 = rooms.get(room.getId());
            room1.setRoomStatus(RoomStatus.AVAILABLE);
        });
        reservationEventPublisher.publish(new ReservationEvent(reservation, ReservationEventType.CHECKED_OUT));
        return reservation;
    }
}
