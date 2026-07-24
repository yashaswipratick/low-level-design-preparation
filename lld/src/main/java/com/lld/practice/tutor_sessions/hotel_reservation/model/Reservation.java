package com.lld.practice.tutor_sessions.hotel_reservation.model;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.state.ReservationState;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Reservation {

    private String id;
    private List<Guest> guests;
    private List<Room> rooms;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime  checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDate bookingDate;
    private ReservationStatus reservationStatus;
    private ReservationState reservationState;

    public Reservation(String id, List<Guest> guests, List<Room> rooms, LocalDate checkInDate, LocalDate checkOutDate, LocalDateTime checkInTime, LocalDateTime checkOutTime, LocalDate bookingDate, ReservationStatus reservationStatus, ReservationState reservationState) {
        this.id = id;
        this.guests = guests;
        this.rooms = rooms;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.bookingDate = bookingDate;
        this.reservationStatus = reservationStatus;
        this.reservationState = reservationState;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Guest> getGuests() {
        return guests;
    }

    public void setGuests(List<Guest> guests) {
        this.guests = guests;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void setReservationStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public ReservationState getReservationState() {
        return reservationState;
    }

    public void setReservationState(ReservationState reservationState) {
        this.reservationState = reservationState;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", guests=" + guests +
                ", rooms=" + rooms +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                ", bookingDate=" + bookingDate +
                ", reservationStatus=" + reservationStatus +
                '}';
    }
}
