package com.orderflow.paymentservice.listener;

import com.orderflow.paymentservice.domain.Payment;
import com.orderflow.paymentservice.domain.PaymentStatus;
import com.orderflow.paymentservice.event.KafkaTopics;
import com.orderflow.paymentservice.event.PaymentRefundedEvent;
import com.orderflow.paymentservice.event.RefundPaymentEvent;
import com.orderflow.paymentservice.repository.PaymentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RefundPaymentListener {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RefundPaymentListener(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = KafkaTopics.REFUND_PAYMENT, groupId = "payment-service", containerFactory = "refundPaymentFactory")
    @Transactional
    public void onRefundPayment(RefundPaymentEvent event) {
        Payment payment = paymentRepository.findByOrderId(event.orderId()).orElse(null);

        if (payment != null && payment.getStatus() != PaymentStatus.REFUNDED) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }

        kafkaTemplate.send(KafkaTopics.PAYMENT_REFUNDED, event.orderId().toString(),
                new PaymentRefundedEvent(event.orderId()));
    }
}