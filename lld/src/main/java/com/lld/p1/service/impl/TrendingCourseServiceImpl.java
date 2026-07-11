package com.lld.p1.service.impl;

import com.lld.p1.model.CompletionEvent;
import com.lld.p1.model.Course;
import com.lld.p1.model.CourseTrend;
import com.lld.p1.model.Timeframe;
import com.lld.p1.service.TrendingCourseService;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class TrendingCourseServiceImpl implements TrendingCourseService {

    private final List<CompletionEvent> completionEvents = new ArrayList<>();
    private final Map<String, Course> courseMap = new HashMap<>();

    @Override
    public List<CourseTrend> getTopTrendingCourses(Timeframe timeframe, int topN, Instant now) {
        if (topN <= 0 || timeframe == null || now == null) {
            return Collections.emptyList();
        }

        Instant windowStart = now.minus(timeframe.getDuration());
        Map<String, Set<String>> uniqueUsersPerCourse = new HashMap<>();

        for (CompletionEvent event : completionEvents) {
            if (event == null || event.getCourseId() == null || event.getUserId() == null || event.getCompletedAt() == null) {
                continue;
            }

            if (!courseMap.containsKey(event.getCourseId())) {
                continue;
            }

            Instant completedAt = event.getCompletedAt();
            if (completedAt.isBefore(windowStart) || completedAt.isAfter(now)) {
                continue;
            }

            uniqueUsersPerCourse
                    .computeIfAbsent(event.getCourseId(), key -> new HashSet<>())
                    .add(event.getUserId());
        }

        return uniqueUsersPerCourse.entrySet().stream()
                .map(entry -> new CourseTrend(courseMap.get(entry.getKey()), entry.getValue().size()))
                .sorted(Comparator.comparingInt(CourseTrend::getCount).reversed()
                        .thenComparing(courseTrend -> courseTrend.getCourse().getCourseName()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    public void addCourse(Course course) {
        if (course == null || course.getCourseId() == null) {
            return;
        }
        courseMap.put(course.getCourseId(), course);
    }

    public void addCompletionEvent(CompletionEvent event) {
        if (event == null) {
            return;
        }
        completionEvents.add(event);
    }
}
