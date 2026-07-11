package com.lld.practice.tutor_sessions.elevator.model;

import java.util.List;

public class Building {

    private String buildingId;
    private String buildingName;
    private List<Floor>  floors;
    private List<Elevator> elevators;

    public Building(String buildingId, String buildingName, List<Floor> floors, List<Elevator> elevators) {
        this.buildingId = buildingId;
        this.buildingName = buildingName;
        this.floors = floors;
        this.elevators = elevators;
    }

    public String getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    public List<Elevator> getElevators() {
        return elevators;
    }

    public void setElevators(List<Elevator> elevators) {
        this.elevators = elevators;
    }

    @Override
    public String toString() {
        return "Building{" +
                "buildingId='" + buildingId + '\'' +
                ", buildingName='" + buildingName + '\'' +
                ", floors=" + floors +
                ", elevators=" + elevators +
                '}';
    }
}
