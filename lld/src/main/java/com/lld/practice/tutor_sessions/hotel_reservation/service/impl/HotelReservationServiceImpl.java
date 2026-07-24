package com.lld.practice.tutor_sessions.hotel_reservation.service.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.ReservationStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Guest;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEvent;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEventPublisher;
import com.lld.practice.tutor_sessions.hotel_reservation.observer.ReservationEventType;
import com.lld.practice.tutor_sessions.hotel_reservation.service.HotelReservationService;
import com.lld.practice.tutor_sessions.hotel_reservation.state.impl.ReservedState;
import com.lld.practice.tutor_sessions.hotel_reservation.store.HotelDataStore;
import com.lld.practice.tutor_sessions.hotel_reservation.validator.HotelBookingServiceValidator;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class HotelReservationServiceImpl implements HotelReservationService {

    private final Map<String, Reservation> reservations;
    private final Map<String, Room> rooms;
    private final ConcurrentHashMap<String, ReentrantLock> roomLocks;
    private final ReservationEventPublisher reservationEventPublisher;
    private final ReentrantLock lock = new ReentrantLock(); // global lock for non-room operations

    // HotelDataStore is injected — same instance shared across all services
    public HotelReservationServiceImpl(HotelDataStore dataStore, ReservationEventPublisher reservationEventPublisher) {
        this.reservations = dataStore.getReservations();
        this.rooms = dataStore.getRooms();
        this.roomLocks = dataStore.getRoomLocks();
        this.reservationEventPublisher = reservationEventPublisher;
    }


    @Override
    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
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

        // Step 1: Find available rooms (no lock needed — just a snapshot)
        List<Room> availableRooms = searchAvailableRooms(checkIn, checkOut);
        if (availableRooms.isEmpty()) {
            throw new HotelBookingException("No available rooms for the given dates");
        }

        int numberOfRooms = (int) Math.ceil(guest.size() / 2.0);
        // Step 2: Pick candidate rooms
        List<Room> candidateRooms = new ArrayList<>(availableRooms.subList(0, numberOfRooms));

        // Step 3: Sort by roomId — DEADLOCK PREVENTION
        // Always acquire locks in same order across all threads
        candidateRooms.sort(Comparator.comparing(Room::getId));

        // Step 4: Acquire per-room locks in sorted order
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        try {
            for (Room room : candidateRooms) {
                // computeIfAbsent is atomic — creates lock if not present
                ReentrantLock roomLock = roomLocks.computeIfAbsent(room.getId(), id -> new ReentrantLock());
                roomLock.lock();
                acquiredLocks.add(roomLock);
            }

            // Step 5: Double-check availability under lock
            // Another thread may have booked a room between step 1 and step 4
            for (Room room : candidateRooms) {
                if (room.getRoomStatus() != RoomStatus.AVAILABLE) {
                    throw new HotelBookingException("Room " + room.getId() + " was just booked by another guest. Please search again.");
                }
            }

            // Step 6: All rooms still available — book them
            Reservation reservation = new Reservation(UUID.randomUUID().toString(),
                    guest, candidateRooms, checkIn, checkOut, null, null,
                    LocalDate.now(), ReservationStatus.RESERVED, new ReservedState());
            candidateRooms.forEach(room -> room.setRoomStatus(RoomStatus.BOOKED));
            reservations.put(reservation.getId(), reservation);
            reservationEventPublisher.publish(new ReservationEvent(reservation, ReservationEventType.RESERVED));
            return reservation;

        } finally {
            // Step 7: Always release ALL acquired locks
            acquiredLocks.forEach(ReentrantLock::unlock);
        }
    }

    @Override
    public Reservation cancelReservation(String reservationId) {
        // Step 1: Input validation — outside lock (fast fail)
        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is null");
        }

        // Step 2: Find reservation using global lock (safe map read)
        Reservation reservation;
        lock.lock();
        try {
            if (reservations.isEmpty()) {
                throw new HotelBookingException("No reservations found");
            }
            if (!reservations.containsKey(reservationId)) {
                throw new HotelBookingException("reservationId not found");
            }
            reservation = reservations.get(reservationId);
        } finally {
            lock.unlock();
        }

        // Step 3: Guard check — delegate to State pattern BEFORE acquiring room locks
        // Throws HotelBookingException immediately if CHECKED_OUT or already CANCELLED
        reservation.getReservationState().cancel(reservation);

        // Step 4: Sort rooms by id — DEADLOCK PREVENTION
        List<Room> roomsToFree = new ArrayList<>(reservation.getRooms());
        roomsToFree.sort(Comparator.comparing(Room::getId));

        // Step 5: Acquire per-room locks in sorted order
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        try {
            for (Room room : roomsToFree) {
                ReentrantLock roomLock = roomLocks.computeIfAbsent(room.getId(), id -> new ReentrantLock());
                roomLock.lock();
                acquiredLocks.add(roomLock);
            }

            // Step 6: Double-check reservation still exists under lock
            // Another thread may have already cancelled it
            if (!reservations.containsKey(reservationId)) {
                throw new HotelBookingException("Reservation already cancelled by another operation");
            }

            // Step 7: Free rooms (state already transitioned in Step 3)
            roomsToFree.forEach(room -> room.setRoomStatus(RoomStatus.AVAILABLE));
            reservations.remove(reservationId);
            reservationEventPublisher.publish(new ReservationEvent(reservation, ReservationEventType.CANCELLED));
            return reservation;
        } finally {
            // Step 8: Always release all room locks
            acquiredLocks.forEach(ReentrantLock::unlock);
        }
    }

    @Override
    public Reservation modifyReservation(String reservationId, LocalDate newCheckIn, LocalDate newCheckOut) {
        // Step 1: Input validation — outside lock (fast fail)
        if (reservationId == null) {
            throw new IllegalArgumentException("reservationId is null");
        }

        // Step 2: Find old reservation using global lock (safe map read)
        Reservation oldReservation;
        lock.lock();
        try {
            if (reservations.isEmpty()) {
                throw new IllegalArgumentException("No reservations found");
            }
            if (!reservations.containsKey(reservationId)) {
                throw new IllegalArgumentException("reservationId not found");
            }
            oldReservation = reservations.get(reservationId);
        } finally {
            lock.unlock();
        }

        // Step 3: Guard check — delegate to State pattern BEFORE acquiring room locks
        // Throws HotelBookingException immediately if CHECKED_IN, CHECKED_OUT, or CANCELLED
        oldReservation.getReservationState().modify(oldReservation, newCheckIn, newCheckOut);

        // Step 4: Sort OLD rooms by id — DEADLOCK PREVENTION
        List<Room> oldRooms = new ArrayList<>(oldReservation.getRooms());
        oldRooms.sort(Comparator.comparing(Room::getId));

        // Step 5: Acquire per-room locks for OLD rooms
        List<ReentrantLock> acquiredLocks = new ArrayList<>();
        try {
            for (Room room : oldRooms) {
                ReentrantLock roomLock = roomLocks.computeIfAbsent(room.getId(), id -> new ReentrantLock());
                roomLock.lock();
                acquiredLocks.add(roomLock);
            }

            // Step 5: Double-check reservation still exists under lock
            if (!reservations.containsKey(reservationId)) {
                throw new HotelBookingException("Reservation was modified or cancelled by another operation");
            }

            // Step 6: Free old rooms and remove old reservation
            oldRooms.forEach(room -> room.setRoomStatus(RoomStatus.AVAILABLE));
            reservations.remove(reservationId);

        } finally {
            // Step 7: Release OLD room locks BEFORE calling reserve()
            // This avoids holding old locks while acquiring new locks (potential deadlock)
            acquiredLocks.forEach(ReentrantLock::unlock);
        }

        // Step 8: Create new reservation for new dates (reserve() handles its own per-room locks)
        Reservation newReservation = reserve(oldReservation.getGuests(), newCheckIn, newCheckOut);
        reservationEventPublisher.publish(new ReservationEvent(newReservation, ReservationEventType.MODIFIED));
        return newReservation;
    }

    @Override
    public Reservation viewReservation(String reservationId) {
        lock.lock();
        try {
            if (reservationId == null) {
                throw new IllegalArgumentException("reservationId is null");
            }

            if (reservations.isEmpty()) {
                throw new IllegalArgumentException("No reservations found");
            }

            if (!reservations.containsKey(reservationId)) {
                throw new IllegalArgumentException("reservationId not found");
            }
            return reservations.get(reservationId);
        } finally {
            lock.unlock();
        }
    }
}
