package com.orderflow.orderservice.web.dto;

import com.orderflow.orderservice.domain.OrderStatus;

import java.util.UUID;

public class CreateOrderResponse {

    private final UUID orderId;
    private final OrderStatus status;

    public CreateOrderResponse(UUID orderId, OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }
}