package com.lld.practice.tutor_sessions.hotel_reservation.chain_of_responsibility;

import com.lld.practice.tutor_sessions.hotel_reservation.exception.HotelBookingException;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Guest;
import com.lld.practice.tutor_sessions.hotel_reservation.model.Room;

import java.util.List;

public class RoomsCapacityHandler extends ReservationHandler {

    @Override
    public void handle(ReservationRequest request) {
        List<Room> availableRooms = request.getAvailableRooms();
        List<Guest> guests = request.getGuests();

        int neededRooms = (int)Math.ceil(guests.size()/ 2.0);

        if (availableRooms.size() < neededRooms) {
            throw new HotelBookingException("Not enough rooms available for the number of guests");
        }
        handleNext(request);
    }
}
