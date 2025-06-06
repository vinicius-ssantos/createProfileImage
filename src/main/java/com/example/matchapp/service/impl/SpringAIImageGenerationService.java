package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.ImageGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
public class SpringAIImageGenerationService implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIImageGenerationService.class);

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    @Autowired
    public SpringAIImageGenerationService(ImageGenProperties properties) {
        this.apiKey = properties.getApiKey();
        this.baseUrl = properties.getBaseUrl();
        this.restTemplate = new RestTemplate();
    }

    @Override
    public byte[] generateImage(Profile profile) {
        MDC.put("profileId", profile.id());
        try {
            logger.info("Requesting image generation using Spring AI");

            try {
                // Set up headers with API key
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + apiKey);

                // Create request body
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("prompt", profile.bio());
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
            MDC.remove("profileId");
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
