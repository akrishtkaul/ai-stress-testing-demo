# Inventory & Orders API — Implementation Summary

## Purpose

This is the **target service** for a live class demo of AI-orchestrated performance/stress testing (COMS 4156 — Black Friday scenario). The app deliberately contains two planted bottlenecks that an AI agent will discover, diagnose, and fix live during the demo. Do not fix them ahead of time.

---

## Tech Stack

- Java 17, Spring Boot 3.2.5, Maven
- Spring Web, Spring Data JPA, H2 (in-memory database)
- Spring Boot Actuator (metrics exposed)
- SpringDoc OpenAPI / Swagger UI (for manual endpoint testing)
- Gatling 3.10.5 + gatling-maven-plugin 4.9.6 (dependency and plugin wired in, no simulation written yet)

---

## Project Layout

```
inventory-api/
├── pom.xml
├── README.md
├── IMPLEMENTATION.md                          ← this file
├── prompts/                                   ← empty, demo prompts go here
└── src/
    ├── main/
    │   ├── java/com/demo/inventoryapi/
    │   │   ├── InventoryApiApplication.java
    │   │   ├── controller/
    │   │   │   ├── ProductController.java
    │   │   │   └── OrderController.java
    │   │   ├── dto/
    │   │   │   ├── CreateOrderRequest.java    ← record: userId
    │   │   │   └── AddItemRequest.java        ← record: productId, quantity
    │   │   ├── service/
    │   │   │   ├── ProductService.java
    │   │   │   └── OrderService.java
    │   │   ├── repository/
    │   │   │   ├── ProductRepository.java
    │   │   │   └── OrderRepository.java
    │   │   ├── entity/
    │   │   │   ├── Product.java
    │   │   │   ├── Order.java
    │   │   │   └── OrderItem.java
    │   │   └── seeder/
    │   │       └── DataSeeder.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/demo/inventoryapi/
            ├── ProductControllerTest.java     ← 4 tests
            └── OrderControllerTest.java       ← 7 tests
```

---

## Data Model

**Product** (`products` table)
- `id` Long PK (auto-generated)
- `name` String — **no index** (intentional bottleneck)
- `category` String
- `description` String
- `price` BigDecimal

**Order** (`orders` table)
- `id` Long PK (auto-generated)
- `userId` String — just a label, not a foreign key
- `createdAt` Instant
- `items` List\<OrderItem\> — `@OneToMany cascade=ALL fetch=EAGER`

**OrderItem** (`order_items` table)
- `id` Long PK
- `order` @ManyToOne (`@JsonBackReference` — excluded from serialization to avoid cycles)
- `product` @ManyToOne EAGER
- `quantity` int

---

## Endpoints

| Method | Path | Request | Response |
|--------|------|---------|----------|
| GET | `/products/search?q={prefix}` | query param `q` | 200 JSON array ≤ 20 matches |
| GET | `/products/{id}` | — | 200 product JSON / 404 |
| POST | `/orders` | `{"userId":"alice"}` | 201 order JSON |
| POST | `/orders/{id}/items` | `{"productId":1,"quantity":2}` | 200 updated order / 404 / 400 |
| GET | `/orders/{id}` | — | 200 order with items / 404 |

Error body format: `{"error": "<message>"}` with appropriate status code.

---

## Deliberate Bottlenecks (do not fix)

### Bottleneck 1 — Tiny Hikari connection pool
**Location:** `src/main/resources/application.properties`
```properties
spring.datasource.hikari.maximum-pool-size=2
spring.datasource.hikari.connection-timeout=2000
```
Under many concurrent clients, requests queue waiting for a connection and time out.

### Bottleneck 2 — Unindexed prefix search
**Location:** `ProductRepository.java` + `Product.java` (no `@Index` on `name`)
```java
@Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT(:prefix, '%'))")
List<Product> findByNamePrefix(@Param("prefix") String prefix, Pageable pageable);
```
With 200,000 rows and no index, every search causes a full table scan.

---

## Seed Data (auto-loaded on startup)

### Products
200,000 products generated from combinations of:
- 20 adjectives × 20 nouns × 20 categories
- Names like "Red Shoe", "Wireless Mouse 5", "Gaming Laptop 12"
- Prices randomized $5–$1000
- Seeded via native SQL in batches of 500 — typically takes ~3 seconds

### Pre-built Demo Users & Orders

| userId | Order IDs | Items in each order |
|--------|-----------|---------------------|
| alice  | 1, 2      | products 1–3 / products 4–5 |
| bob    | 3, 4      | products 6–7 / products 8–10 |
| carol  | 5, 6      | products 11–13 / product 14 |
| dave   | 7, 8      | products 15–16 / products 17–19 |
| eve    | 9, 10     | product 20 / products 21–24 |

These are ready on startup — no need to create orders/users manually during the demo.

---

## Observability

Actuator endpoints exposed:
```
GET /actuator/health
GET /actuator/metrics/hikaricp.connections.pending
```

The `hikaricp.connections.pending` metric is the key signal during the demo — it spikes when the pool is exhausted under load.

---

## Swagger UI

Available at **http://localhost:8080/swagger-ui.html** after startup.
All 5 endpoints are listed with "Try it out" support and correct request schemas.

---

## Running the App

```bash
cd inventory-api
mvn spring-boot:run
# App starts on port 8080
# Seeds 200k products + demo users on first startup (~3–5 seconds total)
```

## Running Tests

```bash
mvn test
# 11 tests: 4 product + 7 order — all passing
```

## Running the Gatling Load Test (after AI writes the simulation)

```bash
mvn gatling:test
# HTML report → target/gatling/
```

---

## What Is NOT Done Yet (intentional)

- `FlashSaleSimulation.java` — the AI writes this live during the demo
- Prompts in `prompts/` — added during demo
- Bottleneck fixes — applied live by the AI after diagnosing the report

---

## Demo Flow (context)

1. Show slide: how you'd stress test manually (hand-write a Gatling simulation)
2. Live: ask the AI — *"Write a Gatling simulation of a flash sale with 200 shoppers ramping up over 60 seconds, where each shopper searches for a product, views it, creates an order, and adds an item. Run it and tell me what's wrong."*
3. AI runs `mvn gatling:test`, reads the HTML report and `/actuator/metrics/hikaricp.connections.pending`, diagnoses both bottlenecks
4. Show "before" report (p99 spikes, timeouts)
5. AI applies fixes (pool size → 50, add `@Index` on `Product.name`), reruns
6. Show "after" report side by side
