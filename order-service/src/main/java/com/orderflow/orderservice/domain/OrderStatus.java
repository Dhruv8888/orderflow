package com.orderflow.orderservice.domain;

public enum OrderStatus {
    CREATED,
    STOCK_RESERVED,
    PAID,
    SHIPPED,
    CANCELLED,
    FAILED
}