package com.orderflow.paymentservice.web;

import com.orderflow.paymentservice.domain.Payment;
import com.orderflow.paymentservice.exception.PaymentNotFoundException;
import com.orderflow.paymentservice.repository.PaymentRepository;
import com.orderflow.paymentservice.web.dto.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        return ResponseEntity.ok(new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCreatedAt()
        ));
    }
}