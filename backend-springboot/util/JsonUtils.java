package com.example.coursereco.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtils {
    private JsonUtils() {}

    // Extract concatenated output_text from Responses API result
    public static String extractResponsesText(JsonNode root) {
        if (root == null) return "";
        JsonNode output = root.get("output");
        if (output == null || !output.isArray()) return "";

        StringBuilder sb = new StringBuilder();
        for (JsonNode item : output) {
            JsonNode content = item.get("content");
            if (content == null || !content.isArray()) continue;
            for (JsonNode c : content) {
                if ("output_text".equals(c.path("type").asText())) {
                    sb.append(c.path("text").asText()).append("\n");
                }
            }
        }
        return sb.toString().trim();
    }
}
