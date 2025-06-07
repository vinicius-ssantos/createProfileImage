package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Gender;
import com.example.matchapp.model.ImageProvider;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.service.ImageGenerationService;
import com.example.matchapp.service.PromptBuilderService;
import com.example.matchapp.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Removed MockitoExtension to avoid Java 24 compatibility issues
class ImageGenerationServiceFactoryTest {

    // Using a real ApplicationContext mock created manually
    private ApplicationContext applicationContext;

    // Using a real instance instead of a mock to avoid Java 24 compatibility issues with Mockito
    private ImageGenProperties properties;

    // Using test implementations instead of mocks
    private TestOpenAIImageGenerationService openAIService;
    private TestSpringAIImageGenerationService springAIService;

    private ImageGenerationServiceFactory factory;

    // Mock implementations for required services
    private PromptBuilderService mockPromptBuilder;
    private RateLimiterService mockRateLimiter;
    private RetryTemplate retryTemplate;

    // Test implementation of OpenAIImageGenerationService that bypasses the API key check
    private static class TestOpenAIImageGenerationService extends OpenAIImageGenerationService {
        // Static method to create properties with a dummy API key
        private static ImageGenProperties createTestProperties() {
            ImageGenProperties props = new ImageGenProperties();
            props.setApiKey("test-api-key-for-unit-tests");
            return props;
        }

        // Use a constructor that calls super with pre-configured properties and required services
        public TestOpenAIImageGenerationService(PromptBuilderService promptBuilder, RetryTemplate retryTemplate, RateLimiterService rateLimiter) {
            // Call the parent constructor with pre-configured properties and required services
            super(createTestProperties(), promptBuilder, retryTemplate, rateLimiter);
        }

        // Override WebClient creation and other initialization that might cause issues
        @Override
        public byte[] generateImage(ProfileEntity profile) {
            // Return dummy image data
            return new byte[] {1, 2, 3, 4, 5};
        }

        // Override the method that would normally make API calls
        @Override
        protected byte[] generateImageFromProvider(ProfileEntity profile) throws Exception {
            // Return dummy image data without making any API calls
            return new byte[] {1, 2, 3, 4, 5};
        }
    }

    // Test implementation of SpringAIImageGenerationService that avoids initialization issues
    private static class TestSpringAIImageGenerationService extends SpringAIImageGenerationService {
        // Static method to create properties with a dummy API key
        private static ImageGenProperties createTestProperties() {
            ImageGenProperties props = new ImageGenProperties();
            props.setApiKey("test-api-key-for-unit-tests");
            return props;
        }

        // Use a constructor that calls super with pre-configured properties and required services
        public TestSpringAIImageGenerationService(PromptBuilderService promptBuilder, RateLimiterService rateLimiter) {
            // Call the parent constructor with pre-configured properties and required services
            super(createTestProperties(), promptBuilder, rateLimiter);
        }

        // Override methods that would normally make API calls
        @Override
        public byte[] generateImage(ProfileEntity profileEntity) {
            // Return dummy image data
            return new byte[] {1, 2, 3, 4, 5};
        }

        @Override
        protected byte[] generateImageFromProvider(ProfileEntity profileEntity) {
            // Return dummy image data without making any API calls
            return new byte[] {1, 2, 3, 4, 5};
        }
    }

    @BeforeEach
    void setUp() {
        // Create a real properties object
        properties = new ImageGenProperties();

        // Create mock implementations for required services
        mockPromptBuilder = mock(PromptBuilderService.class);
        mockRateLimiter = mock(RateLimiterService.class);

        // Create a RetryTemplate for testing
        retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(30000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(com.example.matchapp.exception.ApiConnectionException.class, true);
        retryableExceptions.put(com.example.matchapp.exception.ApiRateLimitException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Create test implementations with the mock services
        openAIService = new TestOpenAIImageGenerationService(mockPromptBuilder, retryTemplate, mockRateLimiter);
        springAIService = new TestSpringAIImageGenerationService(mockPromptBuilder, mockRateLimiter);

        // Create a real ApplicationContext mock using Mockito.mock() instead of @Mock
        applicationContext = mock(ApplicationContext.class);

        // Configure the ApplicationContext mock to return our test implementations
        when(applicationContext.getBean(OpenAIImageGenerationService.class)).thenReturn(openAIService);
        when(applicationContext.getBean(SpringAIImageGenerationService.class)).thenReturn(springAIService);

        // Create the factory with the real properties and mocked context
        factory = new ImageGenerationServiceFactory(applicationContext, properties);
    }

    @Test
    void getImageGenerationService_returnsOpenAIService_whenProviderIsOpenAI() {
        // Create a new properties object with the desired provider
        ImageGenProperties testProps = new ImageGenProperties();
        testProps.setApiKey("test-api-key");
        testProps.setProvider(ImageProvider.OPENAI);

        // Create a new factory with the test properties
        ImageGenerationServiceFactory testFactory = new ImageGenerationServiceFactory(applicationContext, testProps);

        // Act
        ImageGenerationService service = testFactory.getImageGenerationService();

        // Assert
        assertInstanceOf(OpenAIImageGenerationService.class, service);
    }

    @Test
    void getImageGenerationService_returnsSpringAIService_whenProviderIsSpringAI() {
        // Create a new properties object with the desired provider
        ImageGenProperties testProps = new ImageGenProperties();
        testProps.setApiKey("test-api-key");
        testProps.setProvider(ImageProvider.SPRING_AI);

        // Create a new factory with the test properties
        ImageGenerationServiceFactory testFactory = new ImageGenerationServiceFactory(applicationContext, testProps);

        // Act
        ImageGenerationService service = testFactory.getImageGenerationService();

        // Assert
        assertInstanceOf(SpringAIImageGenerationService.class, service);
    }

    @Test
    void getImageGenerationService_returnsMockImplementation_whenProviderIsMock() {
        // Create a new properties object with the desired provider
        ImageGenProperties testProps = new ImageGenProperties();
        testProps.setApiKey("test-api-key");
        testProps.setProvider(ImageProvider.MOCK);

        // Create a new factory with the test properties
        ImageGenerationServiceFactory testFactory = new ImageGenerationServiceFactory(applicationContext, testProps);

        // Act
        ImageGenerationService service = testFactory.getImageGenerationService();

        // Assert
        // The mock implementation is a lambda, so we can't easily check its type
        // Instead, we'll verify it's not one of the other implementations
        assert !(service instanceof OpenAIImageGenerationService);
        assert !(service instanceof SpringAIImageGenerationService);
    }

    @Test
    void getImageGenerationService_returnsOpenAIService_whenProviderIsUnknown() {
        // Create a new properties object with the desired provider
        ImageGenProperties testProps = new ImageGenProperties();
        testProps.setApiKey("test-api-key");
        testProps.setProvider(null);

        // Create a new factory with the test properties
        ImageGenerationServiceFactory testFactory = new ImageGenerationServiceFactory(applicationContext, testProps);

        // Act
        ImageGenerationService service = testFactory.getImageGenerationService();

        // Assert
        assertInstanceOf(OpenAIImageGenerationService.class, service);
    }
}
