# Day 3 — Comparator + Multi-Key Sort

## What is covered
- 5 exercise files in `submission/`
- Problem statements with full context in each file
- Method signatures with `TODO` stubs — **no logic implemented**
- Deterministic test cases in each `main` method with PASS/FAIL checks

## Exercises Overview

| File | Topic | Difficulty |
|---|---|---|
| `ExcerciseOne.java`   | Single-key sort (count DESC) | Beginner |
| `ExcerciseTwo.java`   | Multi-key sort (count DESC + name ASC) | Beginner-Intermediate |
| `ExcerciseThree.java` | Sort custom objects with method references | Intermediate |
| `ExcerciseFour.java`  | Three-key sort (score + category + name) | Intermediate-Hard |
| `ExcerciseFive.java`  | Full ranking pipeline (filter + sort + limit) | Hard |

## Key APIs to Learn Today

```java
// Single key descending
list.sort(Comparator.comparingInt(o -> -Integer.parseInt(o[1])));

// Or using .reversed()
list.sort(Comparator.comparingInt((String[] o) -> Integer.parseInt(o[1])).reversed());

// Multi-key chain
list.sort(Comparator.comparingInt(Obj::getScore).reversed()
                    .thenComparing(Obj::getName));

// Three keys
list.sort(Comparator.comparingInt(Obj::getScore).reversed()
                    .thenComparing(Obj::getCategory)
                    .thenComparing(Obj::getName));

// Full pipeline with streams
list.stream()
    .filter(o -> o.getCount() >= minCount)
    .sorted(Comparator.comparingInt(Obj::getCount).reversed().thenComparing(Obj::getId))
    .limit(topN)
    .map(Obj::getId)
    .collect(Collectors.toList());
```

## Package
```
com.lld.practice.daily_practice.day_03_comparator.submission
```

## Compile
```bash
cd /Users/y0p03mn/preparation/src
javac com/lld/practice/day_03_comparator/submission/*.java
```

## Run
```bash
cd /Users/y0p03mn/preparation/src
java com.lld.practice.daily_practice.day_03_comparator.submission.ExcerciseOne
java com.lld.practice.daily_practice.day_03_comparator.submission.ExcerciseTwo
java com.lld.practice.daily_practice.day_03_comparator.submission.ExcerciseThree
java com.lld.practice.daily_practice.day_03_comparator.submission.ExcerciseFour
java com.lld.practice.daily_practice.day_03_comparator.submission.ExcerciseFive
```

## How to practice
1. Open one exercise file.
2. Read the problem statement fully.
3. Implement **only** the method body marked `TODO`.
4. Run the `main` method and check PASS/FAIL output.
5. Repeat for all five exercises.

