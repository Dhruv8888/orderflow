package com.orderflow.orderservice.service;

import com.orderflow.orderservice.domain.Order;
import com.orderflow.orderservice.domain.OrderEvent;
import com.orderflow.orderservice.domain.OrderStateMachine;
import com.orderflow.orderservice.domain.OrderStatus;
import com.orderflow.orderservice.exception.IllegalOrderStateTransitionException;
import com.orderflow.orderservice.exception.OrderNotFoundException;
import com.orderflow.orderservice.repository.OrderEventRepository;
import com.orderflow.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class OrderEventService {

    private final OrderRepository orderRepository;
    private final OrderEventRepository orderEventRepository;
    private final OrderStateMachine stateMachine;
    private final ObjectMapper objectMapper;

    public OrderEventService(OrderRepository orderRepository,
                              OrderEventRepository orderEventRepository,
                              OrderStateMachine stateMachine,
                              ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderEventRepository = orderEventRepository;
        this.stateMachine = stateMachine;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Order recordEvent(UUID orderId, String eventType, OrderStatus newStatus, Object payload) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        boolean isFirstEvent = orderEventRepository.findByOrderIdOrderByCreatedAtAsc(orderId).isEmpty();

        if (!isFirstEvent && !stateMachine.canTransition(order.getStatus(), newStatus)) {
            throw new IllegalOrderStateTransitionException(orderId, order.getStatus(), newStatus);
        }

        OrderEvent event = new OrderEvent();
        event.setOrderId(orderId);
        event.setEventType(eventType);
        event.setPayloadJson(toJson(payload));
        orderEventRepository.save(event);

        order.setStatus(newStatus);
        orderRepository.save(order);

        return order;
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }

    @Transactional
    public Order recordInformationalEvent(UUID orderId, String eventType, Object payload) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    
        OrderEvent event = new OrderEvent();
        event.setOrderId(orderId);
        event.setEventType(eventType);
        event.setPayloadJson(toJson(payload));
        orderEventRepository.save(event);
    
        return order;
    }

    public List<Order> getStuckOrders(int thresholdMinutes) {
        Instant threshold = Instant.now().minus(thresholdMinutes, ChronoUnit.MINUTES);
        List<UUID> staleOrderIds = orderEventRepository.findOrderIdsWithLatestEventBefore(threshold);
    
        if (staleOrderIds.isEmpty()) {
            return List.of();
        }
    
        List<OrderStatus> terminalStatuses = List.of(OrderStatus.SHIPPED, OrderStatus.FAILED, OrderStatus.CANCELLED);
        return orderRepository.findByIdInAndStatusNotIn(staleOrderIds, terminalStatuses);
    }
}