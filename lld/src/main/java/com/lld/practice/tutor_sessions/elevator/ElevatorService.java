package com.lld.practice.tutor_sessions.elevator;

import com.lld.practice.tutor_sessions.elevator.model.Elevator;
import com.lld.practice.tutor_sessions.elevator.model.requests.Request;

public interface ElevatorService {

    Request execute(Elevator elevator);
}
