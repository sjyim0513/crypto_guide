package com.cryptoguide.api.service.exchange.pipeline.model;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record NoticeItem(
        String externalId,
        String title,
        String url,
        String categories,
        Short noticeType,
        LocalDateTime publishedAt,
        LocalDateTime modifiedAt,
        String content
) {
}
