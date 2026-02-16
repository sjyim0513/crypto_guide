package com.cryptoguide.api.repository;

import com.cryptoguide.api.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    @Query("SELECT p FROM PriceHistory p WHERE p.cryptocurrency.coinId = :coinId " +
           "AND p.interval = :interval AND p.timestamp >= :from " +
           "ORDER BY p.timestamp ASC")
    List<PriceHistory> findByCryptoAndIntervalAndTimeRange(
            @Param("coinId") String coinId,
            @Param("interval") PriceHistory.TimeInterval interval,
            @Param("from") LocalDateTime from
    );

    @Query("SELECT p FROM PriceHistory p WHERE p.cryptocurrency.coinId = :coinId " +
           "AND p.interval = :interval AND p.timestamp BETWEEN :from AND :to " +
           "ORDER BY p.timestamp ASC")
    List<PriceHistory> findByCryptoAndIntervalAndTimeRange(
            @Param("coinId") String coinId,
            @Param("interval") PriceHistory.TimeInterval interval,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    void deleteByCryptocurrencyIdAndTimestampBefore(Long cryptocurrencyId, LocalDateTime before);
}
