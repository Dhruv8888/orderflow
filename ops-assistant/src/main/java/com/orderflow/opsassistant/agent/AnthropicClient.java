package com.orderflow.opsassistant.agent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
public class AnthropicClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public AnthropicClient(@Value("${anthropic.api-key}") String apiKey,
                            @Value("${anthropic.model}") String model,
                            ObjectMapper objectMapper) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.anthropic.com")
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    public JsonNode sendMessage(List<Map<String, Object>> messages, List<Map<String, Object>> tools) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1024,
                "tools", tools,
                "messages", messages
        );

        String responseJson = webClient.post()
                .uri("/v1/messages")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return objectMapper.readTree(responseJson);
    }
}