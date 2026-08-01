package com.orderflow.paymentservice.listener;

import com.orderflow.paymentservice.event.PaymentRequestedEvent;
import com.orderflow.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PaymentRequestedListenerIT {

    @Autowired
    private PaymentRequestedListener listener;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void duplicatePaymentRequestedEventShouldNotCreateDuplicatePayment() {
        UUID orderId = UUID.randomUUID();
        String idempotencyKey = "test-key-" + orderId;
        PaymentRequestedEvent event = new PaymentRequestedEvent(orderId, new BigDecimal("42.00"), idempotencyKey);

        listener.onPaymentRequested(event);
        listener.onPaymentRequested(event);

        long count = paymentRepository.findByIdempotencyKey(idempotencyKey)
                .stream()
                .count();

        assertEquals(1, count, "Processing the same PaymentRequestedEvent twice must result in exactly one Payment row");
    }
}