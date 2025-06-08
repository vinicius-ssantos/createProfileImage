package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.model.Gender;
import com.example.matchapp.service.PromptBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

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
        private String capturedUrl;

        public TestSpringAIImageGenerationService(ImageGenProperties properties, PromptBuilderService promptBuilder) {
            super(properties, promptBuilder, () -> { /* no-op */ });
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

        // Method to get the captured URL
        public String getCapturedUrl() {
            return capturedUrl;
        }

        // Override the method that makes the API call
        @Override
        protected Map<String, Object> makeApiCall(String url, Map<String, Object> requestBody, HttpHeaders headers) {
            System.out.println("[DEBUG_LOG] TestSpringAIImageGenerationService.makeApiCall called");
            this.capturedPrompt = (String) requestBody.get("prompt");
            this.capturedRequestBody = new HashMap<>(requestBody);
            this.capturedUrl = url;

            if (mockException != null) {
                System.out.println("[DEBUG_LOG] Throwing mock exception: " + mockException.getMessage());
                throw mockException;
            }

            System.out.println("[DEBUG_LOG] Returning mock response: " + mockResponse);
            return mockResponse;
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
        properties.setSpringAiBaseUrl("https://api.openai.com");

        promptBuilder = new StubPromptBuilderService("built prompt");
        // Create test service with test properties
        service = new TestSpringAIImageGenerationService(properties, promptBuilder);
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
        assertEquals("dall-e-3", requestBody.get("model"));
        assertEquals("https://api.openai.com", service.getCapturedUrl());
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
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e) {
            // Assert
            assertEquals("Null response from OpenAI", e.getMessage());
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
            fail("Expected RuntimeException was not thrown");
        } catch (RuntimeException e) {
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
        service.setMockException(new RuntimeException("401 Unauthorized"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            service.generateImage(profile);
        });

        assertTrue(exception.getMessage().contains("Authentication failed with OpenAI API"));
    }
}
