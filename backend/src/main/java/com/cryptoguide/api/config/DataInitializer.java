package com.cryptoguide.api.config;

import com.cryptoguide.api.service.ThemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ThemeService themeService;

    @Override
    public void run(String... args) {
        log.info("Initializing default themes...");
        themeService.initializeDefaultThemes();
        log.info("Data initialization completed");
    }
}
