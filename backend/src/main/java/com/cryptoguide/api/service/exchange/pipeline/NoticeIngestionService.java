package com.cryptoguide.api.service.exchange.pipeline;

import com.cryptoguide.api.entity.ExchangeNotice;
import com.cryptoguide.api.repository.ExchangeNoticeRepository;
import com.cryptoguide.api.service.exchange.pipeline.model.IngestionResult;
import com.cryptoguide.api.service.exchange.pipeline.model.NoticeItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeIngestionService {

    private final ExchangeNoticeRepository exchangeNoticeRepository;

    @Transactional
    public IngestionResult ingest(String exchange, List<NoticeItem> items) {
        int inserted = 0;
        int updated = 0;

        for (NoticeItem item : items) {
            if (item.externalId() == null || item.externalId().isBlank()) {
                continue;
            }

            ExchangeNotice notice = exchangeNoticeRepository
                    .findByExchangeAndExternalId(exchange, item.externalId())
                    .orElse(null);

            if (notice == null) {
                ExchangeNotice newNotice = ExchangeNotice.builder()
                        .exchange(exchange)
                        .externalId(item.externalId())
                        .title(item.title())
                        .url(item.url())
                        .categories(item.categories())
                        .noticeType(item.noticeType())
                        .publishedAt(item.publishedAt())
                        .modifiedAt(item.modifiedAt())
                        .content(item.content())
                        .firstSeenAt(LocalDateTime.now())
                        .lastSeenAt(LocalDateTime.now())
                        .build();
                exchangeNoticeRepository.save(newNotice);
                inserted++;
                continue;
            }

            notice.setTitle(item.title());
            notice.setUrl(item.url());
            notice.setCategories(item.categories());
            notice.setNoticeType(item.noticeType());
            notice.setPublishedAt(item.publishedAt());
            notice.setModifiedAt(item.modifiedAt());
            if (item.content() != null && !item.content().isBlank()) {
                notice.setContent(item.content());
            }
            notice.setLastSeenAt(LocalDateTime.now());
            exchangeNoticeRepository.save(notice);
            updated++;
        }

        return new IngestionResult(items.size(), inserted, updated);
    }
}
