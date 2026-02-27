package com.cryptoguide.api.service.exchange.pipeline.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record WarningItem(
        String market,
        String warningType,
        String warningStep,
        LocalDateTime endAt
) {
}
