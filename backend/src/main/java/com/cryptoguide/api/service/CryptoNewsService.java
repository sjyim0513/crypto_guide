package com.cryptoguide.api.service;

import com.cryptoguide.api.dto.CryptoNewsDto;
import com.cryptoguide.api.entity.CryptoNews;
import com.cryptoguide.api.repository.CryptoNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CryptoNewsService {

    private final CryptoNewsRepository newsRepository;
    private final OpenAIService openAIService;

    @Transactional(readOnly = true)
    public Page<CryptoNewsDto> getAllNews(Pageable pageable) {
        return newsRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(CryptoNewsDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<CryptoNewsDto> getNewsByCrypto(String coinId, Pageable pageable) {
        return newsRepository.findByCryptocurrencyCoinId(coinId, pageable)
                .map(CryptoNewsDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<CryptoNewsDto> getNewsByTheme(String themeSlug, Pageable pageable) {
        return newsRepository.findByThemeSlug(themeSlug, pageable)
                .map(CryptoNewsDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public CryptoNewsDto getNewsById(Long id) {
        CryptoNews news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found: " + id));
        return CryptoNewsDto.fromEntity(news);
    }

    @Transactional(readOnly = true)
    public List<CryptoNewsDto> getRecentNews(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return newsRepository.findRecentNews(since).stream()
                .map(CryptoNewsDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CryptoNewsDto> searchNews(String keyword, Pageable pageable) {
        return newsRepository.searchByKeyword(keyword, pageable)
                .map(CryptoNewsDto::fromEntity);
    }

    @Transactional
    public CryptoNews saveNews(CryptoNews news) {
        // Check for duplicates
        if (newsRepository.existsBySourceUrl(news.getSourceUrl())) {
            log.info("News already exists: {}", news.getSourceUrl());
            return null;
        }
        return newsRepository.save(news);
    }

    @Transactional
    public void processAndSummarizeNews(CryptoNews news) {
        try {
            news.setStatus(CryptoNews.NewsStatus.PROCESSING);
            newsRepository.save(news);

            // Generate AI summary
            String summary = openAIService.summarizeNews(news.getTitle(), news.getContent());
            
            news.setSummary(summary);
            news.setStatus(CryptoNews.NewsStatus.COMPLETED);
            newsRepository.save(news);
            
            log.info("Successfully summarized news: {}", news.getTitle());
        } catch (Exception e) {
            log.error("Failed to summarize news: {}", news.getTitle(), e);
            news.setStatus(CryptoNews.NewsStatus.FAILED);
            newsRepository.save(news);
        }
    }

    @Transactional
    public void processPendingNews() {
        List<CryptoNews> pendingNews = newsRepository
                .findByStatusOrderByCreatedAtAsc(CryptoNews.NewsStatus.PENDING);
        
        for (CryptoNews news : pendingNews) {
            processAndSummarizeNews(news);
        }
    }
}
