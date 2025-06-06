package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Profile;
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

    // Test implementation that doesn't make real API calls
    private static class TestSpringAIImageGenerationService extends SpringAIImageGenerationService {
        private Map<String, Object> mockResponse;
        private RuntimeException mockException;
        private String capturedPrompt;
        private Map<String, Object> capturedRequestBody;

        public TestSpringAIImageGenerationService(ImageGenProperties properties) {
            super(properties);
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

        // Override the method that makes the API call
        @Override
        protected Map<String, Object> makeApiCall(String url, Map<String, Object> requestBody, HttpHeaders headers) {
            System.out.println("[DEBUG_LOG] TestSpringAIImageGenerationService.makeApiCall called");
            this.capturedPrompt = (String) requestBody.get("prompt");
            this.capturedRequestBody = new HashMap<>(requestBody);

            if (mockException != null) {
                System.out.println("[DEBUG_LOG] Throwing mock exception: " + mockException.getMessage());
                throw mockException;
            }

            System.out.println("[DEBUG_LOG] Returning mock response: " + mockResponse);
            return mockResponse;
        }
    }

    private TestSpringAIImageGenerationService service;

    @BeforeEach
    void setUp() {
        // Create ImageGenProperties with test values
        ImageGenProperties properties = new ImageGenProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://api.openai.com");

        // Create test service with test properties
        service = new TestSpringAIImageGenerationService(properties);
    }

    @Test
    void generateImage_returnsImageBytes() {
        // Arrange
        Profile profile = new Profile(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity", 
            "MALE", 
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

        // Verify the correct prompt and request body were used
        assertEquals("Test bio for image generation", service.getCapturedPrompt());

        Map<String, Object> requestBody = service.getCapturedRequestBody();
        assertEquals(1, requestBody.get("n"));
        assertEquals("1024x1024", requestBody.get("size"));
        assertEquals("b64_json", requestBody.get("response_format"));
        assertEquals("dall-e-3", requestBody.get("model"));
    }

    @Test
    void generateImage_handlesNullResponse() {
        // Arrange
        Profile profile = new Profile(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity", 
            "MALE", 
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
        Profile profile = new Profile(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity", 
            "MALE", 
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
        Profile profile = new Profile(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity", 
            "MALE", 
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
