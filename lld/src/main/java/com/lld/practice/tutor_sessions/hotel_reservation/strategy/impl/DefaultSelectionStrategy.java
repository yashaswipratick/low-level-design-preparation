package com.lld.practice.tutor_sessions.hotel_reservation.strategy.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Guest;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;
import com.lld.practice.tutor_sessions.hotel_reservation.strategy.RoomSelectionStrategy;

import java.util.List;

public class DefaultSelectionStrategy implements RoomSelectionStrategy {

    @Override
    public List<Room> selectRooms(List<Room> availableRooms, List<Guest> guests) {
        // Default strategy: Select the first available room for each guest
        int numberOfRooms = (int) Math.ceil(guests.size() / 2.0);
        return availableRooms.subList(0, numberOfRooms);
    }
}
