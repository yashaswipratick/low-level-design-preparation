package com.lld.practice.tutor_sessions.hotel_reservation.store;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Notification;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Notification;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Reservation;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared in-memory data store for the hotel booking system.
 * Single instance is created in driver and injected into all services.
 * This ensures all services read/write the same data.
 */
public class HotelDataStore {

    private final Map<String, Reservation> reservations = new HashMap<>();
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, Notification> notifications = new HashMap<>();
    private final Map<String, ReentrantLock> roomLocks = new ConcurrentHashMap<>();

    public Map<String, Reservation> getReservations() {
        return reservations;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Notification> getNotifications() {
        return notifications;
    }
    
    public ConcurrentHashMap<String, ReentrantLock> getRoomLocks() {
        return (ConcurrentHashMap<String, ReentrantLock>) roomLocks;
    }
}

