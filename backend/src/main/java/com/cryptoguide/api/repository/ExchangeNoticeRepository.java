package com.cryptoguide.api.repository;

import com.cryptoguide.api.entity.ExchangeNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeNoticeRepository extends JpaRepository<ExchangeNotice, Long> {

    Optional<ExchangeNotice> findByExchangeAndExternalId(String exchange, String externalId);
}
