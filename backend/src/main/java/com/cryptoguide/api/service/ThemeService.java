package com.cryptoguide.api.service;

import com.cryptoguide.api.dto.ThemeDto;
import com.cryptoguide.api.entity.Theme;
import com.cryptoguide.api.repository.CryptocurrencyRepository;
import com.cryptoguide.api.repository.ThemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final CryptocurrencyRepository cryptocurrencyRepository;

    @Cacheable(value = "themes")
    @Transactional(readOnly = true)
    public List<ThemeDto> getAllThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ThemeDto> getAllThemesWithCount() {
        return themeRepository.findAll().stream()
                .map(t -> ThemeDto.fromEntityWithCount(t, (int) cryptocurrencyRepository.countByThemeLargeIdOrThemeMediumIdOrThemeSmallId(t.getId())))
                .collect(Collectors.toList());
    }

    @Cacheable(value = "theme", key = "#slug")
    @Transactional(readOnly = true)
    public ThemeDto getThemeBySlug(String slug) {
        Theme theme = themeRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Theme not found: " + slug));
        return ThemeDto.fromEntity(theme);
    }

    @Transactional
    public Theme createTheme(Theme theme) {
        if (themeRepository.existsBySlug(theme.getSlug())) {
            throw new RuntimeException("Theme already exists: " + theme.getSlug());
        }
        return themeRepository.save(theme);
    }

    @Transactional
    public void initializeDefaultThemes() {
        List<Theme> defaultThemes = List.of(
            Theme.builder().slug("stablecoin").name("스테이블코인").color("#10B981")
                    .description("미국 달러 등 법정화폐에 가치가 고정된 암호화폐").build(),
            Theme.builder().slug("defi").name("DeFi").color("#3B82F6")
                    .description("탈중앙화 금융 서비스를 제공하는 프로젝트").build(),
            Theme.builder().slug("nft").name("NFT/메타버스").color("#8B5CF6")
                    .description("NFT 및 메타버스 관련 프로젝트").build(),
            Theme.builder().slug("layer1").name("Layer 1").color("#F59E0B")
                    .description("자체 블록체인을 보유한 레이어 1 프로젝트").build(),
            Theme.builder().slug("layer2").name("Layer 2").color("#EC4899")
                    .description("레이어 1 확장성을 개선하는 레이어 2 솔루션").build(),
            Theme.builder().slug("modular").name("모듈러").color("#06B6D4")
                    .description("모듈러 블록체인 아키텍처 프로젝트").build(),
            Theme.builder().slug("depin").name("DePIN").color("#84CC16")
                    .description("탈중앙화 물리적 인프라 네트워크").build(),
            Theme.builder().slug("ai").name("AI").color("#F43F5E")
                    .description("인공지능 관련 블록체인 프로젝트").build(),
            Theme.builder().slug("gaming").name("게임").color("#6366F1")
                    .description("블록체인 게임 및 게임파이 프로젝트").build(),
            Theme.builder().slug("meme").name("밈코인").color("#FBBF24")
                    .description("밈에서 영감을 받은 암호화폐").build(),
            Theme.builder().slug("exchange").name("거래소 토큰").color("#14B8A6")
                    .description("암호화폐 거래소에서 발행한 토큰").build(),
            Theme.builder().slug("privacy").name("프라이버시").color("#64748B")
                    .description("익명성과 프라이버시를 강조하는 프로젝트").build()
        );

        for (Theme theme : defaultThemes) {
            if (!themeRepository.existsBySlug(theme.getSlug())) {
                themeRepository.save(theme);
                log.info("Created theme: {}", theme.getSlug());
            }
        }
    }
}
