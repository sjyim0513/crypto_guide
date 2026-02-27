package com.cryptoguide.api.service.exchange.pipeline.source.crawler;

import com.cryptoguide.api.service.exchange.pipeline.NoticeTypeMapper;
import com.cryptoguide.api.service.exchange.pipeline.model.NoticeItem;
import com.cryptoguide.api.service.exchange.pipeline.source.ExchangeNoticeSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class KorbitNoticeCrawlerSource implements ExchangeNoticeSource {

    private static final String EXCHANGE = "korbit";
    private static final Pattern NOTICE_ID_PATTERN = Pattern.compile("noticeId=([0-9]+)");
    private static final DateTimeFormatter DATE_ONLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<String> PRIMARY_SELECTORS = List.of(
            "a[href*='/notice/']",
            "a[href*='/notice?']",
            "a[href*='/notice']"
    );

    private final CrawlingSourceSupport crawlingSourceSupport;
    private final NoticeTypeMapper noticeTypeMapper;

    @Value("${external.exchange.crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36}")
    private String userAgent;

    @Value("${external.exchange.crawler.korbit.base-url:https://www.korbit.co.kr}")
    private String baseUrl;

    @Value("${external.exchange.crawler.korbit.notice-path:/notice/}")
    private String noticePath;

    @Value("${external.exchange.crawler.korbit.limit:20}")
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
                    url.contains("/notice")
                            && !url.contains("/faq");

            List<CrawlingSourceSupport.CrawledNotice> notices = crawlingSourceSupport.extractBySelectors(
                    doc,
                    PRIMARY_SELECTORS,
                    filter,
                    Math.max(limit, 1)
            );
            if (notices.isEmpty()) {
                notices = crawlingSourceSupport.extractFallback(doc, filter, Math.max(limit, 1));
            }
            List<NoticeItem> fromAnchors = toNoticeItems(notices);
            if (fromAnchors.size() >= Math.max(limit, 1)) {
                return fromAnchors;
            }

            // Korbit notice rows are often rendered without explicit <a> tags.
            List<NoticeItem> fromRows = parseListRows(doc, Math.max(limit, 1));
            if (fromRows.isEmpty()) {
                return fromAnchors;
            }

            Map<String, NoticeItem> merged = new LinkedHashMap<>();
            for (NoticeItem item : fromAnchors) {
                merged.put(item.externalId(), item);
            }
            for (NoticeItem item : fromRows) {
                merged.putIfAbsent(item.externalId(), item);
            }
            return new ArrayList<>(merged.values());
        } catch (Exception e) {
            log.error("Failed to crawl notices from Korbit", e);
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

    private List<NoticeItem> parseListRows(Document doc, int maxLimit) {
        Elements rows = doc.select("li:has(.title-wrapper .title h4), li:has(.title h4)");
        if (rows.isEmpty()) {
            return List.of();
        }

        List<NoticeItem> results = new ArrayList<>();
        for (Element row : rows) {
            if (results.size() >= maxLimit) {
                break;
            }

            String title = textOfFirst(row, ".title-wrapper .title h4, .title h4, h4");
            if (isBlank(title)) {
                continue;
            }

            String dateText = textOfFirst(row, "p.date, .date");
            LocalDateTime publishedAt = parseDate(dateText);
            String detailUrl = extractDetailUrl(row);
            String externalId = detailUrl != null
                    ? detailUrl
                    : title + "::" + (dateText == null ? "" : dateText);
            List<String> categories = extractRowCategories(row);

            results.add(NoticeItem.builder()
                    .externalId(externalId)
                    .title(title)
                    .url(detailUrl != null ? detailUrl : fallbackListUrl())
                    .categories(noticeTypeMapper.toCategoriesJson(categories))
                    .noticeType(noticeTypeMapper.mapByFirstCategory(categories))
                    .publishedAt(publishedAt)
                    .build());
        }
        return results;
    }

    private List<String> extractRowCategories(Element row) {
        List<String> categories = new ArrayList<>();
        for (Element node : row.select(".category, [class*='category'], span")) {
            String text = normalizeText(node.text());
            if (isBlank(text)) {
                continue;
            }
            if (text.length() > 20 || text.matches("\\d{4}-\\d{2}-\\d{2}")) {
                continue;
            }
            categories.add(text);
        }
        return noticeTypeMapper.normalizeCategories(categories);
    }

    private String extractDetailUrl(Element row) {
        Element anchor = row.selectFirst("a[href*='/notice']");
        if (anchor != null) {
            String href = anchor.absUrl("href");
            if (!isBlank(href)) {
                return href;
            }
        }

        String onclick = row.attr("onclick");
        String fromOnclick = extractNoticeIdUrl(onclick);
        if (fromOnclick != null) {
            return fromOnclick;
        }

        String rowHtml = row.outerHtml();
        String fromHtml = extractNoticeIdUrl(rowHtml);
        if (fromHtml != null) {
            return fromHtml;
        }

        String title = textOfFirst(row, ".title-wrapper .title h4, .title h4, h4");
        if (isBlank(title)) {
            return null;
        }
        return fallbackListUrl() + "?title=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
    }

    private String extractNoticeIdUrl(String text) {
        if (isBlank(text)) {
            return null;
        }
        Matcher matcher = NOTICE_ID_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return fallbackListUrl() + "?noticeId=" + matcher.group(1);
    }

    private LocalDateTime parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim(), DATE_ONLY_FORMAT).atStartOfDay();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String textOfFirst(Element root, String selector) {
        Element found = root.selectFirst(selector);
        if (found == null) {
            return null;
        }
        return found.text().replaceAll("\\s+", " ").trim();
    }

    private String fallbackListUrl() {
        if (noticePath == null || noticePath.isBlank()) {
            return baseUrl;
        }
        if (baseUrl.endsWith("/") && noticePath.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + noticePath;
        }
        if (!baseUrl.endsWith("/") && !noticePath.startsWith("/")) {
            return baseUrl + "/" + noticePath;
        }
        return baseUrl + noticePath;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\s+", " ").trim();
    }
}
