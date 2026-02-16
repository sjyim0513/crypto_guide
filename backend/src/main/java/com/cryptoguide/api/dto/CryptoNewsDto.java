package com.cryptoguide.api.dto;

import com.cryptoguide.api.entity.CryptoNews;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptoNewsDto {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private String sourceUrl;
    private String source;
    private String author;
    private String imageUrl;
    private String status;
    private LocalDateTime publishedAt;
    private List<String> relatedCryptoSymbols;
    private List<String> relatedThemes;
    private LocalDateTime createdAt;

    public static CryptoNewsDto fromEntity(CryptoNews entity) {
        return CryptoNewsDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .summary(entity.getSummary())
                .sourceUrl(entity.getSourceUrl())
                .source(entity.getSource())
                .author(entity.getAuthor())
                .imageUrl(entity.getImageUrl())
                .status(entity.getStatus().name())
                .publishedAt(entity.getPublishedAt())
                .relatedCryptoSymbols(entity.getRelatedCryptos().stream()
                        .map(c -> c.getSymbol())
                        .collect(Collectors.toList()))
                .relatedThemes(entity.getRelatedThemes().stream()
                        .map(t -> t.getName())
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
