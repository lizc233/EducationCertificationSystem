package com.educationcertificationsystem.ai.service.impl;

import com.educationcertificationsystem.ai.config.AiAssistantProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LocalHashEmbeddingService {

    private final AiAssistantProperties properties;

    public LocalHashEmbeddingService(AiAssistantProperties properties) {
        this.properties = properties;
    }

    public List<Double> embed(String text) {
        int dimension = Math.max(properties.getLocalEmbeddingDimension(), 32);
        double[] vector = new double[dimension];
        String normalized = StringUtils.hasText(text) ? text.toLowerCase(Locale.ROOT) : "";
        List<String> tokens = tokenize(normalized);
        if (tokens.isEmpty()) {
            tokens = List.of(normalized);
        }
        for (String token : tokens) {
            if (!StringUtils.hasText(token)) {
                continue;
            }
            int index = Math.floorMod(token.hashCode(), dimension);
            vector[index] += 1.0d;
        }
        double norm = 0.0d;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        List<Double> result = new ArrayList<>(dimension);
        for (double value : vector) {
            result.add(norm == 0.0d ? 0.0d : value / norm);
        }
        return result;
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return tokens;
        }
        String[] words = text.split("[^\\p{IsAlphabetic}\\p{IsDigit}\\u4e00-\\u9fa5]+");
        for (String word : words) {
            if (StringUtils.hasText(word)) {
                tokens.add(word);
            }
        }
        if (!tokens.isEmpty()) {
            return tokens;
        }
        for (int i = 0; i < text.length(); i++) {
            tokens.add(String.valueOf(text.charAt(i)));
        }
        return tokens;
    }
}
