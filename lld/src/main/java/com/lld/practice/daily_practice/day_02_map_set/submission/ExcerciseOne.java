package com.lld.practice.daily_practice.day_02_map_set.submission;

import java.util.*;
import java.util.stream.Collectors;

/*
 * ============================================================
 * DAY 2 - EXERCISE 1: Unique Students per Course
 * ============================================================
 * CONTEXT:
 *   A student may complete the same course multiple times.
 *   For leaderboard stats, count each student only once per course.
 *
 * METHOD:
 *   uniqueStudentsPerCourse(events)
 *
 * INPUT:
 *   List<String[]> where each element = {courseId, studentId}
 *
 * OUTPUT:
 *   Map<courseId, uniqueStudentCount>
 *
 * GOAL:
 *   Use collect-then-count pattern with Map<K, Set<V>> and computeIfAbsent.
 *
 * NOTE:
 *   Do not change test data in main. Implement the method and compare with expected output.
 * ============================================================
 */
public class ExcerciseOne {

    public static Map<String, Integer> uniqueStudentsPerCourse(List<String[]> events) {
        // TODO: Implement using Map<String, Set<String>> + computeIfAbsent + set.size()
        Map<String, Set<String>> studentsPerCourse = new HashMap<>();
        for (int i = 0; i < events.size(); i++) {
            studentsPerCourse
                    .computeIfAbsent(events.get(i)[0], key -> new HashSet<>())
                    .add(events.get(i)[1]);
        }
        return studentsPerCourse.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry->entry.getValue().size()));
    }

    public static void main(String[] args) {
        List<String[]> events = new ArrayList<>();
        events.add(new String[]{"C1", "S1"});
        events.add(new String[]{"C1", "S1"});
        events.add(new String[]{"C1", "S2"});
        events.add(new String[]{"C2", "S1"});

        Map<String, Integer> expected = new HashMap<>();
        expected.put("C1", 2);
        expected.put("C2", 1);

        System.out.println("Expected: " + expected);

        try {
            Map<String, Integer> actual = uniqueStudentsPerCourse(events);
            System.out.println("Actual:   " + actual);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Actual:   TODO not implemented yet");
        }
    }
}

