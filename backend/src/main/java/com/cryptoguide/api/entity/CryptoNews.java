package com.cryptoguide.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "crypto_news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptoNews {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 10000)
    private String content;  // Original content in markdown

    @Column(length = 2000)
    private String summary;  // AI-generated summary

    @Column(length = 500)
    private String sourceUrl;

    @Column(nullable = false)
    private String source;  // e.g., "CoinDesk", "The Block"

    private String author;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NewsStatus status = NewsStatus.PENDING;

    private LocalDateTime publishedAt;

    // Related cryptocurrencies
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "news_cryptocurrencies",
        joinColumns = @JoinColumn(name = "news_id"),
        inverseJoinColumns = @JoinColumn(name = "cryptocurrency_id")
    )
    @Builder.Default
    private Set<Cryptocurrency> relatedCryptos = new HashSet<>();

    // Related themes
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "news_themes",
        joinColumns = @JoinColumn(name = "news_id"),
        inverseJoinColumns = @JoinColumn(name = "theme_id")
    )
    @Builder.Default
    private Set<Theme> relatedThemes = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum NewsStatus {
        PENDING,    // 요약 대기
        PROCESSING, // 요약 진행 중
        COMPLETED,  // 요약 완료
        FAILED      // 요약 실패
    }
}
