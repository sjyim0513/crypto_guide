package com.cryptoguide.api.repository;

import com.cryptoguide.api.entity.Cryptocurrency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptocurrencyRepository extends JpaRepository<Cryptocurrency, Long> {

    Optional<Cryptocurrency> findByCoinId(String coinId);

    Optional<Cryptocurrency> findBySymbol(String symbol);

    List<Cryptocurrency> findByNameContainingIgnoreCaseOrSymbolContainingIgnoreCase(String name, String symbol);

    Page<Cryptocurrency> findAllByOrderByMarketCapRankAsc(Pageable pageable);

    @EntityGraph(attributePaths = {"themeLarge", "themeMedium", "themeSmall"})
    @Query("SELECT c FROM Cryptocurrency c WHERE c.themeLarge.slug = :themeSlug OR c.themeMedium.slug = :themeSlug OR c.themeSmall.slug = :themeSlug ORDER BY c.marketCapRank ASC")
    Page<Cryptocurrency> findByThemeSlug(@Param("themeSlug") String themeSlug, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Cryptocurrency c WHERE c.themeLarge.id = :themeId OR c.themeMedium.id = :themeId OR c.themeSmall.id = :themeId")
    long countByThemeLargeIdOrThemeMediumIdOrThemeSmallId(@Param("themeId") Long themeId);

    @Query("SELECT c FROM Cryptocurrency c ORDER BY c.priceChangePercentage24h DESC")
    List<Cryptocurrency> findTopGainers(Pageable pageable);

    @Query("SELECT c FROM Cryptocurrency c ORDER BY c.priceChangePercentage24h ASC")
    List<Cryptocurrency> findTopLosers(Pageable pageable);

    @Query("SELECT c FROM Cryptocurrency c ORDER BY c.totalVolume DESC")
    List<Cryptocurrency> findTopByVolume(Pageable pageable);

    @Query("SELECT c FROM Cryptocurrency c WHERE c.coinId IN :coinIds")
    List<Cryptocurrency> findByCoinIds(@Param("coinIds") List<String> coinIds);

    boolean existsByCoinId(String coinId);
}
