package com.orderflow.opsassistant.agent;

import com.orderflow.opsassistant.remediation.RemediationAction;

import java.util.Arrays;
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
                        "paymentId", "The UUID of the payment"),
                proposeRemediationTool()
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

    /**
     * proposeRemediation is the terminal tool for the monitoring loop (7.14): the model
     * calls this exactly once, after investigating via the read-only tools above, to submit
     * its structured decision. The "action" parameter's enum constraint IS the whitelist —
     * the model literally cannot supply a value outside RemediationAction's constants.
     */
    private static Map<String, Object> proposeRemediationTool() {
        List<String> whitelistedActions = Arrays.stream(RemediationAction.values())
                .map(Enum::name)
                .toList();

        return Map.of(
                "type", "function",
                "name", "proposeRemediation",
                "description", "Submit your final remediation decision after investigating the stuck order using the other tools. Call this exactly once, as your last step.",
                "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "action", Map.of(
                                        "type", "string",
                                        "enum", whitelistedActions,
                                        "description", "The single remediation action to propose, or NONE if no safe automated action applies."
                                ),
                                "reasoning", Map.of(
                                        "type", "string",
                                        "description", "Explanation of the diagnosis and why this action is the correct fix."
                                )
                        ),
                        "required", List.of("action", "reasoning")
                )
        );
    }

    private ToolDefinitions() {
    }
}