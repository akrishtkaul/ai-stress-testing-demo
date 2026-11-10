# Inventory & Orders API

Target service for the AI-orchestrated stress-testing demo (COMS 4156).

**Scenario:** Black Friday — many storefront clients (web, mobile, marketplace) hammering this backend simultaneously.

---

## Quick Start

```bash
cd inventory-api
mvn spring-boot:run
```

- Starts on **port 8080**
- Seeds 200,000 products + pre-built demo orders on first startup (~3–5 seconds)
- Swagger UI: **http://localhost:8080/swagger-ui.html**

---

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/products/search?q={prefix}` | Prefix search, up to 20 results |
| GET | `/products/{id}` | Get product by id |
| POST | `/orders` | Create order: `{"userId":"alice"}` |
| POST | `/orders/{id}/items` | Add item: `{"productId":1,"quantity":2}` |
| GET | `/orders/{id}` | Get order with all items |

---

## Pre-built Demo Data

Ready on startup — no setup needed:

| User | Order IDs |
|------|-----------|
| alice | 1, 2 |
| bob | 3, 4 |
| carol | 5, 6 |
| dave | 7, 8 |
| eve | 9, 10 |

---

## Observability

```bash
curl localhost:8080/actuator/health
curl localhost:8080/actuator/metrics/hikaricp.connections.pending
```

The `hikaricp.connections.pending` metric spikes under load — the pool is intentionally tiny (size=2).

---

## Tests

```bash
mvn test   # 11 tests, all passing
```

---

## Gatling Load Test

After the AI writes `FlashSaleSimulation.java` during the demo:

```bash
mvn gatling:test
# HTML report → target/gatling/
```

---

## Deliberate Bottlenecks (do not fix)

1. **Hikari pool size = 2** — causes connection timeouts under concurrent load
2. **No index on `products.name`** — every search does a full scan of 200k rows

These are discovered and fixed live by the AI during the demo.

---

See `IMPLEMENTATION.md` for full technical details.
