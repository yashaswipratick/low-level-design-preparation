package com.lld.practice.daily_practice.day_02_map_set.submission;

import java.util.*;
import java.util.stream.Collectors;

/*
 * ============================================================
 * DAY 2 - EXERCISE 4: Unique Skills per Department
 * ============================================================
 * CONTEXT:
 *   You receive records (department, employee, skill).
 *   Report how many distinct skills each department has.
 *
 * METHOD:
 *   uniqueSkillsPerDepartment(records)
 *
 * INPUT:
 *   List<String[]> where each element = {departmentId, employeeId, skill}
 *
 * OUTPUT:
 *   Map<departmentId, uniqueSkillCount>
 *
 * BOUNDARY RULE:
 *   If the same skill appears multiple times in a department, count it once.
 *
 * GOAL:
 *   Build department -> set of skills, then convert to counts.
 * ============================================================
 */
public class ExcerciseFour {

    public static Map<String, Integer> uniqueSkillsPerDepartment(List<String[]> records) {
        // TODO: Implement using Map<String, Set<String>> + computeIfAbsent + set.size()
        Map<String, Set<String>> map = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            map.computeIfAbsent(records.get(i)[0], key -> new HashSet<>()).add(records.get(i)[2]);
        }

        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
    }

    public static void main(String[] args) {
        List<String[]> records = new ArrayList<>();
        records.add(new String[]{"D1", "E1", "Java"});
        records.add(new String[]{"D1", "E2", "Java"});
        records.add(new String[]{"D1", "E2", "SQL"});
        records.add(new String[]{"D2", "E3", "Python"});

        Map<String, Integer> expected = new HashMap<>();
        expected.put("D1", 2);
        expected.put("D2", 1);

        System.out.println("Expected: " + expected);

        try {
            Map<String, Integer> actual = uniqueSkillsPerDepartment(records);
            System.out.println("Actual:   " + actual);
        } catch (UnsupportedOperationException ex) {
            System.out.println("Actual:   TODO not implemented yet");
        }
    }
}

