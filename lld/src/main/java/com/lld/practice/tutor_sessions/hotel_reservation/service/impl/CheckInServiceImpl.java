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
            throw new HotelBookingException("reservationId not found");
        }

        Reservation reservation = reservations.get(reservationId);
        reservation.getReservationState().checkIn(reservation);
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
            throw new HotelBookingException("reservationId not found");
        }

        Reservation reservation = reservations.get(reservationId);
        reservation.getReservationState().checkOut(reservation);
        reservation.getRooms()
                .forEach(room ->  {
            rooms.get(room.getId()).setRoomStatus(RoomStatus.AVAILABLE);
        });
        reservationEventPublisher.publish(new ReservationEvent(reservation, ReservationEventType.CHECKED_OUT));
        return reservation;
    }
}
