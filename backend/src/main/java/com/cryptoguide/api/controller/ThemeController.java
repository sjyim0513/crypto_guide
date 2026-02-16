package com.cryptoguide.api.controller;

import com.cryptoguide.api.dto.ThemeDto;
import com.cryptoguide.api.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/themes")
@RequiredArgsConstructor
@Tag(name = "Themes", description = "테마/카테고리 API")
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping
    @Operation(summary = "테마 목록 조회", description = "모든 테마 목록을 조회합니다")
    public ResponseEntity<List<ThemeDto>> getAllThemes() {
        return ResponseEntity.ok(themeService.getAllThemes());
    }

    @GetMapping("/with-count")
    @Operation(summary = "테마 목록 조회 (코인 수 포함)", description = "각 테마별 코인 수를 포함하여 조회합니다")
    public ResponseEntity<List<ThemeDto>> getAllThemesWithCount() {
        return ResponseEntity.ok(themeService.getAllThemesWithCount());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "테마 상세 조회", description = "특정 테마의 상세 정보를 조회합니다")
    public ResponseEntity<ThemeDto> getTheme(@PathVariable String slug) {
        return ResponseEntity.ok(themeService.getThemeBySlug(slug));
    }
}
