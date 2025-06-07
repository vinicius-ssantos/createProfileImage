package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.PromptBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAIImageGenerationServiceTest {

    @Mock
    private PromptBuilderService mockPromptBuilder;

    private static class TestOpenAIImageGenerationService extends OpenAIImageGenerationService {
        private Map<String, Object> capturedRequest;

        TestOpenAIImageGenerationService(ImageGenProperties properties, PromptBuilderService promptBuilder) {
            super(properties, promptBuilder, createTestRetryTemplate(), () -> { /* no-op */ });
        }

        private static org.springframework.retry.support.RetryTemplate createTestRetryTemplate() {
            org.springframework.retry.support.RetryTemplate retryTemplate = new org.springframework.retry.support.RetryTemplate();
            org.springframework.retry.backoff.ExponentialBackOffPolicy backOffPolicy = new org.springframework.retry.backoff.ExponentialBackOffPolicy();
            backOffPolicy.setInitialInterval(1000);
            backOffPolicy.setMultiplier(2.0);
            backOffPolicy.setMaxInterval(30000);
            retryTemplate.setBackOffPolicy(backOffPolicy);

            java.util.Map<Class<? extends Throwable>, Boolean> retryableExceptions = new java.util.HashMap<>();
            retryableExceptions.put(com.example.matchapp.exception.ApiConnectionException.class, true);
            retryableExceptions.put(com.example.matchapp.exception.ApiRateLimitException.class, true);

            org.springframework.retry.policy.SimpleRetryPolicy retryPolicy = new org.springframework.retry.policy.SimpleRetryPolicy(3, retryableExceptions, true);
            retryTemplate.setRetryPolicy(retryPolicy);

            return retryTemplate;
        }

        @Override
        public byte[] generateImage(Profile profile) {
            capturedRequest = createRequest(profile);
            return new byte[] {1, 2, 3};
        }

        Map<String, Object> getCapturedRequest() {
            return capturedRequest;
        }
    }

    private TestOpenAIImageGenerationService service;

    @BeforeEach
    void setUp() {
        ImageGenProperties props = new ImageGenProperties();
        props.setApiKey("test-key");
        props.setBaseUrl("https://api.openai.com");
        service = new TestOpenAIImageGenerationService(props, mockPromptBuilder);
    }

    @Test
    void generateImage_includesModelField() {
        // Arrange
        Profile profile = new Profile(
                "id",
                "First",
                "Last",
                30,
                "Ethnicity",
                com.example.matchapp.model.Gender.MALE,
                "Sample bio",
                "img.jpg",
                "INTJ"
        );

        // Set up the mock to return a non-null prompt
        when(mockPromptBuilder.buildPrompt(profile)).thenReturn("Test prompt");

        // Act
        service.generateImage(profile);

        // Assert
        Map<String, Object> request = service.getCapturedRequest();
        assertEquals("dall-e-3", request.get("model"));
    }

    @Test
    void generateImage_usesPromptBuilderService() {
        // Arrange
        String expectedPrompt = "Test prompt for image generation";
        Profile profile = new Profile(
                "test-id",
                "Jane",
                "Doe",
                28,
                "Asian",
                com.example.matchapp.model.Gender.FEMALE,
                "Professional photographer",
                "jane.jpg",
                "ENFJ"
        );

        when(mockPromptBuilder.buildPrompt(profile)).thenReturn(expectedPrompt);

        // Act
        service.generateImage(profile);

        // Assert
        Map<String, Object> request = service.getCapturedRequest();
        assertEquals(expectedPrompt, request.get("prompt"), "Should use prompt from PromptBuilderService");
        verify(mockPromptBuilder).buildPrompt(profile);
    }
}
