package com.orderflow.orderservice.repository;

import com.orderflow.orderservice.domain.Order;
import com.orderflow.orderservice.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByIdInAndStatusNotIn(List<UUID> ids, List<OrderStatus> excludedStatuses);
    List<Order> findAllByOrderByCreatedAtDesc();
}