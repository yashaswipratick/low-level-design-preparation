package com.lld.practice.daily_practice.day_04_enum_behavior.submission;

/*
 * ============================================================
 * DAY 4 - EXERCISE 3: Enum with Abstract Method (Pattern 3)
 * ============================================================
 * DIFFICULTY: Hard
 * TOPIC: Enum where each constant has a DIFFERENT implementation
 *        of the same abstract method — zero if/else anywhere
 *
 * CONTEXT:
 *   Pattern 1: enum carries data (field + getter)
 *   Pattern 2: enum has a shared method using its field
 *   Pattern 3: enum has an abstract method — each constant
 *              implements it DIFFERENTLY, like polymorphism.
 *
 *   Real use case: a shipping calculator where each ShippingMode
 *   computes cost differently. The service just calls:
 *     mode.calculateCost(weightKg)
 *   and each constant handles its own formula.
 *
 * YOUR TASK:
 *   Build a ShippingMode enum where each constant computes
 *   shipping cost using its own formula:
 *
 *   STANDARD  → flat ₹50 + ₹10 per kg
 *   EXPRESS   → flat ₹100 + ₹25 per kg
 *   OVERNIGHT → flat ₹200 + ₹50 per kg
 *
 * ENUM TO COMPLETE:
 *   enum ShippingMode {
 *       STANDARD {
 *           // TODO: implement calculateCost
 *       },
 *       EXPRESS {
 *           // TODO: implement calculateCost
 *       },
 *       OVERNIGHT {
 *           // TODO: implement calculateCost
 *       };
 *
 *       // TODO: declare abstract method
 *       public abstract double calculateCost(double weightKg);
 *   }
 *
 * RULES:
 *   - Declare calculateCost as abstract in the enum body
 *   - Each constant provides its own @Override implementation
 *   - NO if/else or switch anywhere (not in enum, not in service method)
 *   - Formulas:
 *       STANDARD:  50 + (10 * weightKg)
 *       EXPRESS:   100 + (25 * weightKg)
 *       OVERNIGHT: 200 + (50 * weightKg)
 *
 * EXAMPLE:
 *   ShippingMode.STANDARD.calculateCost(3.0)   → 50 + 30   = 80.0
 *   ShippingMode.EXPRESS.calculateCost(3.0)    → 100 + 75  = 175.0
 *   ShippingMode.OVERNIGHT.calculateCost(3.0)  → 200 + 150 = 350.0
 *
 * KEY CONCEPT:
 *   In Java, enum constants can have their own anonymous class body.
 *   This allows each constant to @Override an abstract method
 *   with completely different logic — true polymorphism without if/else.
 *
 * INTERVIEW SIGNAL:
 *   "I used an abstract method in the enum so each ShippingMode owns its
 *    own cost formula. Adding a new mode requires only a new enum constant —
 *    the service class never changes. This follows the Open/Closed Principle."
 * ============================================================
 */
public class ExcerciseThree {

    // ---- Enum to complete ----
    enum ShippingMode {
        STANDARD {
            // TODO: Override calculateCost → formula: 50 + (10 * weightKg)

            @Override
            public double calculateCost(double weightKg) {
                return 50 + (10 * weightKg);
            }
        },
        EXPRESS {
            // TODO: Override calculateCost → formula: 100 + (25 * weightKg)
            @Override
            public double calculateCost(double weightKg) {
                return 100 + (25 * weightKg);
            }
        },
        OVERNIGHT {
            // TODO: Override calculateCost → formula: 200 + (50 * weightKg)
            @Override
            public double calculateCost(double weightKg) {
                return 200 + (50 * weightKg);
            }
        };

        // TODO: Declare the abstract method that each constant must implement
        //public abstract double calculateCost(double weightKg);

        // Stub so the file compiles before you implement — remove this once you add the abstract method
        public double calculateCost(double weightKg) {
            return weightKg;
        }
    }

    // ---- Method to complete ----
    public static double getShippingCost(ShippingMode mode, double weightKg) {
        // TODO: Call mode.calculateCost(weightKg) — no if/else, no switch
        return mode.calculateCost(weightKg);
    }

    public static void main(String[] args) {
        System.out.println("=== Exercise 3: Enum with Abstract Method ===");
        System.out.println();

        // weightKg = 3.0
        System.out.println("--- Weight: 3.0 kg ---");
        System.out.println("Test 1 | STANDARD  → Expected: 80.0   (50 + 10*3)");
        try {
            System.out.println("         Actual:   " + getShippingCost(ShippingMode.STANDARD, 3.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual:   TODO not implemented yet");
        }

        System.out.println("Test 2 | EXPRESS   → Expected: 175.0  (100 + 25*3)");
        try {
            System.out.println("         Actual:   " + getShippingCost(ShippingMode.EXPRESS, 3.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual:   TODO not implemented yet");
        }

        System.out.println("Test 3 | OVERNIGHT → Expected: 350.0  (200 + 50*3)");
        try {
            System.out.println("         Actual:   " + getShippingCost(ShippingMode.OVERNIGHT, 3.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual:   TODO not implemented yet");
        }

        // weightKg = 5.0
        System.out.println();
        System.out.println("--- Weight: 5.0 kg ---");
        System.out.println("Test 4 | STANDARD  → Expected: 100.0  (50 + 10*5)");
        try {
            System.out.println("         Actual:   " + getShippingCost(ShippingMode.STANDARD, 5.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual:   TODO not implemented yet");
        }

        System.out.println("Test 5 | EXPRESS   → Expected: 225.0  (100 + 25*5)");
        try {
            System.out.println("         Actual:   " + getShippingCost(ShippingMode.EXPRESS, 5.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual:   TODO not implemented yet");
        }

        System.out.println("Test 6 | OVERNIGHT → Expected: 450.0  (200 + 50*5)");
        try {
            System.out.println("         Actual:   " + getShippingCost(ShippingMode.OVERNIGHT, 5.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual:   TODO not implemented yet");
        }

        // Bonus: loop all modes
        System.out.println();
        System.out.println("--- All modes for 2.0 kg ---");
        try {
            for (ShippingMode mode : ShippingMode.values()) {
                System.out.printf("%-10s → ₹%.1f%n", mode.name(), mode.calculateCost(2.0));
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("Loop test: TODO not implemented yet");
        }
    }
}

