package com.cryptoguide.api.service.exchange.pipeline.source.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExchangeSourceSupport {

    private static final DateTimeFormatter BITHUMB_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;

    public JsonNode readTree(String response) throws Exception {
        return objectMapper.readTree(response);
    }

    public List<JsonNode> extractArrayItems(JsonNode root) {
        if (root == null || root.isNull()) {
            return List.of();
        }
        if (root.isArray()) {
            return toList(root);
        }
        JsonNode data = root.path("data");
        if (data.isArray()) {
            return toList(data);
        }
        return List.of();
    }

    public String toJsonString(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return null;
        }
    }

    public LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, BITHUMB_DATE_TIME);
        } catch (Exception ignored) {
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<JsonNode> toList(JsonNode arrayNode) {
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode node : arrayNode) {
            result.add(node);
        }
        return result;
    }
}
