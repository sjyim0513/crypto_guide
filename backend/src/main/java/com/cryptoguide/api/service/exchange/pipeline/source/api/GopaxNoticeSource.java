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
public class GopaxNoticeSource implements ExchangeNoticeSource {

    private static final String EXCHANGE = "gopax";

    private final WebClient.Builder webClientBuilder;
    private final ExchangeSourceSupport support;
    private final NoticeTypeMapper noticeTypeMapper;

    @Value("${external.exchange.gopax.base-url:https://api.gopax.co.kr}")
    private String gopaxBaseUrl;

    @Value("${external.exchange.gopax.notice-limit:20}")
    private int noticeLimit;

    @Value("${external.exchange.gopax.notice-page:0}")
    private int noticePage;

    @Value("${external.exchange.gopax.notice-format:1}")
    private int noticeFormat;

    @Value("${external.exchange.gopax.notice-url-template:https://www.gopax.co.kr/notice/detail?id=%s}")
    private String noticeUrlTemplate;

    @Override
    public String exchange() {
        return EXCHANGE;
    }

    @Override
    public List<NoticeItem> fetchLatestNotices() {
        try {
            String response = webClientBuilder.baseUrl(gopaxBaseUrl).build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/notices")
                            .queryParam("limit", Math.min(noticeLimit, 20))
                            .queryParam("page", Math.max(noticePage, 0))
                            .queryParam("format", noticeFormat)
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
                String externalId = item.path("id").asText(null);
                String title = item.path("title").asText(null);
                if (isBlank(externalId) || isBlank(title)) {
                    continue;
                }

                String url = noticeUrl(externalId);
                Short rawType = item.path("type").isInt() ? (short) item.path("type").asInt() : null;
                results.add(NoticeItem.builder()
                        .externalId(externalId)
                        .title(title)
                        .url(url)
                        .categories(mapCategoriesByType(rawType))
                        .noticeType(noticeTypeMapper.mapGopaxType(rawType))
                        .publishedAt(support.parseDateTime(item.path("createdAt").asText(null)))
                        .modifiedAt(support.parseDateTime(item.path("updatedAt").asText(null)))
                        .content(item.path("content").asText(null))
                        .build());
                        //타입이 1: 공지 2: 거래지원 3: 이벤트 4: 입출금
            }
            return results;
        } catch (Exception e) {
            log.error("Failed to fetch notices from Gopax", e);
            return List.of();
        }
    }

    private String noticeUrl(String id) {
        if (noticeUrlTemplate == null || noticeUrlTemplate.isBlank()) {
            return "https://www.gopax.co.kr/notice/detail?id=" + id;
        }
        return String.format(noticeUrlTemplate, id);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String mapCategoriesByType(Short type) {
        if (type == null) {
            return noticeTypeMapper.toCategoriesJson(List.of("공지사항"));
        }
        return switch (type) {
            case 1 -> noticeTypeMapper.toCategoriesJson(List.of("공지"));
            case 2 -> noticeTypeMapper.toCategoriesJson(List.of("거래지원"));
            case 3 -> noticeTypeMapper.toCategoriesJson(List.of("이벤트"));
            case 4 -> noticeTypeMapper.toCategoriesJson(List.of("입출금"));
            default -> noticeTypeMapper.toCategoriesJson(List.of("공지사항"));
        };
    }
}
