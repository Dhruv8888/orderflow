package com.orderflow.orderservice.repository;

import com.orderflow.orderservice.domain.OrderEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderEventRepository extends JpaRepository<OrderEvent, UUID> {

    List<OrderEvent> findByOrderIdOrderByCreatedAtAsc(UUID orderId);

    @Query("SELECT e.orderId FROM OrderEvent e GROUP BY e.orderId HAVING MAX(e.createdAt) < :threshold")
    List<UUID> findOrderIdsWithLatestEventBefore(@Param("threshold") Instant threshold);
}