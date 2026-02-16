package com.cryptoguide.api.dto;

import com.cryptoguide.api.entity.Cryptocurrency;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptocurrencyDto {
    private Long id;
    private String coinId;
    private String symbol;
    private String name;
    private String imageUrl;

    private BigDecimal currentPrice;
    private BigDecimal marketCap;
    private Integer marketCapRank;
    private BigDecimal fullyDilutedValuation;
    private BigDecimal totalVolume;
    private BigDecimal high24h;
    private BigDecimal low24h;

    private BigDecimal priceChange24h;
    private BigDecimal priceChangePercentage24h;
    private BigDecimal priceChangePercentage7d;
    private BigDecimal priceChangePercentage30d;

    private BigDecimal circulatingSupply;
    private BigDecimal totalSupply;
    private BigDecimal maxSupply;

    private BigDecimal ath;
    private LocalDateTime athDate;
    private BigDecimal athChangePercentage;
    private BigDecimal atl;
    private LocalDateTime atlDate;
    private BigDecimal atlChangePercentage;

    private String description;
    private String homepage;
    private String whitepaper;
    private String github;
    private String twitter;
    private String telegram;

    /** 테마 대·중·소 (각 1개) */
    private ThemeDto themeLarge;
    private ThemeDto themeMedium;
    private ThemeDto themeSmall;

    private LocalDateTime lastUpdated;

    public static CryptocurrencyDto fromEntity(Cryptocurrency entity) {
        return CryptocurrencyDto.builder()
                .id(entity.getId())
                .coinId(entity.getCoinId())
                .symbol(entity.getSymbol())
                .name(entity.getName())
                .imageUrl(entity.getImageUrl())
                .currentPrice(entity.getCurrentPrice())
                .marketCap(entity.getMarketCap())
                .marketCapRank(entity.getMarketCapRank())
                .fullyDilutedValuation(entity.getFullyDilutedValuation())
                .totalVolume(entity.getTotalVolume())
                .high24h(entity.getHigh24h())
                .low24h(entity.getLow24h())
                .priceChange24h(entity.getPriceChange24h())
                .priceChangePercentage24h(entity.getPriceChangePercentage24h())
                .priceChangePercentage7d(entity.getPriceChangePercentage7d())
                .priceChangePercentage30d(entity.getPriceChangePercentage30d())
                .circulatingSupply(entity.getCirculatingSupply())
                .totalSupply(entity.getTotalSupply())
                .maxSupply(entity.getMaxSupply())
                .ath(entity.getAth())
                .athDate(entity.getAthDate())
                .athChangePercentage(entity.getAthChangePercentage())
                .atl(entity.getAtl())
                .atlDate(entity.getAtlDate())
                .atlChangePercentage(entity.getAtlChangePercentage())
                .description(entity.getDescription())
                .homepage(entity.getHomepage())
                .whitepaper(entity.getWhitepaper())
                .github(entity.getGithub())
                .twitter(entity.getTwitter())
                .telegram(entity.getTelegram())
                .themeLarge(ThemeDto.fromEntity(entity.getThemeLarge()))
                .themeMedium(ThemeDto.fromEntity(entity.getThemeMedium()))
                .themeSmall(ThemeDto.fromEntity(entity.getThemeSmall()))
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}
