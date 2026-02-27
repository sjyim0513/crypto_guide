package com.cryptoguide.api.service.exchange.pipeline.source.crawler;

import com.cryptoguide.api.service.exchange.pipeline.NoticeTypeMapper;
import com.cryptoguide.api.service.exchange.pipeline.model.NoticeItem;
import com.cryptoguide.api.service.exchange.pipeline.source.ExchangeNoticeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpbitNoticeCrawlerSource implements ExchangeNoticeSource {

    private static final String EXCHANGE = "upbit";
    private static final List<String> PRIMARY_SELECTORS = List.of(
            "a[href*='/service_center/notice/']",
            "a[href*='/service_center/notice?']",
            "a[href*='/service_center/notice']"
    );

    private final CrawlingSourceSupport crawlingSourceSupport;
    private final NoticeTypeMapper noticeTypeMapper;

    @Value("${external.exchange.crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36}")
    private String userAgent;

    @Value("${external.exchange.crawler.upbit.base-url:https://upbit.com}")
    private String baseUrl;

    @Value("${external.exchange.crawler.upbit.notice-path:/service_center/notice}")
    private String noticePath;

    @Value("${external.exchange.crawler.upbit.limit:20}")
    private int limit;

    @Override
    public String exchange() {
        return EXCHANGE;
    }

    @Override
    public List<NoticeItem> fetchLatestNotices() {
        try {
            Document doc = crawlingSourceSupport.fetchDocument(baseUrl, noticePath, userAgent);
            Predicate<String> filter = url ->
                    url.contains("/service_center/notice")
                            && !url.contains("/service_center/faq");

            List<CrawlingSourceSupport.CrawledNotice> notices = crawlingSourceSupport.extractBySelectors(
                    doc,
                    PRIMARY_SELECTORS,
                    filter,
                    Math.max(limit, 1)
            );
            if (notices.isEmpty()) {
                notices = crawlingSourceSupport.extractFallback(doc, filter, Math.max(limit, 1));
            }
            return toNoticeItems(notices);
        } catch (Exception e) {
            log.error("Failed to crawl notices from Upbit", e);
            return List.of();
        }
    }

    private List<NoticeItem> toNoticeItems(List<CrawlingSourceSupport.CrawledNotice> notices) {
        List<NoticeItem> results = new ArrayList<>();
        for (CrawlingSourceSupport.CrawledNotice notice : notices) {
            List<String> categories = noticeTypeMapper.normalizeCategories(notice.categories());
            results.add(NoticeItem.builder()
                    .externalId(notice.url())
                    .title(notice.title())
                    .url(notice.url())
                    .categories(noticeTypeMapper.toCategoriesJson(categories))
                    .noticeType(noticeTypeMapper.mapByFirstCategory(categories))
                    .build());
        }
        return results;
    }
}
