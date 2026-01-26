package com.mrdeun.java_analyzer.client;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.fasterxml.jackson.databind.*;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OpenAIClient {
    

    @Value("openai.api_key")
    private String OPENAI_API_KEY;

    @Value("openai.model")
    private String OPENAI_MODEL;

    @Value("openai.temperature")
    private double TEMPERATURE;

    @Value("openai.http.timeout_secs")
    private long TIMEOUT;

    @Value("openai.max_tokens")
    private long MAX_TOKENS;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();


    public JsonNode call(List<Map<String, Object>> messages) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", OPENAI_MODEL);
        body.put("messages", messages);  // Changed from "input" to "messages"
        body.put("max_tokens", MAX_TOKENS);
        body.put("temperature", TEMPERATURE);

        String json = mapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_URL))
                .timeout(Duration.ofSeconds(TIMEOUT))
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
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