package com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.impl;

import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.Vehicle;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.VehicleType;

public class Car implements Vehicle {

    private String licensePlate;

    public Car(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    @Override
    public VehicleType getType() {
        return VehicleType.CAR;
    }

    @Override
    public String getLicensePlate() {
        return licensePlate;
    }
}
