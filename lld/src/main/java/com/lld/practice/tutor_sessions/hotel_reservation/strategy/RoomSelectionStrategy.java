package com.lld.practice.tutor_sessions.hotel_reservation.strategy;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Guest;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

import java.util.List;

public interface RoomSelectionStrategy {

    List<Room> selectRooms(List<Room> availableRooms, List<Guest> guests);
}
