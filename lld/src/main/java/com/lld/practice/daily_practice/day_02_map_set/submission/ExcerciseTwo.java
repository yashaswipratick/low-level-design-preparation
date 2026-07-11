package com.lld.practice.daily_practice.day_02_map_set.submission;

import java.util.*;
import java.util.stream.Collectors;

/*
 * ============================================================
 * DAY 2 - EXERCISE 2: Unique Courses per Student
 * ============================================================
 * CONTEXT:
 *   A profile page shows how many distinct courses each student engaged with.
 *
 * METHOD:
 *   uniqueCoursesPerStudent(events)
 *
 * INPUT:
 *   List<String[]> where each element = {studentId, courseId}
 *
 * OUTPUT:
 *   Map<studentId, uniqueCourseCount>
 *
 * GOAL:
 *   Apply the same collect-then-count pattern as Exercise 1, with key/value reversed.
 * ============================================================
 */
public class ExcerciseTwo {

    public static Map<String, Integer> uniqueCoursesPerStudent(List<String[]> events) {
        // TODO: Implement using Map<String, Set<String>> + computeIfAbsent + set.size()
        Map<String, Set<String>> uniqueCoursePerStudent = new HashMap<>();

        for (int i = 0; i < events.size(); i++) {
            uniqueCoursePerStudent
                    .computeIfAbsent(events.get(i)[0], key -> new HashSet<>())
                    .add(events.get(i)[1]);
        }

        return uniqueCoursePerStudent
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
    }

    public static void main(String[] args) {
        List<String[]> events = new ArrayList<>();
        events.add(new String[]{"S1", "C1"});
        events.add(new String[]{"S1", "C1"});
        events.add(new String[]{"S1", "C2"});
        events.add(new String[]{"S2", "C2"});

        Map<String, Integer> expected = new HashMap<>();
        expected.put("S1", 2);
        expected.put("S2", 1);

        System.out.println("Expected: " + expected);

        try {
            Map<String, Integer> actual = uniqueCoursesPerStudent(events);
            System.out.println("Actual:   " + actual);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Actual:   TODO not implemented yet");
        }
    }
}

