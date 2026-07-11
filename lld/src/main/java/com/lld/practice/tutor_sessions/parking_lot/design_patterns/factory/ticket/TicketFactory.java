package com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.ticket;

import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.Vehicle;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.Ticket;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.TicketStatus;

import java.util.UUID;

import static java.time.LocalDateTime.now;

public class TicketFactory {

    public static Ticket issueTicket(Vehicle vehicle, Integer slotNumber) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(UUID.randomUUID().toString());
        ticket.setLicensePlateNumber(vehicle.getLicensePlate());
        ticket.setVehicleType(vehicle.getType());
        ticket.setSlotNumber(slotNumber);
        ticket.setEntry(now());
        ticket.setStatus(TicketStatus.ISSUED);
        ticket.setFee(0.0);
        return ticket;
    }
}
