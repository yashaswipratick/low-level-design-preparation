package com.lld.practice.tutor_sessions.parking_lot.implementation.model;

import java.util.List;

public class ParkingLot {

    private String id;
    private List<Floor> floor;

    public ParkingLot(String id, List<Floor> floor) {
        this.id = id;
        this.floor = floor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Floor> getFloor() {
        return floor;
    }

    public void setFloor(List<Floor> floor) {
        this.floor = floor;
    }

    @Override
    public String toString() {
        return "ParkingLot{" +
                "id='" + id + '\'' +
                ", floor=" + floor +
                '}';
    }
}
