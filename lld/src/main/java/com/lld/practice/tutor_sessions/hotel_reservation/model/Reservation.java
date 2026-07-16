package com.lld.practice.tutor_sessions.hotel_reservation.model;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Reservation {

    private String id;
    private Guest guest;
    private List<Room> rooms;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime  checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDate bookingDate;
    private ReservationStatus reservationStatus;

    public Reservation(String id, Guest guest, List<Room> rooms, LocalDate bookingDate, ReservationStatus reservationStatus) {
        this.id = id;
        this.guest = guest;
        this.rooms = rooms;
        this.bookingDate = bookingDate;
        this.reservationStatus = reservationStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
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

    @Override
    public String toString() {
        return "Reservation{" +
                "id='" + id + '\'' +
                ", guest=" + guest +
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
