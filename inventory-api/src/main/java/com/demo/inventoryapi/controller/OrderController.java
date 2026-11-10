package com.demo.inventoryapi.controller;

import com.demo.inventoryapi.dto.AddItemRequest;
import com.demo.inventoryapi.dto.CreateOrderRequest;
import com.demo.inventoryapi.entity.Order;
import com.demo.inventoryapi.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest body) {
        if (body.userId() == null || body.userId().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId is required"));
        }
        Order order = orderService.createOrder(body.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> addItem(@PathVariable Long id, @RequestBody AddItemRequest body) {
        if (body.productId() == null || body.quantity() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "productId and quantity are required"));
        }
        Order order = orderService.addItem(id, body.productId(), body.quantity());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }
}
