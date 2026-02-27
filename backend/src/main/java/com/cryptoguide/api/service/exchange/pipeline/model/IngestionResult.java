package com.cryptoguide.api.service.exchange.pipeline.model;

public record IngestionResult(
        int fetchedCount,
        int insertedCount,
        int updatedCount
) {
}
