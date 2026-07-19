package com.educationcertificationsystem.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.ai")
public class AiAssistantProperties {

    private boolean enabled = true;

    private int requestTimeoutSeconds = 60;

    private int topK = 5;

    private int reportMaxContextChunks = 6;

    private int improveMaxContextChunks = 6;

    private int localEmbeddingDimension = 128;

    private DeepSeek deepseek = new DeepSeek();

    private Qdrant qdrant = new Qdrant();

    @Data
    public static class DeepSeek {
        private String baseUrl;
        private String apiKey;
        private String model;
    }

    @Data
    public static class Qdrant {
        private boolean enabled = true;
        private String baseUrl;
        private String apiKey;
        private String collectionName;
    }
}
