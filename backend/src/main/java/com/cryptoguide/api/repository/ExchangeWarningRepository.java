package com.cryptoguide.api.repository;

import com.cryptoguide.api.entity.ExchangeWarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ExchangeWarningRepository extends JpaRepository<ExchangeWarning, Long> {

    Optional<ExchangeWarning> findByExchangeAndMarketAndWarningTypeAndWarningStepAndEndAt(
            String exchange,
            String market,
            String warningType,
            String warningStep,
            LocalDateTime endAt
    );
}
