package com.lld.p1.model;

import java.time.Instant;

public class CompletionEvent {

    private String userId;
    private String courseId;
    private Instant completedAt;

    public CompletionEvent() {
    }

    public CompletionEvent(String userId, String courseId, Instant completedAt) {
        this.userId = userId;
        this.courseId = courseId;
        this.completedAt = completedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public String toString() {
        return "CompletionEvent{" +
                "userId='" + userId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", completedAt='" + completedAt + '\'' +
                '}';
    }
}
