package com.cryptoguide.api.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketOverviewDto {
    private BigDecimal totalMarketCap;
    private BigDecimal totalVolume24h;
    private BigDecimal marketCapChangePercentage24h;
    private BigDecimal btcDominance;
    private BigDecimal ethDominance;
    private Integer upCount;
    private Integer downCount;
    private Integer totalCoins;
}
