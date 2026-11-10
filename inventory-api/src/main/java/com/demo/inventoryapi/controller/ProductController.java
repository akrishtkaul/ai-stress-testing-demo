package com.demo.inventoryapi.controller;

import com.demo.inventoryapi.entity.Product;
import com.demo.inventoryapi.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("q") String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "query param 'q' is required"));
        }
        List<Product> results = productService.searchByPrefix(q);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.findById(id);
    }
}
