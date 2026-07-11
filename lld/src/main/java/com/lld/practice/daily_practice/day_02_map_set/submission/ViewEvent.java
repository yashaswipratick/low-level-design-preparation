package com.lld.practice.daily_practice.day_02_map_set.submission;

import java.time.Instant;

public class ViewEvent {
    private final String courseId;
    private final String userId;
    private final Instant viewedAt;

    public ViewEvent(String courseId, String userId, Instant viewedAt) {
        this.courseId = courseId;
        this.userId = userId;
        this.viewedAt = viewedAt;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getViewedAt() {
        return viewedAt;
    }
}

