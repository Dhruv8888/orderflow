package com.orderflow.opsassistant.agent;

import com.orderflow.opsassistant.client.CoreServicesClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpsAssistantAgent {

    private static final int MAX_ITERATIONS = 6;

    private final AnthropicClient anthropicClient;
    private final CoreServicesClient coreServicesClient;

    public OpsAssistantAgent(AnthropicClient anthropicClient, CoreServicesClient coreServicesClient) {
        this.anthropicClient = anthropicClient;
        this.coreServicesClient = coreServicesClient;
    }

    public String ask(String question) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", question));

        List<Map<String, Object>> tools = ToolDefinitions.all();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            JsonNode response = anthropicClient.sendMessage(messages, tools);
            JsonNode content = response.get("content");
            String stopReason = response.get("stop_reason").asText();

            if (!"tool_use".equals(stopReason)) {
                return extractText(content);
            }

            messages.add(toAssistantMessage(content));

            List<Map<String, Object>> toolResults = new ArrayList<>();
            for (JsonNode block : content) {
                if ("tool_use".equals(block.get("type").asText())) {
                    String toolName = block.get("name").asText();
                    String toolUseId = block.get("id").asText();
                    JsonNode input = block.get("input");

                    String result = executeTool(toolName, input);

                    toolResults.add(Map.of(
                            "type", "tool_result",
                            "tool_use_id", toolUseId,
                            "content", result
                    ));
                }
            }

            messages.add(Map.of("role", "user", "content", toolResults));
        }

        return "The agent could not reach a final answer within the allowed number of tool-call iterations.";
    }

    private String executeTool(String toolName, JsonNode input) {
        return switch (toolName) {
            case "getOrderStatus" -> coreServicesClient.getOrderStatus(input.get("orderId").asText());
            case "getOrderHistory" -> coreServicesClient.getOrderHistory(input.get("orderId").asText());
            case "getPaymentDetails" -> coreServicesClient.getPaymentDetails(input.get("paymentId").asText());
            default -> "Unknown tool: " + toolName;
        };
    }

    private Map<String, Object> toAssistantMessage(JsonNode content) {
        List<Object> blocks = new ArrayList<>();
        for (JsonNode block : content) {
            Map<String, Object> blockMap = new HashMap<>();
            block.properties().forEach(entry -> blockMap.put(entry.getKey(), entry.getValue()));
            blocks.add(blockMap);
        }
        return Map.of("role", "assistant", "content", blocks);
    }

    private String extractText(JsonNode content) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode block : content) {
            if ("text".equals(block.get("type").asText())) {
                sb.append(block.get("text").asText());
            }
        }
        return sb.toString();
    }
}