package com.lld.p1.service;

import com.lld.p1.model.CourseTrend;
import com.lld.p1.model.Timeframe;

import java.time.Instant;
import java.util.List;

public interface TrendingCourseService {
    List<CourseTrend> getTopTrendingCourses(Timeframe timeframe, int topN, Instant now);
}
