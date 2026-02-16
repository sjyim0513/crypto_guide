package com.cryptoguide.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Crypto Guide API")
                        .version("1.0.0")
                        .description("가상자산 정보 서비스 API - 실시간 시세, 테마 분류, AI 뉴스 요약")
                        .contact(new Contact()
                                .name("Crypto Guide Team")
                                .email("support@cryptoguide.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Development Server")
                ));
    }
}
