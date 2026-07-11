package com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle;

import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.impl.Bike;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.impl.Car;
import com.lld.practice.tutor_sessions.parking_lot.design_patterns.factory.vehicle.impl.Truck;
import com.lld.practice.tutor_sessions.parking_lot.implementation.model.VehicleType;

public class VehicleFactory {

    // What it should look like (pseudocode):
    public static Vehicle create(VehicleType type, String plate) {
        return switch(type) {
            case CAR   -> new Car(plate);
            case BIKE  -> new Bike(plate);
            case TRUCK -> new Truck(plate);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
    }
}
