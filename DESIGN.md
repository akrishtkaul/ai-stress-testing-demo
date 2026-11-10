# Inventory & Orders API: Stress-Testing Demo Service

## Purpose
This service is the target for a live class demo of **AI-orchestrated performance/stress testing**
(COMS 4156). It must **contain deliberate performance bottlenecks** that an AI agent will later
discover with a Gatling load test, diagnose, and fix live in class.

Scenario: it's Black Friday. Many different storefronts (web shop, mobile app, marketplace,
price-comparison site) call this backend **service** at once.

## IMPORTANT: what NOT to do
- **Do NOT fix, tune, or "improve" the planted bottlenecks** below. They are intentional.
  Do not increase the Hikari pool size, add an index on `products.name`, add caching, or make
  the search query more efficient.
- **Do NOT write the Gatling simulation** (`FlashSaleSimulation.java`). The AI writes it live
  during the demo. Only add the Gatling dependency and Maven plugin so it can be run later.
- Do not use Locust or any non-Java load tool.

## Tech stack
- Java 17+, Spring Boot 3, Maven
- Spring Web, Spring Data JPA, H2 (in-memory), Spring Boot Actuator
- Gatling (Java DSL) via `gatling-maven-plugin`, test scope

## Data model
- **Product**: `id` (Long), `name` (String), `category` (String), `description` (String), `price` (BigDecimal)
- **Order**: `id` (Long), `userId` (String), `createdAt` (Instant), `items` (list of OrderItem)
- **OrderItem**: `id` (Long), `order` (Order), `product` (Product), `quantity` (int)

**Unlimited stock**: no inventory fields, no stock checks.

## Endpoints

| Method & path | Request | Response |
|---|---|---|
| `GET /products/search?q={prefix}` | query param `q` | 200, JSON array of up to 20 products whose **name starts with** `q` (case-insensitive) |
| `GET /products/{id}` | none | 200 product JSON, or 404 if not found |
| `POST /orders` | `{"userId": "u123"}` | 201, `{"id": 1, "userId": "u123", "createdAt": "...", "items": []}` |
| `POST /orders/{id}/items` | `{"productId": 42, "quantity": 1}` | 200, updated order JSON; 404 if order or product not found; 400 if quantity < 1 |
| `GET /orders/{id}` | none | 200 order JSON with items (product id, name, price, quantity), or 404 |

Return proper status codes; invalid input returns 400 with a short JSON error message.

## Seed data
- On startup, seed **200,000 products** with varied names (e.g., combine adjectives + product
  nouns like "Red Running Shoe", "Wireless Gaming Mouse") across ~20 categories.
- Use batch inserts so startup stays reasonably fast (aim for under ~30 seconds).

## Planted bottlenecks (keep exactly as specified)
1. **Tiny connection pool** in `application.properties`:
   ```
   spring.datasource.hikari.maximum-pool-size=2
   spring.datasource.hikari.connection-timeout=2000
   ```
   Under many concurrent clients, requests queue for connections and time out.
2. **Unindexed prefix search**: `products.name` has **no index**, and search uses a prefix
   match (`name LIKE 'q%'`, case-insensitive) that scans the table. (The live fix will be
   adding an index on name, which works for prefix matches.)

## Observability
Expose Actuator metrics so the AI can confirm pool exhaustion during the demo:
```
management.endpoints.web.exposure.include=health,metrics
```
Key metric: `/actuator/metrics/hikaricp.connections.pending`.

## Project layout
```
inventory-api/
├── src/main/java/...                 # app: controllers, services, repositories, entities, seeder
├── src/main/resources/application.properties
├── src/test/java/...                 # basic unit/API tests for endpoints (NOT the Gatling simulation)
├── prompts/                          # empty for now; demo prompts go here
├── pom.xml                           # Spring Boot + Actuator + H2 + Gatling plugin
└── README.md                         # how to run the app and (later) the Gatling test
```

## How it runs
1. Start the app: `mvn spring-boot:run` (port 8080)
2. Later, run the load test: `mvn gatling:test`
3. Gatling writes an HTML report to `target/gatling/`

## Done when
- App starts with seeded data.
- All five endpoints work via curl, including 404/400 cases.
- `/actuator/metrics/hikaricp.connections.pending` responds.
- Basic tests pass with `mvn test`.
- Bottlenecks are still in place (pool size 2, no index on name).

## Demo flow (context only, do not implement)
1. Slide: how you'd stress test manually (hand-written Gatling simulation).
2. Live: ask the AI to "write a Gatling simulation of a flash sale with 200 shoppers ramping up
   over 60 seconds, where each shopper searches, views a product, creates an order, and adds items;
   run it, and tell me what's wrong."
3. AI runs `mvn gatling:test`, reads the report and Hikari metrics, and diagnoses the pool and slow search.
4. Show the "before" report (p99 spikes, failures).
5. AI applies fixes (larger pool, index on name), reruns, and the "after" report is compared side by side.
