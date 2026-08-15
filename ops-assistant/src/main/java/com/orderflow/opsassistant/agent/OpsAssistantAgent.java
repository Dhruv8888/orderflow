package com.orderflow.opsassistant.agent;

import com.orderflow.opsassistant.client.CoreServicesClient;
import com.orderflow.opsassistant.remediation.RemediationAction;
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

    /**
     * Result of the structured remediation loop: the whitelisted action the model chose
     * (or NONE), plus its reasoning. Never contains anything outside RemediationAction's
     * constants — that's enforced by the tool schema's enum, not by this code.
     */
    public record RemediationProposal(RemediationAction action, String reasoning) {
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

    /**
     * Same loop shape as ask(), but terminates on a proposeRemediation function_call
     * instead of on model_output text. Critically: that call's arguments are captured
     * and returned directly — they are never executed and never sent back to Gemini
     * as a function_result. The model's job ends at proposing; turning the proposal
     * into a PendingAction row is StuckOrderMonitor's job, in plain Java.
     */
    public RemediationProposal diagnoseAndProposeRemediation(String question) {
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
                return new RemediationProposal(RemediationAction.NONE,
                        "Agent returned a text response instead of a structured proposal: " + extractText(response));
            }

            for (JsonNode call : functionCalls) {
                if ("proposeRemediation".equals(call.get("name").asText())) {
                    JsonNode arguments = call.get("arguments");
                    RemediationAction action = RemediationAction.valueOf(arguments.get("action").asText());
                    String reasoning = arguments.get("reasoning").asText();
                    return new RemediationProposal(action, reasoning);
                }
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

        return new RemediationProposal(RemediationAction.NONE,
                "Agent could not reach a remediation decision within the allowed number of tool-call iterations.");
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