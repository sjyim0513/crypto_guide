package com.cryptoguide.api.service;

import com.cryptoguide.api.dto.CryptocurrencyDto;
import com.cryptoguide.api.dto.MarketOverviewDto;
import com.cryptoguide.api.entity.Cryptocurrency;
import com.cryptoguide.api.repository.CryptocurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptocurrencyService {

    private final CryptocurrencyRepository cryptocurrencyRepository;

    @Cacheable(value = "cryptos", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<CryptocurrencyDto> getAllCryptocurrencies(Pageable pageable) {
        return cryptocurrencyRepository.findAllByOrderByMarketCapRankAsc(pageable)
                .map(CryptocurrencyDto::fromEntity);
    }

    @Cacheable(value = "crypto", key = "#coinId")
    @Transactional(readOnly = true)
    public CryptocurrencyDto getCryptocurrencyByCoinId(String coinId) {
        Cryptocurrency crypto = cryptocurrencyRepository.findByCoinId(coinId)
                .orElseThrow(() -> new RuntimeException("Cryptocurrency not found: " + coinId));
        return CryptocurrencyDto.fromEntity(crypto);
    }

    @Transactional(readOnly = true)
    public List<CryptocurrencyDto> searchCryptocurrencies(String query) {
        return cryptocurrencyRepository
                .findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(query, query)
                .stream()
                .map(CryptocurrencyDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CryptocurrencyDto> getCryptocurrenciesByTheme(String themeSlug, Pageable pageable) {
        return cryptocurrencyRepository.findByThemeSlug(themeSlug, pageable)
                .map(CryptocurrencyDto::fromEntity);
    }

    @Cacheable(value = "topGainers")
    @Transactional(readOnly = true)
    public List<CryptocurrencyDto> getTopGainers(int limit) {
        return cryptocurrencyRepository.findTopGainers(PageRequest.of(0, limit))
                .stream()
                .map(CryptocurrencyDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "topLosers")
    @Transactional(readOnly = true)
    public List<CryptocurrencyDto> getTopLosers(int limit) {
        return cryptocurrencyRepository.findTopLosers(PageRequest.of(0, limit))
                .stream()
                .map(CryptocurrencyDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "topVolume")
    @Transactional(readOnly = true)
    public List<CryptocurrencyDto> getTopByVolume(int limit) {
        return cryptocurrencyRepository.findTopByVolume(PageRequest.of(0, limit))
                .stream()
                .map(CryptocurrencyDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "marketOverview")
    @Transactional(readOnly = true)
    public MarketOverviewDto getMarketOverview() {
        List<Cryptocurrency> allCryptos = cryptocurrencyRepository.findAll();
        
        BigDecimal totalMarketCap = allCryptos.stream()
                .map(Cryptocurrency::getMarketCap)
                .filter(mc -> mc != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalVolume = allCryptos.stream()
                .map(Cryptocurrency::getTotalVolume)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long upCount = allCryptos.stream()
                .filter(c -> c.getPriceChangePercentage24h() != null && 
                            c.getPriceChangePercentage24h().compareTo(BigDecimal.ZERO) > 0)
                .count();

        long downCount = allCryptos.stream()
                .filter(c -> c.getPriceChangePercentage24h() != null && 
                            c.getPriceChangePercentage24h().compareTo(BigDecimal.ZERO) < 0)
                .count();

        // Calculate BTC and ETH dominance
        BigDecimal btcMarketCap = cryptocurrencyRepository.findByCoinId("bitcoin")
                .map(Cryptocurrency::getMarketCap)
                .orElse(BigDecimal.ZERO);

        BigDecimal ethMarketCap = cryptocurrencyRepository.findByCoinId("ethereum")
                .map(Cryptocurrency::getMarketCap)
                .orElse(BigDecimal.ZERO);

        BigDecimal btcDominance = totalMarketCap.compareTo(BigDecimal.ZERO) > 0
                ? btcMarketCap.multiply(BigDecimal.valueOf(100)).divide(totalMarketCap, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal ethDominance = totalMarketCap.compareTo(BigDecimal.ZERO) > 0
                ? ethMarketCap.multiply(BigDecimal.valueOf(100)).divide(totalMarketCap, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return MarketOverviewDto.builder()
                .totalMarketCap(totalMarketCap)
                .totalVolume24h(totalVolume)
                .btcDominance(btcDominance)
                .ethDominance(ethDominance)
                .upCount((int) upCount)
                .downCount((int) downCount)
                .totalCoins(allCryptos.size())
                .build();
    }

    @CacheEvict(value = {"cryptos", "crypto", "topGainers", "topLosers", "topVolume", "marketOverview"}, allEntries = true)
    @Transactional
    public Cryptocurrency saveCryptocurrency(Cryptocurrency cryptocurrency) {
        return cryptocurrencyRepository.save(cryptocurrency);
    }

    @CacheEvict(value = {"cryptos", "crypto", "topGainers", "topLosers", "topVolume", "marketOverview"}, allEntries = true)
    @Transactional
    public List<Cryptocurrency> saveAllCryptocurrencies(List<Cryptocurrency> cryptocurrencies) {
        return cryptocurrencyRepository.saveAll(cryptocurrencies);
    }
}
