package com.cryptoguide.api.dto;

import com.cryptoguide.api.entity.Theme;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThemeDto {
    private Long id;
    private String slug;
    private String name;
    private String description;
    private String color;
    private String iconUrl;
    private Integer cryptoCount;

    public static ThemeDto fromEntity(Theme entity) {
        return entity == null ? null : ThemeDto.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .name(entity.getName())
                .description(entity.getDescription())
                .color(entity.getColor())
                .iconUrl(entity.getIconUrl())
                .build();
    }

    public static ThemeDto fromEntityWithCount(Theme entity, Integer count) {
        ThemeDto dto = fromEntity(entity);
        if (dto != null) dto.setCryptoCount(count);
        return dto;
    }
}
