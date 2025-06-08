package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.ApiAuthenticationException;
import com.example.matchapp.exception.ApiConnectionException;
import com.example.matchapp.exception.ApiRateLimitException;
import com.example.matchapp.exception.ImageGenerationException;
import com.example.matchapp.exception.InvalidResponseException;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.service.PromptBuilderService;
import com.example.matchapp.service.RateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.matchapp.util.LoggingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
@org.springframework.context.annotation.Profile("!test")
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
    protected byte[] generateImageFromProvider(ProfileEntity profile) {
        // Acquire a permit from the rate limiter before making the API call
        logger.debug("Waiting for rate limiter permit");
        rateLimiter.acquire();
        logger.debug("Rate limiter permit acquired");

        // Set up headers with API key
        logger.debug("Setting up HTTP headers for API request");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Create request body
        logger.debug("Creating request body for image generation");
        Map<String, Object> requestBody = new HashMap<>();
        String prompt = promptBuilder.buildPrompt(profile);
        logger.debug("Generated prompt: {}", prompt);
        requestBody.put("prompt", prompt);
        requestBody.put("n", 1);
        requestBody.put("size", "1024x1024");
        requestBody.put("response_format", "b64_json");
        String model = properties.getSpringAiModel();
        logger.debug("Using model: {}", model);
        requestBody.put("model", model);

        // Make the API call to OpenAI
        logger.info("Making API call to Spring AI with model: {}", model);
        Map<String, Object> response = makeApiCall(baseUrl, requestBody, headers);

        if (response == null) {
            logger.error("Received null response from Spring AI API");
            throw new com.example.matchapp.exception.InvalidResponseException("Null response from Spring AI API");
        }

        logger.debug("Processing API response");
        // Extract the base64 image data from the response
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null || data.isEmpty()) {
            logger.error("Received empty data array in response");
            throw new com.example.matchapp.exception.InvalidResponseException("Empty image data");
        }

        String base64Data = (String) data.get(0).get("b64_json");
        if (base64Data == null || base64Data.isEmpty()) {
            logger.error("Received empty base64 image data");
            throw new com.example.matchapp.exception.InvalidResponseException("Empty base64 image data");
        }

        logger.debug("Successfully extracted base64 image data, length: {}", base64Data.length());
        // Decode the base64 data to bytes
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);
        logger.info("Successfully generated image, size: {} bytes", imageBytes.length);
        return imageBytes;
    }

    @Override
    protected RuntimeException handleProviderException(Exception exception) {
        // Use the same exception types as OpenAIImageGenerationService for consistency
        if (exception instanceof ImageGenerationException) {
            // Just log and rethrow our custom exceptions
            logger.error("Error in Spring AI image generation: {}", exception.getMessage(), exception);
            return (RuntimeException) exception;
        } else if (exception instanceof HttpClientErrorException) {
            HttpClientErrorException httpException = (HttpClientErrorException) exception;
            int statusCode = httpException.getStatusCode().value();

            if (statusCode == 401 || statusCode == 403) {
                logger.error("Authentication failed with Spring AI API. Please check your API key.", exception);
                return new ApiAuthenticationException("Authentication failed with Spring AI API. Please check your API key.", exception);
            } else if (statusCode == 429) {
                logger.warn("Rate limit exceeded with Spring AI API. Will retry after backoff.", exception);
                return new ApiRateLimitException("Rate limit exceeded with Spring AI API", exception);
            } else {
                logger.error("Error response from Spring AI API: {}", httpException.getResponseBodyAsString(), exception);
                return new ImageGenerationException("Error response from Spring AI API: " + statusCode, exception);
            }
        } else if (exception instanceof HttpServerErrorException) {
            logger.warn("Server error from Spring AI API. Will retry after backoff.", exception);
            return new ApiConnectionException("Server error from Spring AI API", exception);
        } else if (exception instanceof java.net.ConnectException || 
                   exception instanceof java.net.SocketTimeoutException) {
            logger.warn("Connection issue with Spring AI API. Will retry after backoff.", exception);
            return new ApiConnectionException("Connection issue with Spring AI API", exception);
        } else if (exception instanceof InvalidResponseException) {
            logger.error("Invalid response from Spring AI API: {}", exception.getMessage(), exception);
            return (InvalidResponseException) exception;
        } else {
            logger.error("Unexpected error during image generation with Spring AI: {}", exception.getMessage(), exception);
            return new ImageGenerationException("Error generating image with Spring AI: " + exception.getMessage(), exception);
        }
    }

    // Removed deprecated generateImage method as it's now properly overridden by the abstract class

    /**
     * Makes an API call to the OpenAI API.
     * This method is protected to allow overriding in tests.
     *
     * @param url         The URL to call
     * @param requestBody The request body
     * @param headers     The HTTP headers
     * @return The response from the API
     */
    protected Map makeApiCall(String url, Map<String, Object> requestBody, HttpHeaders headers) {
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        return restTemplate.postForObject(url, requestEntity, Map.class);
    }
}
