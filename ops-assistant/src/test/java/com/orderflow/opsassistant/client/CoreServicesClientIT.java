package com.orderflow.opsassistant.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CoreServicesClientIT {

    @Autowired
    private CoreServicesClient client;

    @Test
    void manualPrintCheck() {
        // Replace with a real orderId/paymentId from your running system before running this test
        String orderId = "ffc6afde-9fc9-4f2b-8bec-a4accebb0c07";

        System.out.println("--- getOrderStatus ---");
        System.out.println(client.getOrderStatus(orderId));

        System.out.println("--- getOrderHistory ---");
        System.out.println(client.getOrderHistory(orderId));
    }
}