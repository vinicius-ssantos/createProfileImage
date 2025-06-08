package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.model.Gender;
import com.example.matchapp.service.PromptBuilderService;
import com.example.matchapp.service.RateLimiterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.support.RetryTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

class SpringAIImageGenerationServiceTest {

    private static class StubPromptBuilderService implements PromptBuilderService {
        private ProfileEntity lastProfile;
        private final String prompt;

        StubPromptBuilderService(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public String buildPrompt(ProfileEntity profile) {
            this.lastProfile = profile;
            return prompt;
        }

        public ProfileEntity getLastProfile() {
            return lastProfile;
        }
    }

    // Test implementation that doesn't make real API calls
    private static class TestSpringAIImageGenerationService extends SpringAIImageGenerationService {
        private Map<String, Object> mockResponse;
        private RuntimeException mockException;
        private String capturedPrompt;
        private Map<String, Object> capturedRequestBody;

        public TestSpringAIImageGenerationService(
                ImageGenProperties properties, 
                PromptBuilderService promptBuilder,
                RateLimiterService rateLimiter,
                RetryTemplate retryTemplate) {
            super(properties, promptBuilder, rateLimiter, retryTemplate);
        }

        // Method to set the mock response
        public void setMockResponse(Map<String, Object> mockResponse) {
            this.mockResponse = mockResponse;
        }

        // Method to set a mock exception
        public void setMockException(RuntimeException mockException) {
            this.mockException = mockException;
        }

        // Method to get the captured prompt
        public String getCapturedPrompt() {
            return capturedPrompt;
        }

        // Method to get the captured request body
        public Map<String, Object> getCapturedRequestBody() {
            return capturedRequestBody;
        }

        // Override the method that generates the image from the provider
        @Override
        protected byte[] generateImageFromProvider(ProfileEntity profile) {
            System.out.println("[DEBUG_LOG] TestSpringAIImageGenerationService.generateImageFromProvider called");
            Map<String, Object> requestBody = createRequest(profile);
            this.capturedPrompt = (String) requestBody.get("prompt");
            this.capturedRequestBody = new HashMap<>(requestBody);

            if (mockException != null) {
                System.out.println("[DEBUG_LOG] Throwing mock exception: " + mockException.getMessage());
                throw mockException;
            }

            System.out.println("[DEBUG_LOG] Returning mock response: " + mockResponse);

            // Check for null response
            if (mockResponse == null) {
                throw new com.example.matchapp.exception.InvalidResponseException("Null response from Spring AI API");
            }

            // Extract base64 data from mock response
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> data = (List<Map<String, Object>>) mockResponse.get("data");

            // Check for empty data
            if (data == null || data.isEmpty()) {
                throw new com.example.matchapp.exception.InvalidResponseException("Empty image data");
            }

            // Check for missing b64_json field
            String base64 = (String) data.get(0).get("b64_json");
            if (base64 == null || base64.isEmpty()) {
                throw new com.example.matchapp.exception.InvalidResponseException("Missing b64_json field in response");
            }

            // Decode and return the image data
            return Base64.getDecoder().decode(base64);
        }
    }

    private TestSpringAIImageGenerationService service;
    private StubPromptBuilderService promptBuilder;

    @BeforeEach
    void setUp() {
        // Create ImageGenProperties with test values
        ImageGenProperties properties = new ImageGenProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://api.openai.com");

        promptBuilder = new StubPromptBuilderService("built prompt");

        // Create mock RateLimiterService
        RateLimiterService mockRateLimiter = Mockito.mock(RateLimiterService.class);

        // Create mock RetryTemplate
        RetryTemplate mockRetryTemplate = Mockito.mock(RetryTemplate.class);
        Mockito.when(mockRetryTemplate.execute(
                Mockito.any(org.springframework.retry.RetryCallback.class), 
                Mockito.any(org.springframework.retry.RecoveryCallback.class)
        )).thenAnswer(invocation -> {
            // Execute the RetryCallback directly without retrying
            return invocation.getArgument(0, org.springframework.retry.RetryCallback.class).doWithRetry(null);
        });

        // Create test service with test properties and mocks
        service = new TestSpringAIImageGenerationService(properties, promptBuilder, mockRateLimiter, mockRetryTemplate);
    }

    @Test
    void generateImage_returnsImageBytes() {
        // Arrange
        ProfileEntity profile = new ProfileEntity(
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

        // Create a sample base64 encoded string (this is just "test" encoded)
        String base64Data = "dGVzdA==";
        byte[] expectedBytes = Base64.getDecoder().decode(base64Data);

        // Create a mock response
        Map<String, Object> responseData = new HashMap<>();
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("b64_json", base64Data);
        responseData.put("data", List.of(imageData));

        // Set the mock response
        service.setMockResponse(responseData);

        // Act
        byte[] result = service.generateImage(profile);

        // Assert
        assertNotNull(result);
        assertArrayEquals(expectedBytes, result);

        // Verify the prompt came from the builder
        assertEquals("built prompt", service.getCapturedPrompt());
        assertSame(profile, promptBuilder.getLastProfile());

        Map<String, Object> requestBody = service.getCapturedRequestBody();
        assertEquals(1, requestBody.get("n"));
        assertEquals("1024x1024", requestBody.get("size"));
        assertEquals("b64_json", requestBody.get("response_format"));
        assertEquals("dall-e-2", requestBody.get("model"));
    }

    @Test
    void generateImage_handlesNullResponse() {
        // Arrange
        ProfileEntity profile = new ProfileEntity(
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

        // Set null response
        service.setMockResponse(null);

        try {
            // Act
            service.generateImage(profile);
            fail("Expected InvalidResponseException was not thrown");
        } catch (com.example.matchapp.exception.InvalidResponseException e) {
            // Assert
            assertEquals("Null response from Spring AI API", e.getMessage());
        }
    }

    @Test
    void generateImage_handlesEmptyData() {
        // Arrange
        ProfileEntity profile = new ProfileEntity(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity",
            com.example.matchapp.model.Gender.MALE,
            "Test bio for image generation", 
            "test.jpg", 
            "INTJ"
        );

        // Create a mock response with empty data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("data", List.of());

        // Set the mock response
        service.setMockResponse(responseData);

        try {
            // Act
            service.generateImage(profile);
            fail("Expected InvalidResponseException was not thrown");
        } catch (com.example.matchapp.exception.InvalidResponseException e) {
            // Assert
            assertEquals("Empty image data", e.getMessage());
        }
    }

    @Test
    void generateImage_handlesAuthenticationError() {
        // Arrange
        ProfileEntity profile = new ProfileEntity(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity",
            com.example.matchapp.model.Gender.MALE,
            "Test bio for image generation", 
            "test.jpg", 
            "INTJ"
        );

        // Set a mock exception with 401 Unauthorized
        service.setMockException(new org.springframework.web.reactive.function.client.WebClientResponseException(
            401, "Unauthorized", null, null, null));

        // Act & Assert
        com.example.matchapp.exception.ApiAuthenticationException exception = assertThrows(
            com.example.matchapp.exception.ApiAuthenticationException.class, 
            () -> {
                service.generateImage(profile);
            }
        );

        assertTrue(exception.getMessage().contains("Authentication failed with Spring AI API"));
    }
}
