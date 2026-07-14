package com.lld.practice.tutor_sessions.elevator.service;

import com.lld.practice.tutor_sessions.elevator.ElevatorService;
import com.lld.practice.tutor_sessions.elevator.model.Elevator;
import com.lld.practice.tutor_sessions.elevator.model.ElevatorStatus;
import com.lld.practice.tutor_sessions.elevator.model.RequestStatus;
import com.lld.practice.tutor_sessions.elevator.model.requests.Request;

public class ElevatorServiceImpl implements ElevatorService {

    @Override
    public Request execute(Elevator elevator) {
        if (elevator.getRequests().isEmpty()) {
            elevator.setElevatorStatus(ElevatorStatus.IDLE);
            return null;
        }

        Request nextRequest = elevator.getRequests().get(0);
        int targetFloor = nextRequest.getDestinationFloor();

        if (elevator.getElevatorStatus() ==  ElevatorStatus.DOORS_OPEN) {
            nextRequest.setRequestStatus(RequestStatus.COMPLETED);
            elevator.getRequests().remove(nextRequest);
            elevator.setElevatorStatus(ElevatorStatus.IDLE);
            return nextRequest;
        }

        if (elevator.getFloor() == targetFloor) {
            elevator.setElevatorStatus(ElevatorStatus.DOORS_OPEN);
            return null;
        }

        if (elevator.getFloor() < targetFloor) {
            elevator.setElevatorStatus(ElevatorStatus.MOVING_UPWARDS);
            elevator.setFloor(elevator.getFloor() + 1);
        } else {
            elevator.setElevatorStatus(ElevatorStatus.MOVING_DOWNWARDS);
            elevator.setFloor(elevator.getFloor() - 1);
        }
        return null;
    }
}
