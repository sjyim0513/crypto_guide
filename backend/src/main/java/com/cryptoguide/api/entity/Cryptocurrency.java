package com.cryptoguide.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cryptocurrencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cryptocurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String coinId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String imageUrl;

    // Current Market Data
    @Column(precision = 30, scale = 10)
    private BigDecimal currentPrice;

    @Column(precision = 30, scale = 2)
    private BigDecimal marketCap;

    private Integer marketCapRank;

    @Column(precision = 30, scale = 2)
    private BigDecimal fullyDilutedValuation;

    @Column(precision = 30, scale = 2)
    private BigDecimal totalVolume;

    @Column(precision = 30, scale = 10)
    private BigDecimal high24h;

    @Column(precision = 30, scale = 10)
    private BigDecimal low24h;

    // Price Changes
    @Column(precision = 10, scale = 4)
    private BigDecimal priceChange24h;

    @Column(precision = 10, scale = 4)
    private BigDecimal priceChangePercentage24h;

    @Column(precision = 10, scale = 4)
    private BigDecimal priceChangePercentage7d;

    @Column(precision = 10, scale = 4)
    private BigDecimal priceChangePercentage30d;

    // Supply
    @Column(precision = 30, scale = 2)
    private BigDecimal circulatingSupply;

    @Column(precision = 30, scale = 2)
    private BigDecimal totalSupply;

    @Column(precision = 30, scale = 2)
    private BigDecimal maxSupply;

    // ATH/ATL
    @Column(precision = 30, scale = 10)
    private BigDecimal ath;

    private LocalDateTime athDate;

    @Column(precision = 10, scale = 4)
    private BigDecimal athChangePercentage;

    @Column(precision = 30, scale = 10)
    private BigDecimal atl;

    private LocalDateTime atlDate;

    @Column(precision = 10, scale = 4)
    private BigDecimal atlChangePercentage;

    // Project Info
    @Column(length = 5000)
    private String description;

    @Column(length = 500)
    private String homepage;

    @Column(length = 500)
    private String whitepaper;

    @Column(length = 500)
    private String github;

    @Column(length = 500)
    private String twitter;

    @Column(length = 500)
    private String telegram;

    /** 테마 대분류 (1개) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_large_id")
    private Theme themeLarge;

    /** 테마 중분류 (1개) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_medium_id")
    private Theme themeMedium;

    /** 테마 소분류 (1개) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_small_id")
    private Theme themeSmall;

    // Timestamps
    private LocalDateTime lastUpdated;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
