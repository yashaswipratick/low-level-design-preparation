package com.lld.practice.daily_practice.day_04_enum_behavior.submission;

/*
 * ============================================================
 * DAY 4 - EXERCISE 2: Enum with Behavior Method (Pattern 2)
 * ============================================================
 * DIFFICULTY: Intermediate
 * TOPIC: Enum with a concrete method that uses its own field
 *
 * CONTEXT:
 *   Pattern 1 taught you enums that carry data via a field.
 *   Pattern 2 goes one step further: the enum also has a METHOD
 *   that USES that field to compute something.
 *
 *   This removes computation logic from the service class entirely.
 *   The service just calls: timeframe.getDiscountedPrice(price)
 *   instead of having its own discount calculation code.
 *
 * YOUR TASK:
 *   Build a MembershipTier enum where each tier has:
 *   - A discount percentage (field)
 *   - A method to apply the discount to a given price
 *
 * ENUM TO COMPLETE:
 *   enum MembershipTier {
 *       BASIC,    // 0% discount
 *       SILVER,   // 10% discount
 *       GOLD,     // 20% discount
 *       PLATINUM; // 35% discount
 *
 *       // TODO: field, constructor, getter, behavior method
 *   }
 *
 * RULES:
 *   - Add private final int discountPercent field
 *   - Add constructor MembershipTier(int discountPercent)
 *   - Add getter getDiscountPercent()
 *   - Add method: public double applyDiscount(double price)
 *       → returns price after applying the discount
 *       → formula: price * (100 - discountPercent) / 100.0
 *   - NO if/else or switch anywhere outside the enum
 *
 * EXAMPLE:
 *   MembershipTier.BASIC.applyDiscount(100.0)    → 100.0  (0% off)
 *   MembershipTier.SILVER.applyDiscount(100.0)   → 90.0   (10% off)
 *   MembershipTier.GOLD.applyDiscount(200.0)     → 160.0  (20% off)
 *   MembershipTier.PLATINUM.applyDiscount(200.0) → 130.0  (35% off)
 *
 * KEY CONCEPT:
 *   The method lives INSIDE the enum — it can access 'this.discountPercent'
 *   like any normal class method. The caller never needs to know the %.
 *
 * INTERVIEW SIGNAL:
 *   "I put the discount logic inside the enum so the service stays clean.
 *    Adding a new tier only requires a new enum constant — no service change."
 * ============================================================
 */
public class ExcerciseTwo {

    // ---- Enum to complete ----
    enum MembershipTier {
        BASIC(0),    // 0% discount
        SILVER(10),   // 10% discount
        GOLD(20),     // 20% discount
        PLATINUM(30); // 35% discount

        // TODO 1: Add private final int discountPercent
        private final int discountPercent;
        // TODO 2: Add constructor: MembershipTier(int discountPercent)
        MembershipTier(int discountPercent) {
            this.discountPercent = discountPercent;
        }
        // TODO 3: Add getter: public int getDiscountPercent()
        public int getDiscountPercent() { return this.discountPercent; }
        // TODO 4: Add behavior method: public double applyDiscount(double price)
        //         Formula: price * (100 - discountPercent) / 100.0
        public double applyDiscount(double price) {
            return price * (100 - discountPercent) / 100.0;
        }
        // TODO 5: Update constants: BASIC(0), SILVER(10), GOLD(20), PLATINUM(35)

    }

    // ---- Method to complete ----
    public static double getFinalPrice(MembershipTier tier, double originalPrice) {
        // TODO: Call tier.applyDiscount(originalPrice) — no if/else
        return tier.applyDiscount(originalPrice);
    }

    public static void main(String[] args) {
        System.out.println("=== Exercise 2: Enum with Behavior Method ===");
        System.out.println();

        System.out.println("Test 1 | BASIC,    price=100.0  → Expected: 100.0");
        try {
            System.out.println("         Actual: " + getFinalPrice(MembershipTier.BASIC, 100.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual: TODO not implemented yet");
        }

        System.out.println("Test 2 | SILVER,   price=100.0  → Expected: 90.0");
        try {
            System.out.println("         Actual: " + getFinalPrice(MembershipTier.SILVER, 100.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual: TODO not implemented yet");
        }

        System.out.println("Test 3 | GOLD,     price=200.0  → Expected: 160.0");
        try {
            System.out.println("         Actual: " + getFinalPrice(MembershipTier.GOLD, 200.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual: TODO not implemented yet");
        }

        System.out.println("Test 4 | PLATINUM, price=200.0  → Expected: 140.0");
        try {
            System.out.println("         Actual: " + getFinalPrice(MembershipTier.PLATINUM, 200.0));
        } catch (UnsupportedOperationException e) {
            System.out.println("         Actual: TODO not implemented yet");
        }

        // Bonus: loop over all tiers for price=1000
        System.out.println();
        System.out.println("--- All tiers applied to price=1000.0 ---");
        try {
            for (MembershipTier tier : MembershipTier.values()) {
                System.out.printf("%-10s → discount=%d%%, final=%.1f%n",
                        tier.name(), tier.getDiscountPercent(), tier.applyDiscount(1000.0));
            }
        } catch (UnsupportedOperationException e) {
            System.out.println("Loop test: TODO not implemented yet");
        }
    }
}

