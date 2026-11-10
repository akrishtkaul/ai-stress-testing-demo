package com.demo.inventoryapi.repository;

import com.demo.inventoryapi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
