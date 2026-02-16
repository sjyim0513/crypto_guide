package com.cryptoguide.api.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history", indexes = {
    @Index(name = "idx_price_history_crypto_timestamp", columnList = "cryptocurrency_id, timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cryptocurrency_id", nullable = false)
    private Cryptocurrency cryptocurrency;

    @Column(nullable = false, precision = 30, scale = 10)
    private BigDecimal price;

    @Column(precision = 30, scale = 2)
    private BigDecimal marketCap;

    @Column(precision = 30, scale = 2)
    private BigDecimal volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeInterval interval;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TimeInterval {
        MINUTE_1,
        MINUTE_5,
        MINUTE_15,
        HOUR_1,
        HOUR_4,
        DAY_1,
        WEEK_1
    }
}
