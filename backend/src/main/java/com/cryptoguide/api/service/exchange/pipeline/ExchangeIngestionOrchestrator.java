package com.cryptoguide.api.service.exchange.pipeline;

import com.cryptoguide.api.service.exchange.pipeline.model.IngestionResult;
import com.cryptoguide.api.service.exchange.pipeline.source.ExchangeNoticeSource;
import com.cryptoguide.api.service.exchange.pipeline.source.ExchangeWarningSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeIngestionOrchestrator {

    private final NoticeIngestionService noticeIngestionService;
    private final WarningIngestionService warningIngestionService;
    private final List<ExchangeNoticeSource> noticeSources;
    private final List<ExchangeWarningSource> warningSources;

    public Map<String, IngestionResult> ingestAllNotices() {
        Map<String, IngestionResult> results = new LinkedHashMap<>();

        for (ExchangeNoticeSource source : noticeSources) {
            try {
                IngestionResult result = noticeIngestionService.ingest(
                        source.exchange(),
                        source.fetchLatestNotices()
                );
                results.put(source.exchange(), result);
                log.info(
                        "Notice ingest finished. exchange={}, fetched={}, inserted={}, updated={}",
                        source.exchange(),
                        result.fetchedCount(),
                        result.insertedCount(),
                        result.updatedCount()
                );
            } catch (Exception e) {
                log.error("Notice ingest failed. exchange={}", source.exchange(), e);
            }
        }

        return results;
    }

    public Map<String, IngestionResult> ingestAllWarnings() {
        Map<String, IngestionResult> results = new LinkedHashMap<>();

        for (ExchangeWarningSource source : warningSources) {
            try {
                IngestionResult result = warningIngestionService.ingest(
                        source.exchange(),
                        source.fetchLatestWarnings()
                );
                results.put(source.exchange(), result);
                log.info(
                        "Warning ingest finished. exchange={}, fetched={}, inserted={}, updated={}",
                        source.exchange(),
                        result.fetchedCount(),
                        result.insertedCount(),
                        result.updatedCount()
                );
            } catch (Exception e) {
                log.error("Warning ingest failed. exchange={}", source.exchange(), e);
            }
        }

        return results;
    }
}
