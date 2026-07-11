# Day 5 - Input Model vs Output Model Separation

This folder contains Day 5 boilerplate exercises.

## What is included
- 4 exercise files in `submission/`
- Problem statements in each file
- Separate Input/Event, Domain, and Output/API models in every exercise
- Method signatures with `TODO` stubs (no logic implemented)
- Deterministic test cases in each `main` method

## Package
All Java files use:
`com.lld.practice.daily_practice.day_05_model_separation.submission`

## Compile
```bash
cd /Users/y0p03mn/preparation/src
javac com/lld/practice/day_05_model_separation/submission/*.java
```

## Run
```bash
cd /Users/y0p03mn/preparation/src
java com.lld.practice.daily_practice.day_05_model_separation.submission.ExcerciseOne
java com.lld.practice.daily_practice.day_05_model_separation.submission.ExcerciseTwo
java com.lld.practice.daily_practice.day_05_model_separation.submission.ExcerciseThree
java com.lld.practice.daily_practice.day_05_model_separation.submission.ExcerciseFour
```

## Practice checklist
- [ ] Ex1: Basic event -> summary separation
- [ ] Ex2: Dedupe in domain, clean ranking DTO
- [ ] Ex3: Time-window filtering + response projection
- [ ] Ex4: Validation + top-N response wrapper

## Separation rule to remember
- Input model: raw data shape coming into system
- Domain model: internal computation/aggregation shape
- Output model: clean API response shape

Never put raw events inside output DTOs.

