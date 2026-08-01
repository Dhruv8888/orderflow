package com.orderflow.paymentservice.listener;

import com.orderflow.paymentservice.domain.Payment;
import com.orderflow.paymentservice.domain.PaymentStatus;
import com.orderflow.paymentservice.event.KafkaTopics;
import com.orderflow.paymentservice.event.PaymentCompletedEvent;
import com.orderflow.paymentservice.event.PaymentFailedEvent;
import com.orderflow.paymentservice.event.PaymentRequestedEvent;
import com.orderflow.paymentservice.gateway.MockPaymentGateway;
import com.orderflow.paymentservice.repository.PaymentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentRequestedListener {

    private final PaymentRepository paymentRepository;
    private final MockPaymentGateway paymentGateway;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentRequestedListener(PaymentRepository paymentRepository,
                                     MockPaymentGateway paymentGateway,
                                     KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_REQUESTED, groupId = "payment-service", containerFactory = "paymentRequestedFactory")
    @Transactional
    public void onPaymentRequested(PaymentRequestedEvent event) {

        var existing = paymentRepository.findByIdempotencyKey(event.idempotencyKey());
        if (existing.isPresent()) {
            republishResult(existing.get());
            return;
        }

        MockPaymentGateway.ChargeResult result = paymentGateway.charge(event.amount());

        Payment payment = new Payment();
        payment.setOrderId(event.orderId());
        payment.setIdempotencyKey(event.idempotencyKey());
        payment.setAmount(event.amount());
        payment.setStatus(result.success() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        payment = paymentRepository.save(payment);

        publishResult(payment, result.success(), result.reasonCode());
    }

    private void republishResult(Payment payment) {
        publishResult(payment, payment.getStatus() == PaymentStatus.COMPLETED, "DUPLICATE_REQUEST_REPLAYED");
    }

    private void publishResult(Payment payment, boolean success, String reasonCode) {
        if (success) {
            kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED, payment.getOrderId().toString(),
                    new PaymentCompletedEvent(payment.getOrderId(), payment.getId()));
        } else {
            kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, payment.getOrderId().toString(),
                    new PaymentFailedEvent(payment.getOrderId(), reasonCode));
        }
    }
}