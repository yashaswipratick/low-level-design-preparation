package com.lld.practice.tutor_sessions.hotel_reservation.service.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.NotificationTypeStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Guest;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Notification;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;
import com.lld.practice.tutor_sessions.hotel_reservation.service.HotelReservationService;
import com.lld.practice.tutor_sessions.hotel_reservation.service.NotificationService;
import com.lld.practice.tutor_sessions.hotel_reservation.store.HotelDataStore;
import com.lld.practice.tutor_sessions.hotel_reservation.validator.HotelBookingServiceValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HotelReservationServiceImpl implements HotelReservationService {

    private final Map<String, Reservation> reservations;
    private final Map<String, Room> rooms;
    private final NotificationService notificationService;

    // HotelDataStore is injected — same instance shared across all services
    public HotelReservationServiceImpl(HotelDataStore dataStore, NotificationService notificationService) {
        this.reservations = dataStore.getReservations();
        this.rooms = dataStore.getRooms();
        this.notificationService = notificationService;
    }


    @Override
    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        String validate = HotelBookingServiceValidator.validateSearchRooms(checkIn, checkOut, rooms, reservations);
        if (validate != null) {
            throw new IllegalArgumentException(validate);
        }
        List<Room> bookedRooms = reservations.values().stream()
                .filter(reservation ->
                        // Reservations that OVERLAP with requested dates
                        !reservation.getCheckOutDate().isBefore(checkIn) &&
                                !reservation.getCheckInDate().isAfter(checkOut))
                .flatMap(reservation -> reservation.getRooms().stream())
                .toList();

        return rooms.values().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.AVAILABLE)
                .filter(room -> bookedRooms.stream()
                        .noneMatch(booked -> booked.getId().equals(room.getId())))
                .toList();
    }

    @Override
    public Reservation reserve(List<Guest> guest, LocalDate checkIn, LocalDate checkOut) {
        if (guest == null) {
            throw new IllegalArgumentException("guest is null");
        }
        if (checkIn == null || checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("checkin is null OR checkin is after checkOut");
        }

        if (checkOut == null || checkOut.isBefore(checkIn)) {
            throw new IllegalArgumentException("check out is null OR check out is after checkIn");
        }

        List<Room> availableRooms = searchAvailableRooms(checkIn, checkOut);

        if (availableRooms.isEmpty()) {
            throw new HotelBookingException("No available rooms for the given dates");
        }

        int numberOfRooms = (int) Math.ceil(guest.size() / 2.0);
        List<Room> bookedRoom = availableRooms.subList(0, numberOfRooms);
        Reservation reservation = new Reservation(UUID.randomUUID().toString(),
                guest, bookedRoom, checkIn, checkOut, null, null,
                LocalDate.now(), ReservationStatus.RESERVED);
        bookedRoom.forEach(room -> room.setRoomStatus(RoomStatus.BOOKED));

        reservations.put(reservation.getId(), reservation);
        notificationService.trackNotification(reservation, NotificationTypeStatus.EMAIL, "Reservation Successful");

        return reservation;
    }

    @Override
    public Reservation cancelReservation(String reservationId) {
        if (reservations.isEmpty()) {
            throw new HotelBookingException("No reservations found");
        }

        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is null");
        }

        if (!reservations.containsKey(reservationId)) {
            throw new HotelBookingException("reservationId not found");
        }

        Reservation reservation = reservations.get(reservationId);
        reservation.getRooms().forEach(room -> room.setRoomStatus(RoomStatus.AVAILABLE));
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservations.remove(reservationId);
        notificationService.trackNotification(reservation, NotificationTypeStatus.EMAIL, "Reservation Cancelled");
        return reservation;
    }

    @Override
    public Reservation modifyReservation(String reservationId, LocalDate newCheckIn, LocalDate newCheckOut) {
        if (reservations.isEmpty()) {
            throw new IllegalArgumentException("No reservations found");
        }

        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is null");
        }

        if (!reservations.containsKey(reservationId)) {
            throw new IllegalArgumentException("reservationId not found");
        }

        Reservation reservation = reservations.get(reservationId);
        reservation.getRooms().forEach(room -> room.setRoomStatus(RoomStatus.AVAILABLE));
        reservations.remove(reservationId);  // remove old before creating new
        Reservation reserve = reserve(reservation.getGuests(), newCheckIn, newCheckOut);
        notificationService.trackNotification(reserve, NotificationTypeStatus.EMAIL, "Reservation Modified");
        return reserve;
    }

    @Override
    public Reservation viewReservation(String reservationId) {
        if (reservations.isEmpty()) {
            throw new IllegalArgumentException("No reservations found");
        }

        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is null");
        }

        if (!reservations.containsKey(reservationId)) {
            throw new IllegalArgumentException("reservationId not found");
        }
        return reservations.get(reservationId);
    }
}
