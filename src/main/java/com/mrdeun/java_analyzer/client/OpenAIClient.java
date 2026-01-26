package com.mrdeun.java_analyzer.client;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

import com.fasterxml.jackson.databind.*;

public class OpenAIClient {
    
    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    public OpenAIClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public JsonNode call(String model, List<Map<String, Object>> messages) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);  // Changed from "input" to "messages"
        body.put("max_tokens", 4000);
        body.put("temperature", 0.1);

        String json = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_URL))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException(response.body());
        }

        return mapper.readTree(response.body());
    }

    public String extractText(JsonNode response) {
        return response
                .at("/choices/0/message/content")  // Correct path for chat completions
                .asText();
    }
}