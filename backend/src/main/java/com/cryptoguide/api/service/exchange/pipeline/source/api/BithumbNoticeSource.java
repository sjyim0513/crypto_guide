package com.cryptoguide.api.service.exchange.pipeline.source.api;

import com.cryptoguide.api.service.exchange.pipeline.NoticeTypeMapper;
import com.cryptoguide.api.service.exchange.pipeline.model.NoticeItem;
import com.cryptoguide.api.service.exchange.pipeline.source.ExchangeNoticeSource;
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
public class BithumbNoticeSource implements ExchangeNoticeSource {

    private static final String EXCHANGE = "bithumb";

    private final WebClient.Builder webClientBuilder;
    private final ExchangeSourceSupport support;
    private final NoticeTypeMapper noticeTypeMapper;

    @Value("${external.exchange.bithumb.base-url:https://api.bithumb.com}")
    private String bithumbBaseUrl;

    @Value("${external.exchange.bithumb.notice-count:20}")
    private int noticeCount;

    @Value("${external.exchange.bithumb.notice-url-fallback:https://www.bithumb.com/customer_support/info_notice}")
    private String noticeUrlFallback;

    @Override
    public String exchange() {
        return EXCHANGE;
    }

    @Override
    public List<NoticeItem> fetchLatestNotices() {
        try {
            String response = webClientBuilder.baseUrl(bithumbBaseUrl).build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/notices")
                            .queryParam("count", Math.min(noticeCount, 20))
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response == null || response.isBlank()) {
                return List.of();
            }

            JsonNode root = support.readTree(response);
            List<JsonNode> items = support.extractArrayItems(root);

            List<NoticeItem> results = new ArrayList<>();
            for (JsonNode item : items) {
                String title = item.path("title").asText(null);
                String rawUrl = firstNonBlank(
                        item.path("pc_url").asText(null),
                        item.path("mobile_url").asText(null)
                );
                String url = firstNonBlank(rawUrl, noticeUrlFallback);
                String externalId = (rawUrl != null && !rawUrl.isBlank())
                        ? rawUrl
                        : buildFallbackExternalId(title, item.path("published_at").asText(""));

                if (externalId == null || externalId.isBlank() || title == null || title.isBlank()) {
                    continue;
                }

                List<String> rawCategories = extractCategories(item.path("categories"));
                results.add(NoticeItem.builder()
                        .externalId(externalId)
                        .title(title)
                        .url(url)
                        .categories(noticeTypeMapper.toCategoriesJson(rawCategories))
                        .noticeType(noticeTypeMapper.mapByFirstCategory(rawCategories))
                        .publishedAt(support.parseDateTime(item.path("published_at").asText(null)))
                        .modifiedAt(support.parseDateTime(item.path("modified_at").asText(null)))
                        .build());
            }
            return results;
        } catch (Exception e) {
            log.error("Failed to fetch notices from Bithumb", e);
            return List.of();
        }
    }

    private String buildFallbackExternalId(String title, String publishedAt) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return title + "::" + publishedAt;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private List<String> extractCategories(JsonNode categoriesNode) {
        if (categoriesNode == null || !categoriesNode.isArray()) {
            return List.of();
        }
        List<String> categories = new ArrayList<>();
        for (JsonNode categoryNode : categoriesNode) {
            String value = categoryNode.asText(null);
            if (value != null && !value.isBlank()) {
                categories.add(value.trim());
            }
        }
        return categories;
    }
}
