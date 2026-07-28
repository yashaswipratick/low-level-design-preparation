package com.lld.practice.tutor_sessions.hotel_reservation.chain_of_responsibility;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Guest;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

import java.time.LocalDate;
import java.util.List;

public class ReservationRequest {

    List<Guest> guests;
    LocalDate checkIn;
    LocalDate checkOut;
    List<Room> availableRooms;  // populated by AvailabilityHandler, used by CapacityHandler

    public ReservationRequest(List<Guest> guests, LocalDate checkIn, LocalDate checkOut, List<Room> availableRooms) {
        this.guests = guests;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.availableRooms = availableRooms;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        this.guests = guests;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public List<Room> getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(List<Room> availableRooms) {
        this.availableRooms = availableRooms;
    }
}