package com.cryptoguide.api.service.exchange.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NoticeTypeMapper {

    private static final short TYPE_GENERAL = 1;
    private static final short TYPE_TRADING_SUPPORT = 2;
    private static final short TYPE_TRADING_END = 3;
    private static final short TYPE_CAUTION = 4;
    private static final short TYPE_DEPOSIT_WITHDRAW = 5;
    private static final short TYPE_MAINTENANCE = 6;
    private static final short TYPE_FEE = 7;
    private static final short TYPE_EVENT = 8;
    private static final short TYPE_DISCLOSURE = 9;
    private static final short TYPE_SECURITY = 10;
    private static final short TYPE_SERVICE_UPDATE = 11;
    private static final short TYPE_INSIGHT = 12;
    private static final short TYPE_OTHER = 99;

    private final ObjectMapper objectMapper;

    public short mapByFirstCategory(List<String> rawCategories) {
        if (rawCategories == null || rawCategories.isEmpty()) {
            return TYPE_OTHER;
        }
        return mapByRawCategory(rawCategories.get(0));
    }

    public short mapByRawCategory(String rawCategory) {
        String c = normalize(rawCategory);
        if (c.isBlank()) {
            return TYPE_OTHER;
        }

        if (contains(c, "거래지원종료")) return TYPE_TRADING_END;
        if (contains(c, "거래유의", "유의")) return TYPE_CAUTION;
        if (contains(c, "입출금")) return TYPE_DEPOSIT_WITHDRAW;
        if (contains(c, "점검")) return TYPE_MAINTENANCE;
        if (contains(c, "수수료")) return TYPE_FEE;
        if (contains(c, "공시")) return TYPE_DISCLOSURE;
        if (contains(c, "보안")) return TYPE_SECURITY;
        if (contains(c, "업데이트", "신규서비스", "서비스+", "서비스")) return TYPE_SERVICE_UPDATE;
        if (contains(c, "인사이트")) return TYPE_INSIGHT;
        if (contains(c, "거래지원", "거래", "디지털자산", "nft", "web3", "마켓 추가", "신규")) return TYPE_TRADING_SUPPORT;
        if (contains(c, "이벤트", "당첨자발표", "후기")) return TYPE_EVENT;
        if (contains(c, "공지사항", "공지", "안내", "중요")) return TYPE_GENERAL;
        return TYPE_OTHER;
    }

    public short mapGopaxType(Short gopaxType) {
        if (gopaxType == null) {
            return TYPE_GENERAL;
        }
        return switch (gopaxType) {
            case 1 -> TYPE_GENERAL;
            case 2 -> TYPE_TRADING_SUPPORT;
            case 3 -> TYPE_EVENT;
            case 4 -> TYPE_DEPOSIT_WITHDRAW;
            default -> TYPE_OTHER;
        };
    }

    public String toCategoriesJson(List<String> rawCategories) {
        List<String> normalized = normalizeCategories(rawCategories);
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception ignored) {
            return "[]";
        }
    }

    public List<String> normalizeCategories(List<String> rawCategories) {
        if (rawCategories == null || rawCategories.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (String value : rawCategories) {
            if (value == null) {
                continue;
            }
            String text = value.replaceAll("\\s+", " ").trim();
            if (!text.isBlank() && !result.contains(text)) {
                result.add(text);
            }
        }
        return result;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private boolean contains(String target, String... keywords) {
        for (String keyword : keywords) {
            if (target.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
