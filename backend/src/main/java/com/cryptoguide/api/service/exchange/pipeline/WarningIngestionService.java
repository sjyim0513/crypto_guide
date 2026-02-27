package com.cryptoguide.api.service.exchange.pipeline;

import com.cryptoguide.api.entity.ExchangeWarning;
import com.cryptoguide.api.repository.ExchangeWarningRepository;
import com.cryptoguide.api.service.exchange.pipeline.model.IngestionResult;
import com.cryptoguide.api.service.exchange.pipeline.model.WarningItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarningIngestionService {

    private final ExchangeWarningRepository exchangeWarningRepository;

    @Transactional
    public IngestionResult ingest(String exchange, List<WarningItem> items) {
        int inserted = 0;
        int updated = 0;

        for (WarningItem item : items) {
            if (isInvalid(item)) {
                continue;
            }

            ExchangeWarning warning = exchangeWarningRepository
                    .findByExchangeAndMarketAndWarningTypeAndWarningStepAndEndAt(
                            exchange,
                            item.market(),
                            item.warningType(),
                            item.warningStep(),
                            item.endAt()
                    )
                    .orElse(null);

            if (warning == null) {
                ExchangeWarning newWarning = ExchangeWarning.builder()
                        .exchange(exchange)
                        .market(item.market())
                        .warningType(item.warningType())
                        .warningStep(item.warningStep())
                        .endAt(item.endAt())
                        .firstSeenAt(LocalDateTime.now())
                        .lastSeenAt(LocalDateTime.now())
                        .build();
                exchangeWarningRepository.save(newWarning);
                inserted++;
                continue;
            }

            warning.setLastSeenAt(LocalDateTime.now());
            exchangeWarningRepository.save(warning);
            updated++;
        }

        return new IngestionResult(items.size(), inserted, updated);
    }

    private boolean isInvalid(WarningItem item) {
        return item.market() == null || item.market().isBlank()
                || item.warningType() == null || item.warningType().isBlank()
                || item.warningStep() == null || item.warningStep().isBlank();
    }
}
