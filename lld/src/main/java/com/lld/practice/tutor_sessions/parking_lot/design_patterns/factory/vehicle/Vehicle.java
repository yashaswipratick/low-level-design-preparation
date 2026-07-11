package com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle;

import com.lld.practice.tutor_sessions.parking_lot.implementation.model.VehicleType;

public interface Vehicle {

    VehicleType getType();
    String getLicensePlate();
}
