package com.lld.p1;

import com.lld.p1.model.CompletionEvent;
import com.lld.p1.model.Course;
import com.lld.p1.model.Timeframe;
import com.lld.p1.service.impl.TrendingCourseServiceImpl;

import java.time.Instant;

public class TrendingCourse {

    public static void main(String[] args) {
        TrendingCourseServiceImpl trendingCourseService = new TrendingCourseServiceImpl();
        Instant now = Instant.parse("2026-05-19T12:00:00Z");
        trendingCourseService.addCourse(new Course("C1", "Java"));
        trendingCourseService.addCourse(new Course("C2", "Python"));
        trendingCourseService.addCourse(new Course("C3", "Go"));

        trendingCourseService.addCompletionEvent(new CompletionEvent("U1", "C1", toUtcDate("2026-05-19T10:00:00Z")));
        trendingCourseService.addCompletionEvent(new CompletionEvent("U2", "C1", toUtcDate("2026-05-19T09:00:00Z")));
        trendingCourseService.addCompletionEvent(new CompletionEvent("U3", "C1", toUtcDate("2026-05-18T11:00:00Z")));
        trendingCourseService.addCompletionEvent(new CompletionEvent("U1", "C2", toUtcDate("2026-05-19T11:00:00Z")));
        trendingCourseService.addCompletionEvent(new CompletionEvent("U4", "C2", toUtcDate("2026-05-15T10:00:00Z")));
        trendingCourseService.addCompletionEvent(new CompletionEvent("U5", "C2", toUtcDate("2026-04-29T08:00:00Z")));
        trendingCourseService.addCompletionEvent(new CompletionEvent("U6", "C3", toUtcDate("2026-05-18T09:00:00Z")));
        trendingCourseService.addCompletionEvent(new CompletionEvent("U6", "C3", toUtcDate("2026-05-18T09:30:00Z")));
        trendingCourseService.addCompletionEvent(new CompletionEvent("U7", "C3", toUtcDate("2026-03-10T09:30:00Z")));

        System.out.println(trendingCourseService.getTopTrendingCourses(Timeframe.ONE_DAY, 3, now));
        System.out.println(trendingCourseService.getTopTrendingCourses(Timeframe.ONE_WEEK, 4, now));
        System.out.println(trendingCourseService.getTopTrendingCourses(Timeframe.ONE_MONTH, 5, now));
    }

    private static Instant toUtcDate(String isoInstant) {
        return Instant.parse(isoInstant);
    }
}
