package com.demo.inventoryapi.repository;

import com.demo.inventoryapi.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // BOTTLENECK 2: no index on name, so this causes a full table scan on every call
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT(:prefix, '%'))")
    List<Product> findByNamePrefix(@Param("prefix") String prefix, Pageable pageable);
}
