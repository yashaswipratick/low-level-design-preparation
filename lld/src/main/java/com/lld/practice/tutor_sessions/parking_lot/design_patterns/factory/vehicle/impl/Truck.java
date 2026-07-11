package com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.impl;

import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.Vehicle;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.VehicleType;

public class Truck implements Vehicle {

    private String licensePlate;

    public Truck(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    @Override
    public VehicleType getType() {
        return VehicleType.TRUCK;
    }

    @Override
    public String getLicensePlate() {
        return licensePlate;
    }
}
