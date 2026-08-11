package com.orderflow.opsassistant.agent;

import com.orderflow.opsassistant.client.CoreServicesClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OpsAssistantAgent {

    private static final int MAX_ITERATIONS = 6;

    private final GeminiClient geminiClient;
    private final CoreServicesClient coreServicesClient;

    public OpsAssistantAgent(GeminiClient geminiClient, CoreServicesClient coreServicesClient) {
        this.geminiClient = geminiClient;
        this.coreServicesClient = coreServicesClient;
    }

    public String ask(String question) {
        List<Map<String, Object>> tools = ToolDefinitions.all();

        JsonNode response = geminiClient.createInteraction(question, tools, null);
        String interactionId = response.get("id").asText();

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            List<JsonNode> functionCalls = new ArrayList<>();
            for (JsonNode step : response.get("steps")) {
                if ("function_call".equals(step.get("type").asText())) {
                    functionCalls.add(step);
                }
            }

            if (functionCalls.isEmpty()) {
                return extractText(response);
            }

            List<Map<String, Object>> functionResults = new ArrayList<>();
            for (JsonNode call : functionCalls) {
                String toolName = call.get("name").asText();
                String callId = call.get("id").asText();
                JsonNode arguments = call.get("arguments");

                String result = executeTool(toolName, arguments);

                functionResults.add(Map.of(
                        "type", "function_result",
                        "name", toolName,
                        "call_id", callId,
                        "result", List.of(Map.of("type", "text", "text", result))
                ));
            }

            response = geminiClient.createInteraction(functionResults, tools, interactionId);
            interactionId = response.get("id").asText();
        }

        return "The agent could not reach a final answer within the allowed number of tool-call iterations.";
    }

    private String executeTool(String toolName, JsonNode arguments) {
        return switch (toolName) {
            case "getOrderStatus" -> coreServicesClient.getOrderStatus(arguments.get("orderId").asText());
            case "getOrderHistory" -> coreServicesClient.getOrderHistory(arguments.get("orderId").asText());
            case "getPaymentDetails" -> coreServicesClient.getPaymentDetails(arguments.get("paymentId").asText());
            default -> "Unknown tool: " + toolName;
        };
    }

    private String extractText(JsonNode response) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode step : response.get("steps")) {
            String type = step.get("type").asText();
            if ("model_output".equals(type) && step.has("content")) {
                for (JsonNode part : step.get("content")) {
                    if (part.has("text")) {
                        sb.append(part.get("text").asText());
                    }
                }
            }
        }
        return sb.toString();
    }
}