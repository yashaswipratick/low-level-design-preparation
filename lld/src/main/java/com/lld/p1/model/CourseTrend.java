package com.lld.p1.model;

public class CourseTrend {

    private Course course;
    private Integer count;

    public CourseTrend() {
    }

    public CourseTrend(Course course, Integer count) {
        this.course = course;
        this.count = count;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CourseTrend{" +
                "course=" + course +
                ", count=" + count +
                '}';
    }
}
