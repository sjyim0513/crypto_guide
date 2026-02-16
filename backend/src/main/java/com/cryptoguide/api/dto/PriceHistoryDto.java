package com.cryptoguide.api.dto;

import com.cryptoguide.api.entity.PriceHistory;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistoryDto {
    private LocalDateTime timestamp;
    private BigDecimal price;
    private BigDecimal marketCap;
    private BigDecimal volume;

    public static PriceHistoryDto fromEntity(PriceHistory entity) {
        return PriceHistoryDto.builder()
                .timestamp(entity.getTimestamp())
                .price(entity.getPrice())
                .marketCap(entity.getMarketCap())
                .volume(entity.getVolume())
                .build();
    }
}
