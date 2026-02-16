package com.cryptoguide.api.controller;

import com.cryptoguide.api.dto.CryptocurrencyDto;
import com.cryptoguide.api.dto.MarketOverviewDto;
import com.cryptoguide.api.dto.PriceHistoryDto;
import com.cryptoguide.api.entity.PriceHistory;
import com.cryptoguide.api.repository.PriceHistoryRepository;
import com.cryptoguide.api.service.CryptocurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/cryptocurrencies")
@RequiredArgsConstructor
@Tag(name = "Cryptocurrencies", description = "암호화폐 정보 API")
public class CryptocurrencyController {

    private final CryptocurrencyService cryptoService;
    private final PriceHistoryRepository priceHistoryRepository;

    @GetMapping
    @Operation(summary = "암호화폐 목록 조회", description = "시가총액 순으로 암호화폐 목록을 조회합니다")
    public ResponseEntity<Page<CryptocurrencyDto>> getAllCryptocurrencies(
            @PageableDefault(size = 100) Pageable pageable) {
        return ResponseEntity.ok(cryptoService.getAllCryptocurrencies(pageable));
    }

    @GetMapping("/{coinId}")
    @Operation(summary = "암호화폐 상세 조회", description = "특정 암호화폐의 상세 정보를 조회합니다")
    public ResponseEntity<CryptocurrencyDto> getCryptocurrency(@PathVariable String coinId) {
        return ResponseEntity.ok(cryptoService.getCryptocurrencyByCoinId(coinId));
    }

    @GetMapping("/search")
    @Operation(summary = "암호화폐 검색", description = "이름 또는 심볼로 암호화폐를 검색합니다")
    public ResponseEntity<List<CryptocurrencyDto>> searchCryptocurrencies(
            @RequestParam String query) {
        return ResponseEntity.ok(cryptoService.searchCryptocurrencies(query));
    }

    @GetMapping("/theme/{themeSlug}")
    @Operation(summary = "테마별 암호화폐 조회", description = "특정 테마에 속한 암호화폐 목록을 조회합니다")
    public ResponseEntity<Page<CryptocurrencyDto>> getCryptocurrenciesByTheme(
            @PathVariable String themeSlug,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(cryptoService.getCryptocurrenciesByTheme(themeSlug, pageable));
    }

    @GetMapping("/top-gainers")
    @Operation(summary = "상승률 상위 코인", description = "24시간 상승률 상위 암호화폐를 조회합니다")
    public ResponseEntity<List<CryptocurrencyDto>> getTopGainers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(cryptoService.getTopGainers(limit));
    }

    @GetMapping("/top-losers")
    @Operation(summary = "하락률 상위 코인", description = "24시간 하락률 상위 암호화폐를 조회합니다")
    public ResponseEntity<List<CryptocurrencyDto>> getTopLosers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(cryptoService.getTopLosers(limit));
    }

    @GetMapping("/top-volume")
    @Operation(summary = "거래량 상위 코인", description = "24시간 거래량 상위 암호화폐를 조회합니다")
    public ResponseEntity<List<CryptocurrencyDto>> getTopByVolume(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(cryptoService.getTopByVolume(limit));
    }

    @GetMapping("/market-overview")
    @Operation(summary = "시장 개요", description = "전체 암호화폐 시장 개요를 조회합니다")
    public ResponseEntity<MarketOverviewDto> getMarketOverview() {
        return ResponseEntity.ok(cryptoService.getMarketOverview());
    }

    @GetMapping("/{coinId}/price-history")
    @Operation(summary = "가격 히스토리 조회", description = "특정 암호화폐의 가격 히스토리를 조회합니다")
    public ResponseEntity<List<PriceHistoryDto>> getPriceHistory(
            @PathVariable String coinId,
            @RequestParam(defaultValue = "HOUR_1") PriceHistory.TimeInterval interval,
            @RequestParam(defaultValue = "24") int hours) {
        
        LocalDateTime from = LocalDateTime.now().minusHours(hours);
        List<PriceHistoryDto> history = priceHistoryRepository
                .findByCryptoAndIntervalAndTimeRange(coinId, interval, from)
                .stream()
                .map(PriceHistoryDto::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(history);
    }
}
