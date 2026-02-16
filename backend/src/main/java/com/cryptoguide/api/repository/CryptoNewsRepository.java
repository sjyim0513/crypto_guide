package com.cryptoguide.api.repository;

import com.cryptoguide.api.entity.CryptoNews;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CryptoNewsRepository extends JpaRepository<CryptoNews, Long> {

    Page<CryptoNews> findByStatusOrderByPublishedAtDesc(CryptoNews.NewsStatus status, Pageable pageable);

    Page<CryptoNews> findAllByOrderByPublishedAtDesc(Pageable pageable);

    @Query("SELECT n FROM CryptoNews n JOIN n.relatedCryptos c WHERE c.coinId = :coinId ORDER BY n.publishedAt DESC")
    Page<CryptoNews> findByCryptocurrencyCoinId(@Param("coinId") String coinId, Pageable pageable);

    @Query("SELECT n FROM CryptoNews n JOIN n.relatedThemes t WHERE t.slug = :themeSlug ORDER BY n.publishedAt DESC")
    Page<CryptoNews> findByThemeSlug(@Param("themeSlug") String themeSlug, Pageable pageable);

    List<CryptoNews> findByStatusOrderByCreatedAtAsc(CryptoNews.NewsStatus status);

    @Query("SELECT n FROM CryptoNews n WHERE n.publishedAt >= :since ORDER BY n.publishedAt DESC")
    List<CryptoNews> findRecentNews(@Param("since") LocalDateTime since);

    boolean existsBySourceUrl(String sourceUrl);

    @Query("SELECT n FROM CryptoNews n WHERE n.title LIKE %:keyword% OR n.content LIKE %:keyword% ORDER BY n.publishedAt DESC")
    Page<CryptoNews> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
