package com.lld.p1.model;

import java.time.Duration;

public enum Timeframe {
    ONE_DAY(Duration.ofDays(1)),
    ONE_WEEK(Duration.ofDays(7)),
    ONE_MONTH(Duration.ofDays(30));

    private final Duration duration;

    Timeframe(Duration duration) {
        this.duration = duration;
    }

    public Duration getDuration() {
        return duration;
    }
}
