package com.cryptoguide.api.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class OpenAIService {

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.max-tokens:500}")
    private Integer maxTokens;

    private OpenAiService openAiService;

    @PostConstruct
    public void init() {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
            log.info("OpenAI service initialized");
        } else {
            log.warn("OpenAI API key not configured. AI features will be disabled.");
        }
    }

    public String summarizeNews(String title, String content) {
        if (openAiService == null) {
            log.warn("OpenAI service not available. Returning empty summary.");
            return "AI 요약 기능이 비활성화되어 있습니다.";
        }

        String prompt = String.format("""
            다음은 암호화폐 관련 뉴스입니다. 이 뉴스를 한국어로 3-4문장으로 요약해주세요.
            요약은 핵심 정보만 포함하고, 전문 용어는 가능한 쉽게 설명해주세요.
            
            제목: %s
            
            내용:
            %s
            
            요약:
            """, title, truncateContent(content, 3000));

        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(List.of(
                            new ChatMessage("system", "당신은 암호화폐 뉴스를 요약하는 전문 분석가입니다. 정확하고 객관적인 요약을 제공합니다."),
                            new ChatMessage("user", prompt)
                    ))
                    .maxTokens(maxTokens)
                    .temperature(0.3)
                    .build();

            var response = openAiService.createChatCompletion(request);
            return response.getChoices().get(0).getMessage().getContent().trim();
        } catch (Exception e) {
            log.error("Failed to generate summary with OpenAI", e);
            throw new RuntimeException("AI 요약 생성 실패", e);
        }
    }

    public String categorizeCrypto(String name, String description) {
        if (openAiService == null) {
            return null;
        }

        String prompt = String.format("""
            다음 암호화폐 프로젝트를 분석하고, 가장 적합한 카테고리들을 선택해주세요.
            
            프로젝트 이름: %s
            설명: %s
            
            가능한 카테고리:
            - stablecoin (스테이블코인)
            - defi (탈중앙화 금융)
            - nft (NFT/메타버스)
            - layer1 (레이어 1)
            - layer2 (레이어 2)
            - modular (모듈러)
            - depin (DePIN)
            - ai (인공지능)
            - gaming (게임)
            - meme (밈코인)
            - exchange (거래소 토큰)
            - privacy (프라이버시)
            
            쉼표로 구분하여 해당하는 카테고리 슬러그만 응답해주세요 (예: defi,layer1):
            """, name, truncateContent(description, 1000));

        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(List.of(
                            new ChatMessage("system", "당신은 암호화폐 프로젝트를 분류하는 전문가입니다."),
                            new ChatMessage("user", prompt)
                    ))
                    .maxTokens(100)
                    .temperature(0.1)
                    .build();

            var response = openAiService.createChatCompletion(request);
            return response.getChoices().get(0).getMessage().getContent().trim();
        } catch (Exception e) {
            log.error("Failed to categorize crypto with OpenAI", e);
            return null;
        }
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
}
