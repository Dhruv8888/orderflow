package com.orderflow.opsassistant.agent;

import java.util.List;
import java.util.Map;

public final class ToolDefinitions {

    public static List<Map<String, Object>> all() {
        return List.of(
                tool("getOrderStatus",
                        "Get the current status and details of an order by its order ID.",
                        "orderId", "The UUID of the order"),
                tool("getOrderHistory",
                        "Get the full, ordered event history for an order, showing every state transition and when it happened.",
                        "orderId", "The UUID of the order"),
                tool("getPaymentDetails",
                        "Get details of a specific payment by its payment ID, including its status (COMPLETED, FAILED, REFUNDED).",
                        "paymentId", "The UUID of the payment")
        );
    }

    private static Map<String, Object> tool(String name, String description, String paramName, String paramDescription) {
        return Map.of(
                "type", "function",
                "name", name,
                "description", description,
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                paramName, Map.of(
                                        "type", "string",
                                        "description", paramDescription
                                )
                        ),
                        "required", List.of(paramName)
                )
        );
    }

    private ToolDefinitions() {
    }
}