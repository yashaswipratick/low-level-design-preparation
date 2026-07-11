package com.lld.practice.tutor_sessions.elevator.model.requests;

import com.lld.practice.tutor_sessions.elevator.model.RequestStatus;

public abstract class Request {

    private String requestId;
    private RequestStatus requestStatus;
    public abstract int getDestinationFloor();

    public Request(String requestId, RequestStatus requestStatus) {
        this.requestId = requestId;
        this.requestStatus = requestStatus;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestId=" + requestId +
                ", requestStatus=" + requestStatus +
                '}';
    }
}
