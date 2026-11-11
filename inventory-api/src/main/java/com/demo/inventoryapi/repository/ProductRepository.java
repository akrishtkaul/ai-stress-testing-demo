package com.demo.inventoryapi.repository;

import com.demo.inventoryapi.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // BOTTLENECK 2: no index on name, so this causes a full table scan on every call.
    // Case-sensitive + ORDER BY so that adding an index on name will visibly fix performance live.
    @Query("SELECT p FROM Product p WHERE p.name LIKE CONCAT(:prefix, '%') ORDER BY p.name")
    List<Product> findByNamePrefix(@Param("prefix") String prefix, Pageable pageable);
}
