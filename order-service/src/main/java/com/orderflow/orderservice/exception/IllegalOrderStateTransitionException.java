package com.orderflow.orderservice.exception;

import com.orderflow.orderservice.domain.OrderStatus;

import java.util.UUID;

public class IllegalOrderStateTransitionException extends RuntimeException {

    public IllegalOrderStateTransitionException(UUID orderId, OrderStatus from, OrderStatus to) {
        super("Illegal transition for order " + orderId + ": " + from + " -> " + to);
    }
}