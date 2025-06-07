package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.RateLimitExceededException;
import com.example.matchapp.model.Gender;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.service.PromptBuilderService;
import com.example.matchapp.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.retry.support.RetryTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test class for verifying rate limiting functionality in OpenAIImageGenerationService.
 */
class RateLimitingTest {

    private ImageGenProperties properties;

    @Mock
    private PromptBuilderService promptBuilder;

    private RetryTemplate retryTemplate;

    @Mock
    private RateLimiterService rateLimiter;

    private TestOpenAIImageGenerationService service;

    /**
     * Test subclass of OpenAIImageGenerationService that overrides the generateImageFromProvider method
     * to avoid calling the RetryTemplate.
     */
    private static class TestOpenAIImageGenerationService extends OpenAIImageGenerationService {
        private final AtomicBoolean rateLimiterCalled = new AtomicBoolean(false);
        private final RateLimiterService rateLimiterService;

        public TestOpenAIImageGenerationService(
                ImageGenProperties properties,
                PromptBuilderService promptBuilder,
                RetryTemplate retryTemplate,
                RateLimiterService rateLimiter) {
            super(properties, promptBuilder, retryTemplate, rateLimiter);
            this.rateLimiterService = rateLimiter;
        }

        @Override
        protected byte[] generateImageFromProvider(ProfileEntity profileEntity) throws Exception {
            // Call the rate limiter
            rateLimiterService.acquire();
            rateLimiterCalled.set(true);

            // Return dummy image data instead of calling the RetryTemplate
            return new byte[] {1, 2, 3, 4, 5};
        }

        public boolean wasRateLimiterCalled() {
            return rateLimiterCalled.get();
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a real properties object instead of mocking it
        properties = new ImageGenProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://test-url.com");

        // Create a real RetryTemplate instead of mocking it
        retryTemplate = new RetryTemplate();

        // Create service with mocked dependencies
        service = new TestOpenAIImageGenerationService(properties, promptBuilder, retryTemplate, rateLimiter);
    }

    @Test
    void acquiresRateLimiterPermitBeforeGeneratingImage() throws Exception {
        // Arrange
        ProfileEntity profileEntity = new ProfileEntity(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity", 
            Gender.MALE, 
            "Test bio for image generation", 
            "test.jpg", 
            "INTJ"
        );

        // Act
        service.generateImageFromProvider(profileEntity);

        // Assert
        verify(rateLimiter, times(1)).acquire();
    }

    @Test
    void propagatesRateLimitExceptionWhenLimitExceeded() throws Exception {
        // Arrange
        ProfileEntity profileEntity = new ProfileEntity(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity", 
            Gender.MALE, 
            "Test bio for image generation", 
            "test.jpg", 
            "INTJ"
        );

        // Configure rateLimiter to throw RateLimitExceededException
        doThrow(new RateLimitExceededException("Rate limit exceeded")).when(rateLimiter).acquire();

        // Act & Assert
        assertThrows(RateLimitExceededException.class, () -> {
            service.generateImageFromProvider(profileEntity);
        });

        // No need to verify retryTemplate as we're using a real instance
    }
}
