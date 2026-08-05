package com.orderflow.inventoryservice.event;

public final class KafkaTopics {

    public static final String ORDER_CREATED = "order-created";
    public static final String STOCK_RESERVED = "stock-reserved";
    public static final String STOCK_RESERVATION_FAILED = "stock-reservation-failed";
    public static final String PAYMENT_REQUESTED = "payment-requested";
    public static final String PAYMENT_COMPLETED = "payment-completed";
    public static final String PAYMENT_FAILED = "payment-failed";
    public static final String RELEASE_STOCK = "release-stock";
    public static final String STOCK_RELEASED = "stock-released";
    public static final String REFUND_PAYMENT = "refund-payment";
    public static final String ORDER_SHIPPED = "order-shipped";
    public static final String ORDER_CANCELLED = "order-cancelled";

    private KafkaTopics() {
    }
}