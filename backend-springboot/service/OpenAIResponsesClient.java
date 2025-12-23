package com.example.coursereco.service;

import com.example.coursereco.util.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

@Service
public class OpenAIResponsesClient {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${openai.apiKey:}")
    private String apiKey;

    @Value("${openai.baseUrl:https://api.openai.com/v1/responses}")
    private String baseUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.maxOutputTokens:700}")
    private int maxOutputTokens;

    @Value("${openai.temperature:0.3}")
    private double temperature;

    public String generateText(String instructions, String input) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is missing (set env var on backend).");
        }

        Map<String, Object> payload = Map.of(
                "model", model,
                "instructions", instructions,
                "input", input,
                "max_output_tokens", maxOutputTokens,
                "temperature", temperature,
                "store", false,              // don’t store responses unless you want to
                "text", Map.of("format", Map.of("type", "text"))
        );

        String body = mapper.writeValueAsString(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) {
            throw new RuntimeException("OpenAI error: " + res.statusCode() + " " + res.body());
        }

        JsonNode root = mapper.readTree(res.body());
        return JsonUtils.extractResponsesText(root);
    }
}
