package com.lld.practice.tutor_sessions.hotel_reservation.validator;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomType;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

import java.time.LocalDate;
import java.util.Map;

public class HotelBookingServiceValidator {

    public static String validateSearchRooms(LocalDate checkIn, LocalDate checkOut, Map<String, Room>  rooms, Map<String, Reservation> reservations) {
        String validationError = null;
        if (checkIn.isAfter(checkOut)) {
            validationError = "Check-in date cannot be after check-out date.";
        } else if (checkIn.isBefore(LocalDate.now())) {
            validationError = "Check-in date cannot be before check-in date.";
        } else if (checkOut.isBefore(LocalDate.now())) {
            validationError = "Check-out date cannot be before check-in date.";
        } else if (checkIn.isEqual(checkOut)) {
            validationError = "Check-in date cannot be equals check-in date.";
        } else if (rooms.isEmpty()) {
            validationError = "Rooms cannot be empty.";
        }
        return validationError;
    }
}
