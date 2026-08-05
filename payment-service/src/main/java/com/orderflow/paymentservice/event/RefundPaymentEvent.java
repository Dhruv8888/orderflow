package com.orderflow.paymentservice.event;

import java.util.UUID;

public record RefundPaymentEvent(UUID orderId) {
}