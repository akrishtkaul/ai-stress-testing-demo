# AI Stress Testing Demo

COMS 4156 — Live demo of AI-orchestrated performance/stress testing.

## Overview

A Spring Boot REST service (Inventory & Orders API) with deliberate performance bottlenecks planted inside. During the demo, an AI agent writes a Gatling load test, runs it, diagnoses the bottlenecks from the report and metrics, and fixes them live.

## Getting Started

```bash
cd inventory-api
mvn spring-boot:run
```

Starts on port 8080. Seeds 200,000 products and pre-built demo users on first run.

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health check: http://localhost:8080/actuator/health

## Project Structure

```
inventory-api/   — the target Spring Boot service
prompts/         — prompts used during the live demo
```

See [`inventory-api/README.md`](inventory-api/README.md) for full endpoint and usage details.  
See [`inventory-api/IMPLEMENTATION.md`](inventory-api/IMPLEMENTATION.md) for technical implementation details.
