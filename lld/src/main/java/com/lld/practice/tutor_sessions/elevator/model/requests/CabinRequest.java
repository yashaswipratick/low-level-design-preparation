package com.lld.practice.tutor_sessions.elevator.model.requests;

import com.lld.practice.tutor_sessions.elevator.model.RequestStatus;

public class CabinRequest extends  Request{

    private int destinationFloor;

    public CabinRequest(String requestId, RequestStatus requestStatus, int destinationFloor) {
        super(requestId, requestStatus);
        this.destinationFloor = destinationFloor;
    }

    @Override
    public int getDestinationFloor() {
        return this.destinationFloor;
    }

    public void setDestinationFloor(int destinationFloor) {
        this.destinationFloor = destinationFloor;
    }

    @Override
    public String toString() {
        return "CabinRequest{" +
                "destinationFloor=" + destinationFloor +
                '}';
    }
}
