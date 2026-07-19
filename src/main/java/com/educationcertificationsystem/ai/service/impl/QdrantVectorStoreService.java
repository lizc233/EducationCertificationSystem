package com.educationcertificationsystem.ai.service.impl;

import com.educationcertificationsystem.ai.config.AiAssistantProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class QdrantVectorStoreService {

    private final AiAssistantProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public QdrantVectorStoreService(AiAssistantProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().build();
    }

    public boolean enabled() {
        return properties.getQdrant() != null
                && properties.getQdrant().isEnabled()
                && StringUtils.hasText(properties.getQdrant().getBaseUrl())
                && StringUtils.hasText(properties.getQdrant().getCollectionName());
    }

    public void ensureCollection(int vectorSize) {
        if (!enabled()) {
            return;
        }
        Map<String, Object> request = Map.of(
                "vectors", Map.of(
                        "size", vectorSize,
                        "distance", "Cosine"));
        exchange("PUT", collectionUri(), request);
    }

    public void upsertPoint(String pointId, List<Double> vector, Map<String, Object> payload) {
        if (!enabled()) {
            return;
        }
        Map<String, Object> request = Map.of(
                "points", List.of(Map.of(
                        "id", pointId,
                        "vector", vector,
                        "payload", payload)));
        exchange("PUT", collectionUri() + "/points?wait=true", request);
    }

    public void deletePoints(List<String> pointIds) {
        if (!enabled() || pointIds == null || pointIds.isEmpty()) {
            return;
        }
        Map<String, Object> request = Map.of("points", pointIds);
        exchange("POST", collectionUri() + "/points/delete?wait=true", request);
    }

    public List<SearchResult> search(List<Double> vector, int limit, Map<String, Object> filterEquals) {
        if (!enabled()) {
            return List.of();
        }
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("vector", vector);
        request.put("limit", Math.max(limit, 1));
        request.put("with_payload", true);
        if (filterEquals != null && !filterEquals.isEmpty()) {
            List<Map<String, Object>> must = new ArrayList<>();
            for (Map.Entry<String, Object> entry : filterEquals.entrySet()) {
                must.add(Map.of(
                        "key", entry.getKey(),
                        "match", Map.of("value", entry.getValue())));
            }
            request.put("filter", Map.of("must", must));
        }
        String response = exchange("POST", collectionUri() + "/points/search", request);
        return parseSearchResults(response);
    }

    private String collectionUri() {
        return trimBaseUrl() + "/collections/" + properties.getQdrant().getCollectionName();
    }

    private String trimBaseUrl() {
        String baseUrl = properties.getQdrant().getBaseUrl();
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String exchange(String method, String uri, Object body) {
        try {
            RestClient.RequestBodySpec requestSpec;
            if ("PUT".equals(method)) {
                requestSpec = restClient.put().uri(URI.create(uri));
            } else if ("POST".equals(method)) {
                requestSpec = restClient.post().uri(URI.create(uri));
            } else {
                throw new IllegalArgumentException("Unsupported method: " + method);
            }
            if (StringUtils.hasText(properties.getQdrant().getApiKey())) {
                requestSpec.header("api-key", properties.getQdrant().getApiKey());
            }
            return requestSpec
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Qdrant request failed: " + ex.getMessage(), ex);
        }
    }

    private List<SearchResult> parseSearchResults(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode resultNode = root.path("result");
            if (!resultNode.isArray()) {
                return List.of();
            }
            List<SearchResult> results = new ArrayList<>();
            for (JsonNode node : resultNode) {
                SearchResult item = new SearchResult();
                item.setPointId(node.path("id").asText());
                item.setScore(node.path("score").asDouble());
                JsonNode payloadNode = node.path("payload");
                if (payloadNode.isObject()) {
                    item.setPayload(objectMapper.convertValue(payloadNode, Map.class));
                } else {
                    item.setPayload(Map.of());
                }
                results.add(item);
            }
            return results;
        } catch (Exception ex) {
            throw new IllegalStateException("Parse Qdrant search result failed", ex);
        }
    }

    @Data
    public static class SearchResult {
        private String pointId;
        private double score;
        private Map<String, Object> payload;
    }
}
