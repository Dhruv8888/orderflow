package com.orderflow.paymentservice.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MockPaymentGateway {

    private final double failureRate;

    public MockPaymentGateway(@Value("${payment.failure-rate:0.2}") double failureRate) {
        this.failureRate = failureRate;
    }

    public ChargeResult charge(BigDecimal amount) {
        boolean shouldFail = ThreadLocalRandom.current().nextDouble() < failureRate;

        if (shouldFail) {
            return new ChargeResult(false, "GATEWAY_DECLINED");
        }
        return new ChargeResult(true, null);
    }

    public record ChargeResult(boolean success, String reasonCode) {
    }
}