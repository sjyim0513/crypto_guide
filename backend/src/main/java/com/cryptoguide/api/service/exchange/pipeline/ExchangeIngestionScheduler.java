package com.cryptoguide.api.service.exchange.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeIngestionScheduler {

    private final ExchangeIngestionOrchestrator orchestrator;

    @Scheduled(cron = "${scheduler.exchange.notice.cron:0 */5 * * * *}")
    public void runNoticeIngestion() {
        log.info("Starting scheduled notice ingestion");
        orchestrator.ingestAllNotices();
    }

    @Scheduled(cron = "${scheduler.exchange.warning.cron:30 */5 * * * *}")
    public void runWarningIngestion() {
        log.info("Starting scheduled warning ingestion");
        orchestrator.ingestAllWarnings();
    }
}
