package com.orderflow.paymentservice.event;

public final class KafkaTopics {

    public static final String PAYMENT_REQUESTED = "payment-requested";
    public static final String PAYMENT_COMPLETED = "payment-completed";
    public static final String PAYMENT_FAILED = "payment-failed";
    public static final String REFUND_PAYMENT = "refund-payment";

    private KafkaTopics() {
    }
}