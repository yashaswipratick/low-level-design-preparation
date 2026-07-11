# Day 4 — Enum with Behavior

## What is covered
- 3 exercise files in `submission/`
- All 3 enum design patterns used in real LLD problems
- Method signatures with `TODO` stubs — **no logic implemented**
- Deterministic test cases in each `main` method

## The 3 Patterns (in order of difficulty)

| File | Pattern | What the Enum Does |
|---|---|---|
| `ExcerciseOne.java`   | Pattern 1 — Field + Getter | Carries data, no logic |
| `ExcerciseTwo.java`   | Pattern 2 — Behavior Method | Carries data AND computes with it |
| `ExcerciseThree.java` | Pattern 3 — Abstract Method | Each constant has its own different logic |

## The Golden Rule
**If you find yourself writing `if (enum == X)` in a service — stop.**
The logic belongs inside the enum.

## Key Syntax to Learn

```java
// Pattern 1 — field + getter
enum Timeframe {
    WEEKLY(7), MONTHLY(30);
    private final int days;
    Timeframe(int days) { this.days = days; }
    public int getDays() { return days; }
}

// Pattern 2 — shared behavior method using field
enum MembershipTier {
    GOLD(20);
    private final int discountPercent;
    MembershipTier(int d) { this.discountPercent = d; }
    public double applyDiscount(double price) {
        return price * (100 - discountPercent) / 100.0;
    }
}

// Pattern 3 — abstract method, each constant overrides
enum ShippingMode {
    STANDARD {
        @Override
        public double calculateCost(double kg) { return 50 + 10 * kg; }
    };
    public abstract double calculateCost(double kg);
}
```

## Package
```
com.lld.practice.daily_practice.day_04_enum_behavior.submission
```

## Compile
```bash
cd /Users/y0p03mn/preparation/src
javac com/lld/practice/day_04_enum_behavior/submission/*.java
```

## Run
```bash
cd /Users/y0p03mn/preparation/src
java com.lld.practice.daily_practice.day_04_enum_behavior.submission.ExcerciseOne
java com.lld.practice.daily_practice.day_04_enum_behavior.submission.ExcerciseTwo
java com.lld.practice.daily_practice.day_04_enum_behavior.submission.ExcerciseThree
```

## How to practice
1. Open one exercise file.
2. Read the problem statement fully — pay attention to the RULES section.
3. Implement **only** the parts marked `TODO`.
4. Run `main` and check the output matches expected values.
5. Repeat for all three exercises.

## Connection to P1
In P1 Trending Courses, `Timeframe` should use Pattern 1.
Instead of: `if (timeframe == WEEKLY) return 7;`
Write: `timeframe.getDays()` — and let the enum carry the value.

