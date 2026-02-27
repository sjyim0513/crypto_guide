package com.cryptoguide.api.controller;

import com.cryptoguide.api.service.exchange.pipeline.ExchangeIngestionOrchestrator;
import com.cryptoguide.api.service.exchange.pipeline.model.IngestionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/exchange-ingestion")
@RequiredArgsConstructor
public class ExchangeIngestionController {

    private final ExchangeIngestionOrchestrator orchestrator;

    @PostMapping("/notices/run-once")
    public ResponseEntity<Map<String, IngestionResult>> ingestNoticesOnce() {
        return ResponseEntity.ok(orchestrator.ingestAllNotices());
    }

    @PostMapping("/warnings/run-once")
    public ResponseEntity<Map<String, IngestionResult>> ingestWarningsOnce() {
        return ResponseEntity.ok(orchestrator.ingestAllWarnings());
    }
}
