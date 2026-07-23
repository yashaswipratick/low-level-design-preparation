package com.lld.practice.tutor_sessions.hotel_reservation.service;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomType;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Guest;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

import java.time.LocalDate;
import java.util.List;

public interface HotelReservationService {

    List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut);

    Reservation reserve(List<Guest> guest, LocalDate checkIn, LocalDate checkOut);

    Reservation cancelReservation(String reservationId);

    Reservation modifyReservation(String reservationId, LocalDate newCheckIn, LocalDate newCheckOut);

    Reservation viewReservation(String reservationId);
}
