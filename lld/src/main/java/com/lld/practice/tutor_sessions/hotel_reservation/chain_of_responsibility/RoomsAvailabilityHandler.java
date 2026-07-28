package com.lld.practice.tutor_sessions.hotel_reservation.chain_of_responsibility;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class RoomsAvailabilityHandler extends ReservationHandler {

    private Map<String, Room> rooms;
    private Map<String, Reservation> reservations;

    public RoomsAvailabilityHandler(Map<String, Room> rooms, Map<String, Reservation> reservations) {
        this.rooms = rooms;
        this.reservations = reservations;
    }

    @Override
    public void handle(ReservationRequest request) {
        LocalDate checkIn = request.getCheckIn();
        LocalDate checkOut = request.getCheckOut();

        List<Room> bookedRooms = reservations.values().stream()
                .filter(reservation ->
                        // Reservations that OVERLAP with requested dates
                        !reservation.getCheckOutDate().isBefore(checkIn) &&
                                !reservation.getCheckInDate().isAfter(checkOut))
                .flatMap(reservation -> reservation.getRooms().stream())
                .toList();

        List<Room> availableRooms = rooms.values().stream()
                .filter(room -> room.getRoomStatus() == RoomStatus.AVAILABLE)
                .filter(room -> bookedRooms.stream()
                        .noneMatch(booked -> booked.getId().equals(room.getId())))
                .toList();

        if (availableRooms.isEmpty()) {
            throw new HotelBookingException("Reservation request must have at least one room");
        }

        request.setAvailableRooms(availableRooms);

        handleNext(request);
    }
}
