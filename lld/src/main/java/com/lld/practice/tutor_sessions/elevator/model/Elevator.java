package com.lld.practice.tutor_sessions.elevator.model;

import com.lld.practice.tutor_sessions.elevator.model.requests.Request;

import java.util.List;

public class Elevator {

    private String elevatorId;
    private int floor;
    private ElevatorStatus elevatorStatus;
    private List<Request> requests;

    public Elevator(String elevatorId, int floor, ElevatorStatus elevatorStatus, List<Request> requests) {
        this.elevatorId = elevatorId;
        this.floor = floor;
        this.elevatorStatus = elevatorStatus;
        this.requests = requests;
    }

    public String getElevatorId() {
        return elevatorId;
    }

    public void setElevatorId(String elevatorId) {
        this.elevatorId = elevatorId;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public ElevatorStatus getElevatorStatus() {
        return elevatorStatus;
    }

    public void setElevatorStatus(ElevatorStatus elevatorStatus) {
        this.elevatorStatus = elevatorStatus;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    @Override
    public String toString() {
        return "Elevator{" +
                "elevatorId='" + elevatorId + '\'' +
                ", floor=" + floor +
                ", elevatorStatus=" + elevatorStatus +
                ", requests=" + requests +
                '}';
    }
}
