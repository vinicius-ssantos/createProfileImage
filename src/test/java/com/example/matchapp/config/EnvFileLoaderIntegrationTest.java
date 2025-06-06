package com.example.matchapp.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EnvFileLoaderIntegrationTest {

    @Configuration
    @EnableConfigurationProperties(ImageGenProperties.class)
    static class TestConfig {
        @Bean
        EnvFileLoader envFileLoader() {
            return new EnvFileLoader();
        }
    }

    @TempDir
    Path tempDir;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void propertiesLoadedFromEnvFile() throws Exception {
        Path env = tempDir.resolve(".env");
        Files.writeString(env, "OPENAI_API_KEY=test-key\nOPENAI_BASE_URL=https://example.com\n");
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            contextRunner.run(context -> {
                ImageGenProperties props = context.getBean(ImageGenProperties.class);
                assertEquals("test-key", props.getApiKey());
                assertEquals("https://example.com", props.getBaseUrl());
            });
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }
}
