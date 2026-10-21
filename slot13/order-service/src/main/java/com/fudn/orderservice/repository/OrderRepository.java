package com.fudn.orderservice.repository;

import com.fudn.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
//repo
public interface OrderRepository extends JpaRepository<Order, Long> {
}
