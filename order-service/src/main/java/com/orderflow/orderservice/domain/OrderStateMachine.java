package com.orderflow.orderservice.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(
                OrderStatus.CREATED,
                EnumSet.of(OrderStatus.STOCK_RESERVED, OrderStatus.FAILED, OrderStatus.CANCELLED)
        );
        ALLOWED_TRANSITIONS.put(
                OrderStatus.STOCK_RESERVED,
                EnumSet.of(OrderStatus.PAID, OrderStatus.FAILED, OrderStatus.CANCELLED)
        );
        ALLOWED_TRANSITIONS.put(
                OrderStatus.PAID,
                EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED)
        );
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.FAILED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        return ALLOWED_TRANSITIONS.get(from).contains(to);
    }
}