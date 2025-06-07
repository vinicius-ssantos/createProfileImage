package com.example.matchapp.health;

import com.example.matchapp.config.ImageGenProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator that checks the availability of the OpenAI API.
 * This indicator will be included in the health endpoint response.
 */
@Component
public class OpenAIHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIHealthIndicator.class);
    private static final String HEALTH_CHECK_TIMEOUT_SECONDS = "5";

    private final WebClient webClient;
    private final ImageGenProperties properties;

    public OpenAIHealthIndicator(@org.springframework.beans.factory.annotation.Qualifier("imageGenProperties") ImageGenProperties properties) {
        // Create defensive copy to prevent external modification
        this.properties = copyImageGenProperties(properties);
        this.webClient = WebClient.builder()
                .baseUrl(this.properties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.properties.getApiKey())
                .build();
    }

    /**
     * Creates a defensive copy of ImageGenProperties.
     *
     * @param original the original ImageGenProperties object
     * @return a new ImageGenProperties object with the same properties
     */
    private ImageGenProperties copyImageGenProperties(ImageGenProperties original) {
        if (original == null) {
            return null;
        }
        ImageGenProperties copy = new ImageGenProperties();
        copy.setApiKey(original.getApiKey());
        copy.setBaseUrl(original.getBaseUrl());
        copy.setModel(original.getModel());
        copy.setImageSize(original.getImageSize());
        copy.setMaxRetries(original.getMaxRetries());
        copy.setRetryDelay(original.getRetryDelay());
        copy.setUseMock(original.isUseMock());
        copy.setRequestsPerMinute(original.getRequestsPerMinute());
        copy.setBurstCapacity(original.getBurstCapacity());
        copy.setProvider(original.getProvider());
        copy.setSpringAiBaseUrl(original.getSpringAiBaseUrl());
        copy.setSpringAiModel(original.getSpringAiModel());
        return copy;
    }

    @Override
    public Health health() {
        // If using mock in test environment, return UP without checking
        if (properties.isUseMock()) {
            return Health.up()
                    .withDetail("mode", "mock")
                    .withDetail("message", "Using mock OpenAI service")
                    .build();
        }

        try {
            // Perform a lightweight check to see if the OpenAI API is accessible
            // We're not actually generating an image, just checking if the API is responsive
            return checkApiAvailability()
                    .onErrorResume(e -> {
                        logger.warn("OpenAI API health check failed: {}", e.getMessage());
                        return Mono.just(Health.down()
                                .withDetail("error", e.getMessage())
                                .withDetail("apiUrl", properties.getBaseUrl())
                                .build());
                    })
                    .block(Duration.ofSeconds(Integer.parseInt(HEALTH_CHECK_TIMEOUT_SECONDS)));
        } catch (Exception e) {
            logger.warn("Error during OpenAI API health check: {}", e.getMessage());
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("apiUrl", properties.getBaseUrl())
                    .build();
        }
    }

    private Mono<Health> checkApiAvailability() {
        // Create a minimal request to check API availability
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", "test");
        requestBody.put("n", 1);
        requestBody.put("size", "256x256");
        requestBody.put("model", "dall-e-2");

        return webClient.post()
                .bodyValue(requestBody)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK) || 
                        response.statusCode().equals(HttpStatus.ACCEPTED)) {
                        return Mono.just(Health.up()
                                .withDetail("status", response.statusCode().value())
                                .withDetail("apiUrl", properties.getBaseUrl())
                                .withDetail("model", properties.getModel())
                                .build());
                    } else if (response.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                        return Mono.just(Health.down()
                                .withDetail("status", response.statusCode().value())
                                .withDetail("error", "API key authentication failed")
                                .withDetail("apiUrl", properties.getBaseUrl())
                                .build());
                    } else if (response.statusCode().equals(HttpStatus.TOO_MANY_REQUESTS)) {
                        return Mono.just(Health.down()
                                .withDetail("status", response.statusCode().value())
                                .withDetail("error", "Rate limit exceeded")
                                .withDetail("apiUrl", properties.getBaseUrl())
                                .build());
                    } else {
                        return Mono.just(Health.down()
                                .withDetail("status", response.statusCode().value())
                                .withDetail("apiUrl", properties.getBaseUrl())
                                .build());
                    }
                });
    }
}
