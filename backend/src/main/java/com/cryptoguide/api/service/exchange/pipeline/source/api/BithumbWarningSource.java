package com.cryptoguide.api.service.exchange.pipeline.source.api;

import com.cryptoguide.api.service.exchange.pipeline.model.WarningItem;
import com.cryptoguide.api.service.exchange.pipeline.source.ExchangeWarningSource;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BithumbWarningSource implements ExchangeWarningSource {

    private static final String EXCHANGE = "bithumb";

    private final WebClient.Builder webClientBuilder;
    private final ExchangeSourceSupport support;

    @Value("${external.exchange.bithumb.base-url:https://api.bithumb.com}")
    private String bithumbBaseUrl;

    @Override
    public String exchange() {
        return EXCHANGE;
    }

    @Override
    public List<WarningItem> fetchLatestWarnings() {
        try {
            String response = webClientBuilder.baseUrl(bithumbBaseUrl).build()
                    .get()
                    .uri("/v1/market/virtual_asset_warning")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                return List.of();
            }

            JsonNode root = support.readTree(response);
            List<JsonNode> items = support.extractArrayItems(root);

            List<WarningItem> results = new ArrayList<>();
            for (JsonNode item : items) {
                String market = item.path("market").asText(null);
                String warningType = item.path("warning_type").asText(null);
                String warningStep = item.path("warning_step").asText(null);

                if (isBlank(market) || isBlank(warningType) || isBlank(warningStep)) {
                    continue;
                }

                results.add(WarningItem.builder()
                        .market(market)
                        .warningType(warningType)
                        .warningStep(warningStep)
                        .endAt(support.parseDateTime(item.path("end_date").asText(null)))
                        .build());
            }
            return results;
        } catch (Exception e) {
            log.error("Failed to fetch warnings from Bithumb", e);
            return List.of();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
