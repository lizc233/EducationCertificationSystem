package com.educationcertificationsystem.ai.service.impl;

import com.educationcertificationsystem.ai.config.AiAssistantProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class DeepSeekClientService {

    private final AiAssistantProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public DeepSeekClientService(AiAssistantProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public String generate(String systemPrompt, String userPrompt, boolean jsonOutput) {
        if (!StringUtils.hasText(properties.getDeepseek().getApiKey())) {
            throw new IllegalStateException("DeepSeek API key is not configured");
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", properties.getDeepseek().getModel());
        request.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));
        request.put("temperature", 0.4);
        if (jsonOutput) {
            request.put("response_format", Map.of("type", "json_object"));
        }
        try {
            String response = restClient.post()
                    .uri(URI.create(apiUri()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + properties.getDeepseek().getApiKey())
                    .body(request)
                    .retrieve()
                    .body(String.class);
            return parseContent(response);
        } catch (Exception ex) {
            throw new IllegalStateException("DeepSeek request failed: " + ex.getMessage(), ex);
        }
    }

    private String apiUri() {
        String baseUrl = properties.getDeepseek().getBaseUrl();
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalized + "/chat/completions";
    }

    private String parseContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (!contentNode.isTextual()) {
                throw new IllegalStateException("DeepSeek response content is empty");
            }
            return contentNode.asText();
        } catch (Exception ex) {
            throw new IllegalStateException("Parse DeepSeek response failed", ex);
        }
    }
}
