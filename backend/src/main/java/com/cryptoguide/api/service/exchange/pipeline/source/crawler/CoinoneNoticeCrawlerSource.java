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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoinoneNoticeCrawlerSource implements ExchangeNoticeSource {

    private static final String EXCHANGE = "coinone";
    private static final List<String> PRIMARY_SELECTORS = List.of(
            "a[href*='/info/notice/']",
            "a[href*='/info/notice?']",
            "a[href*='/info/notice']"
    );
    private static final Pattern NOTICE_URL_PATTERN = Pattern.compile("(/info/notice(?:/[^\"'\\s)]+|\\?[^\"'\\s)]+)?)");
    private static final String ROW_SELECTORS =
            "li:has(h4), li:has(h3), li:has(.title), " +
            "div:has(h4), div:has(h3), div:has(.title), " +
            "article:has(h4), article:has(h3), article:has(.title)";

    private final CrawlingSourceSupport crawlingSourceSupport;
    private final NoticeTypeMapper noticeTypeMapper;

    @Value("${external.exchange.crawler.user-agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36}")
    private String userAgent;

    @Value("${external.exchange.crawler.coinone.base-url:https://coinone.co.kr}")
    private String baseUrl;

    @Value("${external.exchange.crawler.coinone.notice-path:/info/notice}")
    private String noticePath;

    @Value("${external.exchange.crawler.coinone.limit:20}")
    private int limit;

    @Override
    public String exchange() {
        return EXCHANGE;
    }

    @Override
    public List<NoticeItem> fetchLatestNotices() {
        try {
            Document doc = crawlingSourceSupport.fetchDocument(baseUrl, noticePath, userAgent);
            Predicate<String> filter = this::isNoticeDetailUrl;

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
            log.error("Failed to crawl notices from Coinone", e);
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
        if (doc == null) {
            return List.of();
        }

        Elements rows = doc.select(ROW_SELECTORS);
        if (rows.isEmpty()) {
            return List.of();
        }

        List<NoticeItem> results = new ArrayList<>();
        for (Element row : rows) {
            if (results.size() >= maxLimit) {
                break;
            }

            String title = textOfFirst(row, "h4, h3, .title, .subject, [class*='title']");
            if (isBlank(title)) {
                continue;
            }

            String detailUrl = extractDetailUrl(row);
            String externalId = detailUrl != null
                    ? detailUrl
                    : "coinone::notice::" + title;
            List<String> categories = extractRowCategories(row);

            results.add(NoticeItem.builder()
                    .externalId(externalId)
                    .title(title)
                    .url(detailUrl != null ? detailUrl : fallbackListUrl())
                    .categories(noticeTypeMapper.toCategoriesJson(categories))
                    .noticeType(noticeTypeMapper.mapByFirstCategory(categories))
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
        Element anchor = row.selectFirst("a[href*='/info/notice']");
        if (anchor != null) {
            String href = anchor.absUrl("href");
            if (!isBlank(href)) {
                return href;
            }
        }

        String[] attrs = new String[]{
                row.attr("data-href"),
                row.attr("data-url"),
                row.attr("data-link"),
                row.attr("onclick"),
                row.outerHtml()
        };
        for (String candidate : attrs) {
            String found = extractNoticeUrl(candidate);
            if (found != null) {
                return found;
            }
        }

        String title = textOfFirst(row, "h4, h3, .title, .subject, [class*='title']");
        if (isBlank(title)) {
            return null;
        }
        return fallbackListUrl() + "?title=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
    }

    private String extractNoticeUrl(String text) {
        if (isBlank(text)) {
            return null;
        }

        if (text.startsWith("http://") || text.startsWith("https://")) {
            return text;
        }

        Matcher matcher = NOTICE_URL_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }

        String path = matcher.group(1);
        if (isBlank(path)) {
            return null;
        }
        return absoluteUrl(path);
    }

    private String textOfFirst(Element root, String selector) {
        Element found = root.selectFirst(selector);
        if (found == null) {
            return null;
        }
        return found.text().replaceAll("\\s+", " ").trim();
    }

    private String absoluteUrl(String path) {
        if (path == null || path.isBlank()) {
            return fallbackListUrl();
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (baseUrl.endsWith("/") && path.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + path;
        }
        if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
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

    private boolean isNoticeDetailUrl(String url) {
        if (isBlank(url)) {
            return false;
        }
        String lower = url.toLowerCase();
        if (!lower.contains("/info/notice") || lower.contains("/info/faq")) {
            return false;
        }
        // Exclude the notice list root and keep only detail-like URLs.
        return lower.contains("/info/notice?")
                || lower.matches(".*/info/notice/[^/?#].*");
    }
}
