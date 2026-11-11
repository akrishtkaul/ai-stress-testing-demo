The Inventory & Orders API in this repo is already running on http://localhost:8080. Don't start or restart it.

1. Write a Gatling simulation in the Java DSL (not Scala) at src/test/java/com/demo/inventoryapi/FlashSaleSimulation.java. Ramp up to 200 concurrent shoppers over 60 seconds. Each shopper:
   - searches for products: GET /products/search?q=Red
   - takes a product id from the results and views it: GET /products/{id}
   - creates an order: POST /orders with {"userId": "<random>"} and saves the returned order id
   - adds the product to it: POST /orders/{id}/items with {"productId": <id>, "quantity": 1}
2. Run it with mvn gatling:test.
3. Summarize the results: requests per second, p50/p95/p99 response times, and error rate per endpoint.
4. Check /actuator/metrics/hikaricp.connections.timeout and /actuator/metrics/hikaricp.connections.max, then look at the code and config, and explain exactly what's causing the problems and why.

Don't fix anything yet. Explain the causes first.
