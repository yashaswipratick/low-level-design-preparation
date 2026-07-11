package com.lld.practice.daily_practice.day_03_comparator.submission;

import java.util.*;

/*
 * ============================================================
 * DAY 3 - EXERCISE 4: Three-Key Sort (Score DESC + Category ASC + Name ASC)
 * ============================================================
 * DIFFICULTY: Intermediate-Hard
 * TOPIC: Comparator — chaining 3 keys
 *
 * CONTEXT:
 *   A product catalog needs to be sorted by:
 *     1. Score DESC         (higher is better)
 *     2. Category ASC       (A → Z) when scores are equal
 *     3. ProductName ASC    (A → Z) when score AND category are equal
 *
 *   This teaches you to chain more than 2 comparators — a common interview pattern.
 *
 * METHOD:
 *   public static List<Product> sortProducts(List<Product> products)
 *
 * INPUT:
 *   List<Product> — each has String name, String category, int score
 *
 * OUTPUT:
 *   Sorted by: score DESC → category ASC → name ASC
 *
 * RULES:
 *   - Chain all 3 keys in ONE comparator expression
 *   - No if/else, no manual loops for ordering
 *
 * EXAMPLE:
 *   Input:
 *     ("Pen",   "Office", 80)
 *     ("Desk",  "Office", 80)
 *     ("Chair", "Home",   80)
 *     ("Lamp",  "Home",   95)
 *
 *   Step 1 — sort by score DESC:
 *     Lamp=95 first, then Pen/Desk/Chair all at 80
 *   Step 2 — among score=80, sort by category ASC:
 *     "Home" < "Office" → Chair first, then Pen and Desk
 *   Step 3 — among same score+category, sort by name ASC:
 *     Desk < Pen → Desk before Pen
 *
 *   Expected: [ Lamp=95, Chair=80/Home, Desk=80/Office, Pen=80/Office ]
 *
 * KEY API TO USE:
 *   Comparator.comparingInt(Product::getScore).reversed()
 *             .thenComparing(Product::getCategory)
 *             .thenComparing(Product::getName)
 * ============================================================
 */
public class ExcerciseFour {

    // ---- Model (do not change) ----
    static class Product {
        private final String name;
        private final String category;
        private final int score;

        public Product(String name, String category, int score) {
            this.name = name;
            this.category = category;
            this.score = score;
        }

        public String getName()     { return name; }
        public String getCategory() { return category; }
        public int getScore()       { return score; }

        @Override
        public String toString() { return name + "=" + score + "/" + category; }
    }

    // ---- Your method ----
    public static List<Product> sortProducts(List<Product> products) {
        // TODO: Sort by score DESC, then category ASC, then name ASC — chain all 3 in one comparator
        List<Product> list = new ArrayList<>(products);
        list.sort(Comparator.comparingInt(Product::getScore).reversed()
                .thenComparing(Product::getCategory)
                .thenComparing(Product::getName));
        return list;
    }

    public static void main(String[] args) {
        List<Product> products = new ArrayList<>(Arrays.asList(
                new Product("Pen",   "Office", 80),
                new Product("Desk",  "Office", 80),
                new Product("Chair", "Home",   80),
                new Product("Lamp",  "Home",   95)
        ));

        System.out.println("=== Exercise 4: Three-Key Sort ===");
        System.out.println("Input:    Pen=80/Office, Desk=80/Office, Chair=80/Home, Lamp=95/Home");
        System.out.println("Expected: Lamp=95/Home, Chair=80/Home, Desk=80/Office, Pen=80/Office");
        System.out.println();

        try {
            List<Product> result = sortProducts(products);
            System.out.println("Actual:   " + result);

            boolean pass = result.get(0).getName().equals("Lamp")
                    && result.get(1).getName().equals("Chair")
                    && result.get(2).getName().equals("Desk")
                    && result.get(3).getName().equals("Pen");
            System.out.println("Result: " + (pass ? "PASS ✓" : "FAIL ✗"));
        } catch (UnsupportedOperationException e) {
            System.out.println("Actual:   TODO not implemented yet");
        }
    }
}

