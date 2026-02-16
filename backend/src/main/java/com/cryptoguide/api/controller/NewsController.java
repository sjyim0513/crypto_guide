package com.cryptoguide.api.controller;

import com.cryptoguide.api.dto.CryptoNewsDto;
import com.cryptoguide.api.service.CryptoNewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/news")
@RequiredArgsConstructor
@Tag(name = "News", description = "암호화폐 뉴스 API")
public class NewsController {

    private final CryptoNewsService newsService;

    @GetMapping
    @Operation(summary = "뉴스 목록 조회", description = "최신 뉴스 목록을 조회합니다")
    public ResponseEntity<Page<CryptoNewsDto>> getAllNews(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(newsService.getAllNews(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "뉴스 상세 조회", description = "특정 뉴스의 상세 정보를 조회합니다")
    public ResponseEntity<CryptoNewsDto> getNews(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    @GetMapping("/crypto/{coinId}")
    @Operation(summary = "암호화폐별 뉴스 조회", description = "특정 암호화폐 관련 뉴스를 조회합니다")
    public ResponseEntity<Page<CryptoNewsDto>> getNewsByCrypto(
            @PathVariable String coinId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(newsService.getNewsByCrypto(coinId, pageable));
    }

    @GetMapping("/theme/{themeSlug}")
    @Operation(summary = "테마별 뉴스 조회", description = "특정 테마 관련 뉴스를 조회합니다")
    public ResponseEntity<Page<CryptoNewsDto>> getNewsByTheme(
            @PathVariable String themeSlug,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(newsService.getNewsByTheme(themeSlug, pageable));
    }

    @GetMapping("/recent")
    @Operation(summary = "최근 뉴스 조회", description = "지정된 시간 내의 최근 뉴스를 조회합니다")
    public ResponseEntity<List<CryptoNewsDto>> getRecentNews(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(newsService.getRecentNews(hours));
    }

    @GetMapping("/search")
    @Operation(summary = "뉴스 검색", description = "키워드로 뉴스를 검색합니다")
    public ResponseEntity<Page<CryptoNewsDto>> searchNews(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(newsService.searchNews(keyword, pageable));
    }
}
