package com.lld.practice.tutor_sessions.parking_lot.implementation.service;

import com.lld.practice.tutor_sessions.parking_lot.implementation.model.Slot;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.SlotType;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.Ticket;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.VehicleType;

public interface ParkingLotService {

    Ticket entry(String licensePlate, VehicleType vehicleType);         // ISSUED → returns ticket with slotNum + entryTime

    Ticket pay(String ticketId);               // ISSUED → PAID, computes fee, sets exitTime

    void exit(String ticketId);                // PAID → EXITED, releases slot

    Ticket reportLost(String ticketId);          // ISSUED → LOST

    Ticket payLostTicketPenalty(String ticketId);
}
