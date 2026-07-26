package com.orderflow.orderservice.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderStateMachineTest {

    private OrderStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new OrderStateMachine();
    }

    @ParameterizedTest(name = "{0} -> {1} should be legal")
    @CsvSource({
            "CREATED, STOCK_RESERVED",
            "CREATED, FAILED",
            "CREATED, CANCELLED",
            "STOCK_RESERVED, PAID",
            "STOCK_RESERVED, FAILED",
            "STOCK_RESERVED, CANCELLED",
            "PAID, SHIPPED",
            "PAID, CANCELLED"
    })
    void legalTransitionsShouldReturnTrue(OrderStatus from, OrderStatus to) {
        assertTrue(stateMachine.canTransition(from, to));
    }

    @ParameterizedTest(name = "{0} -> {1} should be illegal")
    @CsvSource({
            "SHIPPED, CREATED",
            "PAID, CREATED",
            "PAID, FAILED",
            "CREATED, PAID",
            "CREATED, SHIPPED",
            "CANCELLED, CREATED",
            "FAILED, CREATED",
            "STOCK_RESERVED, CREATED",
            "STOCK_RESERVED, SHIPPED"
    })
    void illegalTransitionsShouldReturnFalse(OrderStatus from, OrderStatus to) {
        assertFalse(stateMachine.canTransition(from, to));
    }

    @Test
    void terminalStatesShouldHaveNoLegalTransitions() {
        for (OrderStatus to : OrderStatus.values()) {
            assertFalse(stateMachine.canTransition(OrderStatus.SHIPPED, to),
                    "SHIPPED should not transition to " + to);
            assertFalse(stateMachine.canTransition(OrderStatus.FAILED, to),
                    "FAILED should not transition to " + to);
            assertFalse(stateMachine.canTransition(OrderStatus.CANCELLED, to),
                    "CANCELLED should not transition to " + to);
        }
    }

    @Test
    void nullFromOrToShouldReturnFalse() {
        assertFalse(stateMachine.canTransition(null, OrderStatus.CREATED));
        assertFalse(stateMachine.canTransition(OrderStatus.CREATED, null));
        assertFalse(stateMachine.canTransition(null, null));
    }
}