package com.orderflow.opsassistant.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GeminiClient(@Value("${gemini.api-key}") String apiKey,
                         @Value("${gemini.model}") String model,
                         ObjectMapper objectMapper) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader("content-type", "application/json")
                .build();
    }

    public JsonNode createInteraction(Object input, List<Map<String, Object>> tools, String previousInteractionId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", input);
        requestBody.put("tools", tools);
        if (previousInteractionId != null) {
            requestBody.put("previous_interaction_id", previousInteractionId);
        }

        String responseJson = webClient.post()
                .uri("/v1beta/interactions")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), resp ->
                        resp.bodyToMono(String.class).map(body ->
                                new RuntimeException("Gemini API error: " + body)))
                .bodyToMono(String.class)
                .block();

        return objectMapper.readTree(responseJson);
    }
}