package com.lld.practice.tutor_sessions.hotel_reservation.factory;

import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

public interface RoomFactory {

    Room createRoom(String roomId, String roomNumber);
}
