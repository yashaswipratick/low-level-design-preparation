package com.lld.practice.tutor_sessions.elevator;

import com.lld.practice.tutor_sessions.elevator.model.*;
import com.lld.practice.tutor_sessions.elevator.model.requests.HallRequest;
import com.lld.practice.tutor_sessions.elevator.model.requests.Request;
import com.lld.practice.tutor_sessions.elevator.service.DispatcherService;
import com.lld.practice.tutor_sessions.elevator.service.ElevatorServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Test driver for Elevator System
 * Scenario: 2 elevators, 3 hall requests
 */
public class ElevatorSystemDriver {

    public static void main(String[] args) {
        System.out.println("=== ELEVATOR SYSTEM TEST ===\n");

        // Initialize system
        DispatcherService dispatcher = new DispatcherService();
        ElevatorServiceImpl elevatorService = new ElevatorServiceImpl();

        // Create 2 elevators
        Elevator elevator1 = new Elevator("E1", 0, ElevatorStatus.IDLE, new ArrayList<>());
        Elevator elevator2 = new Elevator("E2", 5, ElevatorStatus.IDLE, new ArrayList<>());
        List<Elevator> elevators = List.of(elevator1, elevator2);

        System.out.println("Initial State:");
        printElevatorState(elevator1);
        printElevatorState(elevator2);
        System.out.println();

        // Test Scenario: 3 hall requests
        HallRequest req1 = new HallRequest("R1", RequestStatus.PENDING, 3, Direction.UP);
        HallRequest req2 = new HallRequest("R2", RequestStatus.PENDING, 7, Direction.DOWN);
        HallRequest req3 = new HallRequest("R3", RequestStatus.PENDING, 1, Direction.UP);

        System.out.println("=== TEST 1: Dispatch Request R1 (Floor 3, UP) ===");
        try {
            Request assigned1 = dispatcher.dispatch(req1, elevators);
            System.out.println("✓ Request " + assigned1.getRequestId() + " dispatched");
            System.out.println("  Status: " + assigned1.getRequestStatus());
            printElevatorState(elevator1);
            printElevatorState(elevator2);
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
        }
        System.out.println();

        System.out.println("=== TEST 2: Dispatch Request R2 (Floor 7, DOWN) ===");
        try {
            Request assigned2 = dispatcher.dispatch(req2, elevators);
            System.out.println("✓ Request " + assigned2.getRequestId() + " dispatched");
            System.out.println("  Status: " + assigned2.getRequestStatus());
            printElevatorState(elevator1);
            printElevatorState(elevator2);
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
        }
        System.out.println();

        System.out.println("=== TEST 3: Dispatch Request R3 (Floor 1, UP) ===");
        try {
            Request assigned3 = dispatcher.dispatch(req3, elevators);
            System.out.println("✓ Request " + assigned3.getRequestId() + " dispatched");
            System.out.println("  Status: " + assigned3.getRequestStatus());
            printElevatorState(elevator1);
            printElevatorState(elevator2);
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
        }
        System.out.println();

        // Execute elevator movement for E1
        System.out.println("=== TEST 4: Execute Elevator E1 (should move toward floor 3) ===");
        Elevator e1 = findElevatorById(elevators, "E1");
        if (e1 != null && !e1.getRequests().isEmpty()) {
            Request completed = elevatorService.execute(e1);
            System.out.println("Execution result: " + (completed != null ? "Completed " + completed.getRequestId() : "In progress"));
            printElevatorState(e1);
        } else {
            System.out.println("E1 has no requests");
        }
        System.out.println();

        // Simulate full execution cycle for E1
        System.out.println("=== TEST 5: Full execution cycle for E1 ===");
        int maxSteps = 10;
        int step = 0;
        while (!e1.getRequests().isEmpty() && step < maxSteps) {
            step++;
            Request completed = elevatorService.execute(e1);
            System.out.println("Step " + step + ": " + e1.getElevatorStatus() + " at floor " + e1.getFloor());
            if (completed != null) {
                System.out.println("  ✓ Completed request: " + completed.getRequestId() + " (status: " + completed.getRequestStatus() + ")");
            }
        }
        printElevatorState(e1);
        System.out.println();

        // Test edge case: dispatch with no available elevators
        System.out.println("=== TEST 6: Dispatch with all elevators OUT_OF_SERVICE ===");
        elevator1.setElevatorStatus(ElevatorStatus.OUT_OF_SERVICE);
        elevator2.setElevatorStatus(ElevatorStatus.OUT_OF_SERVICE);
        HallRequest req4 = new HallRequest("R4", RequestStatus.PENDING, 2, Direction.UP);
        try {
            dispatcher.dispatch(req4, elevators);
            System.out.println("✗ FAILED: Should have thrown exception");
        } catch (Exception e) {
            System.out.println("✓ Expected exception caught: " + e.getClass().getSimpleName());
            System.out.println("  Message: " + e.getMessage());
        }
        System.out.println();

        System.out.println("=== ALL TESTS COMPLETE ===");
    }

    private static void printElevatorState(Elevator elevator) {
        System.out.println("  " + elevator.getElevatorId() + ": Floor " + elevator.getFloor() + 
                          ", Status: " + elevator.getElevatorStatus() + 
                          ", Requests: " + elevator.getRequests().size());
    }

    private static Elevator findElevatorById(List<Elevator> elevators, String id) {
        return elevators.stream()
                .filter(e -> e.getElevatorId().equals(id))
                .findFirst()
                .orElse(null);
    }
}

