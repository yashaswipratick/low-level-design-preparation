package com.lld.practice.tutor_sessions.hotel_reservation.factory;

import com.lld.practice.tutor_sessions.hotel_reservation.enums.RoomType;
import com.lld.practice.tutor_sessions.hotel_reservation.factory.impl.DeluxeRoomFactory;
import com.lld.practice.tutor_sessions.hotel_reservation.factory.impl.PremiumDeluxeRoomFactory;
import com.lld.practice.tutor_sessions.hotel_reservation.factory.impl.SuperDeluxeRoomFactory;

public class RoomFactoryProvider {

    public static RoomFactory getRoomFactory(RoomType roomType) {
        switch (roomType) {
            case SUPER_DELUXE:
                return new SuperDeluxeRoomFactory();
            case DELUXE:
                return new DeluxeRoomFactory();
            case PREMIUM:
                return new PremiumDeluxeRoomFactory();
            default:
                throw new RuntimeException("No Room Factory found for type: " + roomType);
        }
    }
}
