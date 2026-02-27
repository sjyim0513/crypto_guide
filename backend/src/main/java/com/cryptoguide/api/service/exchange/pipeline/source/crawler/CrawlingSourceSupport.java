package com.cryptoguide.api.service.exchange.pipeline.source.crawler;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrawlingSourceSupport {

    @Value("${external.exchange.crawler.playwright.headless:true}")
    private boolean headless;

    @Value("${external.exchange.crawler.playwright.navigate-timeout-ms:20000}")
    private double navigateTimeoutMs;

    @Value("${external.exchange.crawler.playwright.network-idle-timeout-ms:8000}")
    private double networkIdleTimeoutMs;

    @Value("${external.exchange.crawler.playwright.extra-wait-ms:1200}")
    private double extraWaitMs;

    public Document fetchDocument(String baseUrl, String path, String userAgent) {
        String targetUrl = buildTargetUrl(baseUrl, path);
        String renderedHtml = fetchRenderedHtml(targetUrl, userAgent);
        if (renderedHtml == null || renderedHtml.isBlank()) {
            return null;
        }
        return Jsoup.parse(renderedHtml, baseUrl);
    }

    public List<CrawledNotice> extractBySelectors(
            Document doc,
            List<String> selectors,
            Predicate<String> urlPredicate,
            int limit
    ) {
        if (doc == null) {
            return List.of();
        }

        Map<String, CrawledNotice> dedup = new LinkedHashMap<>();
        for (String selector : selectors) {
            Elements anchors = doc.select(selector);
            putNotices(dedup, anchors, urlPredicate, limit);
            if (dedup.size() >= limit) {
                break;
            }
        }
        return new ArrayList<>(dedup.values());
    }

    public List<CrawledNotice> extractFallback(
            Document doc,
            Predicate<String> urlPredicate,
            int limit
    ) {
        if (doc == null) {
            return List.of();
        }
        Map<String, CrawledNotice> dedup = new LinkedHashMap<>();
        putNotices(dedup, doc.select("a[href]"), urlPredicate, limit);
        return new ArrayList<>(dedup.values());
    }

    private void putNotices(
            Map<String, CrawledNotice> dedup,
            Elements anchors,
            Predicate<String> urlPredicate,
            int limit
    ) {
        for (Element a : anchors) {
            if (dedup.size() >= limit) {
                return;
            }

            String url = a.absUrl("href");
            if (url == null || url.isBlank()) {
                continue;
            }
            if (!urlPredicate.test(url)) {
                continue;
            }

            String title = normalizeText(a.text());
            if (title == null || title.isBlank()) {
                continue;
            }

            dedup.putIfAbsent(url, new CrawledNotice(title, url, extractCategories(a)));
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    private List<String> extractCategories(Element anchor) {
        LinkedHashSet<String> categories = new LinkedHashSet<>();

        // Upbit-like structure: a > span > span(category)
        for (Element span : anchor.select("span > span, span")) {
            String text = normalizeText(span.text());
            if (text == null || text.isBlank()) {
                continue;
            }
            if (text.length() > 14) {
                continue;
            }
            if (text.matches("\\d{4}-\\d{2}-\\d{2}")) {
                continue;
            }
            categories.add(text);
        }

        // Korbit-like structure: sibling/ancestor has .category
        Element row = anchor.closest("tr, li, article, div");
        if (row != null) {
            for (Element categoryNode : row.select(".category, [class*='category']")) {
                String text = normalizeText(categoryNode.text());
                if (text == null || text.isBlank()) {
                    continue;
                }
                if (text.length() <= 20) {
                    categories.add(text);
                }
            }
        }
        return new ArrayList<>(categories);
    }

    private String fetchRenderedHtml(String targetUrl, String userAgent) {
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions().setHeadless(headless)
             );
             BrowserContext context = browser.newContext(
                     new Browser.NewContextOptions().setUserAgent(userAgent)
             );
             Page page = context.newPage()) {

            page.navigate(
                    targetUrl,
                    new Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                            .setTimeout(navigateTimeoutMs)
            );
            try {
                page.waitForLoadState(
                        LoadState.NETWORKIDLE,
                        new Page.WaitForLoadStateOptions().setTimeout(networkIdleTimeoutMs)
                );
            } catch (TimeoutError ignored) {
                log.debug("Network idle wait timed out, continue with current DOM. url={}", targetUrl);
            }
            page.waitForTimeout(extraWaitMs);
            return page.content();
        } catch (Exception e) {
            log.error("Failed to render page with Playwright. url={}", targetUrl, e);
            return null;
        }
    }

    private String buildTargetUrl(String baseUrl, String path) {
        if (path == null || path.isBlank()) {
            return baseUrl;
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

    public record CrawledNotice(String title, String url, List<String> categories) {}
}
