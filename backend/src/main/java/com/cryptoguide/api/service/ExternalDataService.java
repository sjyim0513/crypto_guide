package com.cryptoguide.api.service;

import com.cryptoguide.api.entity.Cryptocurrency;
import com.cryptoguide.api.entity.PriceHistory;
import com.cryptoguide.api.entity.Theme;
import com.cryptoguide.api.repository.CryptocurrencyRepository;
import com.cryptoguide.api.repository.PriceHistoryRepository;
import com.cryptoguide.api.repository.ThemeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalDataService {

    private final CryptocurrencyRepository cryptoRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final ThemeRepository themeRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${external.coingecko.base-url:https://api.coingecko.com/api/v3}")
    private String coingeckoBaseUrl;

    @Value("${external.coingecko.api-key:}")
    private String coingeckoApiKey;

    private WebClient getCoingeckoClient() {
        WebClient.Builder builder = webClientBuilder.baseUrl(coingeckoBaseUrl);
        if (coingeckoApiKey != null && !coingeckoApiKey.isEmpty()) {
            builder.defaultHeader("x-cg-pro-api-key", coingeckoApiKey);
        }
        return builder.build();
    }

    @Scheduled(cron = "${scheduler.price-update.cron:0 */5 * * * *}")
    @Transactional
    public void updateMarketData() {
        log.info("Starting market data update...");
        
        try {
            WebClient client = getCoingeckoClient();
            
            // Fetch top 250 coins by market cap
            String response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/coins/markets")
                            .queryParam("vs_currency", "usd")
                            .queryParam("order", "market_cap_desc")
                            .queryParam("per_page", 250)
                            .queryParam("page", 1)
                            .queryParam("sparkline", false)
                            .queryParam("price_change_percentage", "1h,24h,7d,30d")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                processMarketData(response);
            }
            
            log.info("Market data update completed");
        } catch (Exception e) {
            log.error("Failed to update market data", e);
        }
    }

    private void processMarketData(String jsonResponse) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode coins = mapper.readTree(jsonResponse);

            for (JsonNode coinNode : coins) {
                String coinId = coinNode.get("id").asText();
                
                Cryptocurrency crypto = cryptoRepository.findByCoinId(coinId)
                        .orElse(Cryptocurrency.builder().coinId(coinId).build());

                // Update basic info
                crypto.setSymbol(coinNode.get("symbol").asText().toUpperCase());
                crypto.setName(coinNode.get("name").asText());
                crypto.setImageUrl(getTextOrNull(coinNode, "image"));

                // Update market data
                crypto.setCurrentPrice(getBigDecimalOrNull(coinNode, "current_price"));
                crypto.setMarketCap(getBigDecimalOrNull(coinNode, "market_cap"));
                crypto.setMarketCapRank(getIntOrNull(coinNode, "market_cap_rank"));
                crypto.setFullyDilutedValuation(getBigDecimalOrNull(coinNode, "fully_diluted_valuation"));
                crypto.setTotalVolume(getBigDecimalOrNull(coinNode, "total_volume"));
                crypto.setHigh24h(getBigDecimalOrNull(coinNode, "high_24h"));
                crypto.setLow24h(getBigDecimalOrNull(coinNode, "low_24h"));

                // Update price changes
                crypto.setPriceChange24h(getBigDecimalOrNull(coinNode, "price_change_24h"));
                crypto.setPriceChangePercentage24h(getBigDecimalOrNull(coinNode, "price_change_percentage_24h"));
                crypto.setPriceChangePercentage7d(getBigDecimalOrNull(coinNode, "price_change_percentage_7d_in_currency"));
                crypto.setPriceChangePercentage30d(getBigDecimalOrNull(coinNode, "price_change_percentage_30d_in_currency"));

                // Update supply
                crypto.setCirculatingSupply(getBigDecimalOrNull(coinNode, "circulating_supply"));
                crypto.setTotalSupply(getBigDecimalOrNull(coinNode, "total_supply"));
                crypto.setMaxSupply(getBigDecimalOrNull(coinNode, "max_supply"));

                // Update ATH/ATL
                crypto.setAth(getBigDecimalOrNull(coinNode, "ath"));
                crypto.setAthDate(getDateTimeOrNull(coinNode, "ath_date"));
                crypto.setAthChangePercentage(getBigDecimalOrNull(coinNode, "ath_change_percentage"));
                crypto.setAtl(getBigDecimalOrNull(coinNode, "atl"));
                crypto.setAtlDate(getDateTimeOrNull(coinNode, "atl_date"));
                crypto.setAtlChangePercentage(getBigDecimalOrNull(coinNode, "atl_change_percentage"));

                crypto.setLastUpdated(LocalDateTime.now());
                
                cryptoRepository.save(crypto);

                // Save price history
                savePriceHistory(crypto);
            }
        } catch (Exception e) {
            log.error("Error processing market data", e);
        }
    }

    private void savePriceHistory(Cryptocurrency crypto) {
        if (crypto.getCurrentPrice() == null) return;

        PriceHistory history = PriceHistory.builder()
                .cryptocurrency(crypto)
                .price(crypto.getCurrentPrice())
                .marketCap(crypto.getMarketCap())
                .volume(crypto.getTotalVolume())
                .interval(PriceHistory.TimeInterval.MINUTE_5)
                .timestamp(LocalDateTime.now())
                .build();

        priceHistoryRepository.save(history);
    }

    @Transactional
    public void fetchCoinDetails(String coinId) {
        log.info("Fetching details for coin: {}", coinId);
        
        try {
            WebClient client = getCoingeckoClient();
            
            String response = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/coins/{id}")
                            .queryParam("localization", false)
                            .queryParam("tickers", false)
                            .queryParam("market_data", true)
                            .queryParam("community_data", false)
                            .queryParam("developer_data", false)
                            .build(coinId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                processCoinDetails(response);
            }
        } catch (Exception e) {
            log.error("Failed to fetch coin details for: {}", coinId, e);
        }
    }

    private void processCoinDetails(String jsonResponse) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode coinNode = mapper.readTree(jsonResponse);

            String coinId = coinNode.get("id").asText();
            Cryptocurrency crypto = cryptoRepository.findByCoinId(coinId)
                    .orElseThrow(() -> new RuntimeException("Cryptocurrency not found: " + coinId));

            // Update description
            JsonNode descriptionNode = coinNode.path("description").path("en");
            if (!descriptionNode.isMissingNode()) {
                crypto.setDescription(descriptionNode.asText());
            }

            // Update links
            JsonNode linksNode = coinNode.path("links");
            if (!linksNode.isMissingNode()) {
                JsonNode homepageArray = linksNode.path("homepage");
                if (homepageArray.isArray() && homepageArray.size() > 0) {
                    crypto.setHomepage(homepageArray.get(0).asText());
                }

                JsonNode whitepaper = linksNode.path("whitepaper");
                if (!whitepaper.isMissingNode() && !whitepaper.asText().isEmpty()) {
                    crypto.setWhitepaper(whitepaper.asText());
                }

                JsonNode reposNode = linksNode.path("repos_url").path("github");
                if (reposNode.isArray() && reposNode.size() > 0) {
                    crypto.setGithub(reposNode.get(0).asText());
                }

                String twitterHandle = linksNode.path("twitter_screen_name").asText();
                if (!twitterHandle.isEmpty()) {
                    crypto.setTwitter("https://twitter.com/" + twitterHandle);
                }

                String telegramChannel = linksNode.path("telegram_channel_identifier").asText();
                if (!telegramChannel.isEmpty()) {
                    crypto.setTelegram("https://t.me/" + telegramChannel);
                }
            }

            // Map categories to themes (대·중·소 순서로 첫 3개 매핑)
            JsonNode categoriesNode = coinNode.path("categories");
            if (categoriesNode.isArray()) {
                List<Theme> ordered = new ArrayList<>();
                for (JsonNode category : categoriesNode) {
                    String categoryName = category.asText().toLowerCase();
                    mapCategoryToTheme(categoryName).ifPresent(ordered::add);
                }
                if (ordered.size() > 0) crypto.setThemeLarge(ordered.get(0));
                if (ordered.size() > 1) crypto.setThemeMedium(ordered.get(1));
                if (ordered.size() > 2) crypto.setThemeSmall(ordered.get(2));
            }

            cryptoRepository.save(crypto);
            log.info("Updated details for: {}", coinId);
        } catch (Exception e) {
            log.error("Error processing coin details", e);
        }
    }

    private Optional<Theme> mapCategoryToTheme(String category) {
        Map<String, String> categoryMapping = Map.ofEntries(
                Map.entry("stablecoins", "stablecoin"),
                Map.entry("decentralized finance (defi)", "defi"),
                Map.entry("defi", "defi"),
                Map.entry("non-fungible tokens (nft)", "nft"),
                Map.entry("nft", "nft"),
                Map.entry("metaverse", "nft"),
                Map.entry("layer 1", "layer1"),
                Map.entry("smart contract platform", "layer1"),
                Map.entry("layer 2", "layer2"),
                Map.entry("modular blockchain", "modular"),
                Map.entry("decentralized physical infrastructure networks (depin)", "depin"),
                Map.entry("depin", "depin"),
                Map.entry("artificial intelligence", "ai"),
                Map.entry("ai", "ai"),
                Map.entry("gaming", "gaming"),
                Map.entry("play-to-earn", "gaming"),
                Map.entry("meme", "meme"),
                Map.entry("meme-token", "meme"),
                Map.entry("centralized exchange (cex)", "exchange"),
                Map.entry("decentralized exchange", "defi"),
                Map.entry("privacy coins", "privacy")
        );

        String themeSlug = categoryMapping.get(category);
        if (themeSlug != null) {
            return themeRepository.findBySlug(themeSlug);
        }
        return Optional.empty();
    }

    private String getTextOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }

    private BigDecimal getBigDecimalOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        try {
            return new BigDecimal(fieldNode.asText());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getIntOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asInt() : null;
    }

    private LocalDateTime getDateTimeOrNull(JsonNode node, String field) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null || fieldNode.isNull()) return null;
        try {
            return ZonedDateTime.parse(fieldNode.asText()).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }
}
