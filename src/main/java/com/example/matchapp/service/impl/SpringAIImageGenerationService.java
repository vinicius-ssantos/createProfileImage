package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.ImageGenerationService;
import com.example.matchapp.service.PromptBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.matchapp.util.LoggingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class SpringAIImageGenerationService extends AbstractImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIImageGenerationService.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final com.example.matchapp.service.RateLimiterService rateLimiter;

    @Autowired
    public SpringAIImageGenerationService(
            @org.springframework.beans.factory.annotation.Qualifier("imageGenProperties") ImageGenProperties properties, 
            PromptBuilderService promptBuilder,
            com.example.matchapp.service.RateLimiterService rateLimiter) {
        super(properties, promptBuilder);
        this.apiKey = properties.getApiKey();
        this.baseUrl = properties.getSpringAiBaseUrl();
        this.restTemplate = new RestTemplate();
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected String getProviderName() {
        return "Spring AI";
    }

    @Override
    protected byte[] generateImageFromProvider(Profile profile) throws Exception {
        // Acquire a permit from the rate limiter before making the API call
        rateLimiter.acquire();

        // Set up headers with API key
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        String prompt = promptBuilder.buildPrompt(profile);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");
        requestBody.put("response_format", "b64_json");
        requestBody.put("model", properties.getSpringAiModel());

        // Make the API call to OpenAI
        Map<String, Object> response = makeApiCall(baseUrl, requestBody, headers);

        if (response == null) {
            throw new RuntimeException("Null response from OpenAI");
        }

        // Extract the base64 image data from the response
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("Empty image data");
        }

        String base64Data = (String) data.get(0).get("b64_json");
        if (base64Data == null || base64Data.isEmpty()) {
            throw new RuntimeException("Empty base64 image data");
        }

        // Decode the base64 data to bytes
        return Base64.getDecoder().decode(base64Data);
    }

    @Override
    protected RuntimeException handleProviderException(Exception exception) {
        if (exception.getMessage() != null && exception.getMessage().contains("401 Unauthorized")) {
            logger.error("Authentication failed with OpenAI API. Please check your API key in the .env file.", exception);
            return new IllegalStateException("Authentication failed with OpenAI API. Please check your API key in the .env file.", exception);
        } else {
            logger.error("Error generating image with Spring AI OpenAI client", exception);
            return new RuntimeException("Error generating image with Spring AI OpenAI client: " + exception.getMessage(), exception);
        }
    }

    // This method is now overridden by the abstract class
    // Keeping it for backward compatibility until all code is updated
    @Override
    @Deprecated
    public byte[] generateImage(Profile profile) {
        LoggingUtils.setProfileId(profile.id());
        try {
            logger.info("Requesting image generation using Spring AI");
            rateLimiter.acquire();

            try {
                // Set up headers with API key
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + apiKey);

                // Create request body
                Map<String, Object> requestBody = new HashMap<>();
                String prompt = promptBuilder.buildPrompt(profile);
                requestBody.put("prompt", prompt);
                requestBody.put("n", 1);
                requestBody.put("size", "1024x1024");
                requestBody.put("response_format", "b64_json");
                requestBody.put("model", "dall-e-3");

                // Make the API call to OpenAI
                Map<String, Object> response = makeApiCall(baseUrl, requestBody, headers);

                if (response == null) {
                    throw new RuntimeException("Null response from OpenAI");
                }

                // Extract the base64 image data from the response
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                if (data == null || data.isEmpty()) {
                    throw new RuntimeException("Empty image data");
                }

                String base64Data = (String) data.get(0).get("b64_json");
                if (base64Data == null || base64Data.isEmpty()) {
                    throw new RuntimeException("Empty base64 image data");
                }

                // Decode the base64 data to bytes
                return Base64.getDecoder().decode(base64Data);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("401 Unauthorized")) {
                    logger.error("Authentication failed with OpenAI API. Please check your API key in the .env file.", e);
                    throw new IllegalStateException("Authentication failed with OpenAI API. Please check your API key in the .env file.", e);
                } else if (e.getMessage() != null && (e.getMessage().equals("Null response from OpenAI") || e.getMessage().equals("Empty image data"))) {
                    logger.error("Error generating image with Spring AI OpenAI client", e);
                    throw e;
                } else {
                    logger.error("Error generating image with Spring AI OpenAI client", e);
                    throw new RuntimeException("Error generating image with Spring AI OpenAI client: " + e.getMessage(), e);
                }
            }
        } finally {
            LoggingUtils.clearMDC();
        }
    }

    /**
     * Makes an API call to the OpenAI API.
     * This method is protected to allow overriding in tests.
     *
     * @param url The URL to call
     * @param requestBody The request body
     * @param headers The HTTP headers
     * @return The response from the API
     */
    protected Map<String, Object> makeApiCall(String url, Map<String, Object> requestBody, HttpHeaders headers) {
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(url, requestEntity, Map.class);
    }
}
