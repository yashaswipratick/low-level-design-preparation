package com.lld.practice.tutor_sessions.hotel_reservation.factory.impl;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomStatus;
import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomType;
import com.lld.practice.tutor_sessions.hotel_reservation.factory.RoomFactory;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

public class PremiumDeluxeRoomFactory implements RoomFactory {

    @Override
    public Room createRoom(String roomId, String roomNumber) {
        return new Room(roomId, roomNumber, RoomType.PREMIUM, 200.0, RoomStatus.AVAILABLE);
    }
}
