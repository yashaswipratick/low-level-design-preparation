package com.lld.practice.tutor_sessions.elevator.model.requests;

import com.lld.practice.tutor_sessions.elevator.model.Direction;
import com.lld.practice.tutor_sessions.elevator.model.RequestStatus;

public class HallRequest extends Request {

    private int sourceFloor;
    private Direction direction;

    public HallRequest(String requestId, RequestStatus requestStatus, int sourceFloor, Direction direction) {
        super(requestId, requestStatus);
        this.sourceFloor = sourceFloor;
        this.direction = direction;
    }

    public int getSourceFloor() {
        return sourceFloor;
    }

    public void setSourceFloor(int sourceFloor) {
        this.sourceFloor = sourceFloor;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "HallRequest{" +
                "sourceFloor=" + sourceFloor +
                ", direction=" + direction +
                '}';
    }

    @Override
    public int getDestinationFloor() {
        return this.getSourceFloor();
    }
}
