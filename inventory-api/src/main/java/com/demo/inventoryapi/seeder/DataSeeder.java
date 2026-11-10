package com.demo.inventoryapi.seeder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final String[] ADJECTIVES = {
        "Red", "Blue", "Wireless", "Smart", "Heavy", "Lightweight", "Pro", "Mini",
        "Ultra", "Compact", "Gaming", "Portable", "Solar", "Foldable", "Noise-Cancelling",
        "Ergonomic", "Premium", "Budget", "Turbo", "Slim"
    };

    private static final String[] NOUNS = {
        "Shoe", "Mouse", "Keyboard", "Monitor", "Laptop", "Headphone", "Camera",
        "Charger", "Watch", "Jacket", "Backpack", "Speaker", "Controller", "Lamp",
        "Desk", "Chair", "Tablet", "Drone", "Printer", "Router"
    };

    private static final String[] CATEGORIES = {
        "Electronics", "Footwear", "Accessories", "Computers", "Audio", "Cameras",
        "Wearables", "Clothing", "Bags", "Home Office", "Gaming", "Lighting",
        "Mobile", "Networking", "Smart Home", "Sports", "Outdoors", "Tools",
        "Toys", "Appliances"
    };

    private static final int TOTAL = 200_000;
    private static final int BATCH = 500;

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(String... args) {
        long existing = (long) em.createQuery("SELECT COUNT(p) FROM Product p").getSingleResult();
        if (existing > 0) return;

        Random rng = new Random(42);
        int adjLen = ADJECTIVES.length;
        int nounLen = NOUNS.length;
        int catLen = CATEGORIES.length;

        System.out.println("Seeding " + TOTAL + " products...");
        long start = System.currentTimeMillis();

        for (int i = 0; i < TOTAL; i++) {
            String adj = ADJECTIVES[i % adjLen];
            String noun = NOUNS[(i / adjLen) % nounLen];
            int suffix = i / (adjLen * nounLen);
            String name = suffix == 0 ? adj + " " + noun : adj + " " + noun + " " + suffix;
            String category = CATEGORIES[i % catLen];
            String description = category + " product: " + name;
            BigDecimal price = BigDecimal.valueOf(5 + rng.nextInt(995))
                    .add(BigDecimal.valueOf(rng.nextInt(100)).movePointLeft(2))
                    .setScale(2, RoundingMode.HALF_UP);

            em.createNativeQuery(
                "INSERT INTO products (name, category, description, price) VALUES (?, ?, ?, ?)"
            )
            .setParameter(1, name)
            .setParameter(2, category)
            .setParameter(3, description)
            .setParameter(4, price)
            .executeUpdate();

            if ((i + 1) % BATCH == 0) {
                em.flush();
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("Seeded %,d products in %.1f seconds%n", TOTAL, elapsed / 1000.0);

        seedDemoOrders();
    }

    private void seedDemoOrders() {
        // Pre-built users and orders ready for the demo
        // Users: alice, bob, carol, dave, eve
        // Each gets 2 orders with a few items each
        String[][] users = {
            {"alice",  "1,2,3",    "4,5"},
            {"bob",    "6,7",      "8,9,10"},
            {"carol",  "11,12,13", "14"},
            {"dave",   "15,16",    "17,18,19"},
            {"eve",    "20",       "21,22,23,24"},
        };

        for (String[] row : users) {
            String userId = row[0];
            for (int o = 1; o <= 2; o++) {
                em.createNativeQuery("INSERT INTO orders (user_id, created_at) VALUES (?, CURRENT_TIMESTAMP)")
                  .setParameter(1, userId)
                  .executeUpdate();

                long orderId = ((Number) em.createNativeQuery(
                    "SELECT MAX(id) FROM orders WHERE user_id = ?")
                    .setParameter(1, userId)
                    .getSingleResult()).longValue();

                String[] productIds = row[o].split(",");
                for (String pid : productIds) {
                    int qty = new Random().nextInt(3) + 1;
                    em.createNativeQuery(
                        "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)")
                      .setParameter(1, orderId)
                      .setParameter(2, Long.parseLong(pid.trim()))
                      .setParameter(3, qty)
                      .executeUpdate();
                }
            }
        }

        System.out.println("Seeded demo users: alice, bob, carol, dave, eve (2 orders each)");
    }
}
