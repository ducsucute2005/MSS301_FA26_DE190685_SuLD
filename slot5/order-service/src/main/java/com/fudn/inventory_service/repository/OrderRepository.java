package com.fudn.inventory_service.repository;

import com.fudn.inventory_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
//repo
public interface OrderRepository extends JpaRepository<Order, Long> {
}
