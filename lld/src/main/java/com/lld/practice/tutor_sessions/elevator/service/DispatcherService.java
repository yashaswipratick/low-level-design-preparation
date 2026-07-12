package com.lld.practice.tutor_sessions.elevator.service;

import com.lld.practice.tutor_sessions.elevator.model.Direction;
import com.lld.practice.tutor_sessions.elevator.model.Elevator;
import com.lld.practice.tutor_sessions.elevator.model.ElevatorStatus;
import com.lld.practice.tutor_sessions.elevator.model.RequestStatus;
import com.lld.practice.tutor_sessions.elevator.model.requests.HallRequest;
import com.lld.practice.tutor_sessions.elevator.model.requests.Request;

import java.util.ArrayList;
import java.util.List;

public class DispatcherService {

    public Request dispatch(HallRequest request, List<Elevator> elevators) {

        // 1. Guard checks (you already have these - keep them)
        if (elevators == null || elevators.isEmpty()) {
            throw new IllegalArgumentException("elevators is null or empty");
        }

        if (request == null) {
            throw new IllegalArgumentException("request is null");
        }

        // 2. Filter out unavailable elevators
        List<Elevator> availableElevators = elevators.stream()
                .filter(elevator -> elevator.getElevatorStatus() != ElevatorStatus.OUT_OF_SERVICE)
                .toList();

        if (availableElevators.isEmpty()) {
            throw new IllegalArgumentException("available elevators is null or empty");
        }

        // 3. Find primary candidates: moving in same direction AND will cross request floor
        List<Elevator> primaryCandidate = new ArrayList<>();
        for (Elevator elevator : availableElevators) {
            boolean isMovingDirection = false;
            boolean willCrossFloor = false;

            if (request.getDirection().equals(Direction.UP) && elevator.getElevatorStatus() == ElevatorStatus.MOVING_UPWARDS) {
                isMovingDirection = true;
                willCrossFloor = elevator.getFloor() <= request.getDestinationFloor();
            } else if (request.getDirection().equals(Direction.DOWN) && elevator.getElevatorStatus() == ElevatorStatus.MOVING_DOWNWARDS) {
                isMovingDirection = true;
                willCrossFloor = elevator.getFloor() >= request.getDestinationFloor();
            }

            if (isMovingDirection && willCrossFloor) {
                primaryCandidate.add(elevator);
            }

            // 4. If primary candidates exist, select best one
            Elevator selectedElevator = null;
            if (!primaryCandidate.isEmpty()) {
                selectedElevator = findNearestElevator(primaryCandidate, request.getSourceFloor());
            } else {
                List<Elevator> idleElevator = availableElevators.stream().filter(details -> details.getElevatorStatus() == ElevatorStatus.IDLE).toList();
                if (!idleElevator.isEmpty()) {
                    selectedElevator = findNearestElevator(idleElevator, request.getSourceFloor());
                } else {
                    selectedElevator = findNearestElevator(availableElevators, request.getSourceFloor());
                }
            }
            if (selectedElevator != null) {
                selectedElevator.getRequests().add(request);
                request.setRequestStatus(RequestStatus.ASSIGNED);
            }
        }
        return request;
    }

    private Elevator findNearestElevator(List<Elevator> elevators, int targetFloor) {
        Elevator nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            int distance = Math.abs(elevator.getFloor() - targetFloor);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = elevator;
            } else if (distance == minDistance) {
                if (nearest == null || elevator.getElevatorId().compareTo(nearest.getElevatorId()) < 0) {
                    nearest = elevator;
                }
            }
        }
        return nearest;
    }
}
