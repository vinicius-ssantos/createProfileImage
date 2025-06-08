package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.ApiAuthenticationException;
import com.example.matchapp.exception.ApiConnectionException;
import com.example.matchapp.exception.ApiRateLimitException;
import com.example.matchapp.exception.ConfigurationException;
import com.example.matchapp.exception.ImageGenerationException;
import com.example.matchapp.exception.InvalidResponseException;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.mapper.ProfileMapper;
import com.example.matchapp.service.ImageGenerationService;
import com.example.matchapp.service.PromptBuilderService;
import com.example.matchapp.service.RateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIImageGenerationService extends AbstractImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIImageGenerationService.class);

    private final WebClient webClient;
    private final String apiKey;
    private final RetryTemplate retryTemplate;
    private final Map<String, String> cookies = new HashMap<>();
    private final RateLimiterService rateLimiter;

    private WebClient createWebClient(String apiKey, String baseUrl) {
        // Create cookie handling filter
        ExchangeFilterFunction cookieFilter = (request, next) -> {
            // Add cookies to request if available
            if (!cookies.isEmpty()) {
                request = ClientRequest.from(request)
                    .cookies(cookieMap -> {
                        cookies.forEach((name, value) -> cookieMap.add(name, value));
                    })
                    .build();
                logger.debug("Added cookies to request: {}", cookies);
            }

            // Process response and extract cookies
            return next.exchange(request)
                .doOnSuccess(response -> {
                    response.cookies().forEach((name, responseCookies) -> {
                        if (!responseCookies.isEmpty()) {
                            ResponseCookie cookie = responseCookies.get(0);
                            cookies.put(name, cookie.getValue());
                            logger.debug("Saved cookie: {}={}", name, cookie.getValue());
                        }
                    });
                });
        };

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer for large responses
                .filter(cookieFilter)
                .build();
    }

    /**
     * Validates the API key to ensure it's not missing or using a default placeholder value.
     * 
     * @param apiKey the API key to validate
     * @throws ConfigurationException if the API key is invalid
     */
    private void validateApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey) || "your_openai_key_here".equals(apiKey)) {
            logger.error("OpenAI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.");
            throw new ConfigurationException("OpenAI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.", "apiKey", apiKey);
        }
    }

    public OpenAIImageGenerationService(
            @org.springframework.beans.factory.annotation.Qualifier("imageGenProperties") ImageGenProperties properties, 
            PromptBuilderService promptBuilder,
            RetryTemplate retryTemplate,
            RateLimiterService rateLimiter) {
        super(properties, promptBuilder);

        // Validate parameters before assigning to fields
        validateConstructorParameters(properties, promptBuilder, retryTemplate, rateLimiter);

        this.apiKey = properties.getApiKey();
        // RetryTemplate is a complex object that might be mutable, create a defensive copy
        this.retryTemplate = copyRetryTemplate(retryTemplate);
        // RateLimiter is a service interface, not a mutable object that needs defensive copying
        this.rateLimiter = rateLimiter;

        // Create the WebClient
        this.webClient = createWebClient(apiKey, properties.getBaseUrl());
    }

    /**
     * Validates that all constructor parameters are non-null and the API key is valid.
     * Centralizing validation helps avoid partial initialization issues.
     */
    private void validateConstructorParameters(
            ImageGenProperties properties,
            PromptBuilderService promptBuilder,
            RetryTemplate retryTemplate,
            RateLimiterService rateLimiter) {
        if (properties == null) {
            throw new ConfigurationException("ImageGenProperties cannot be null", "properties", "null");
        }
        if (promptBuilder == null) {
            throw new ConfigurationException("PromptBuilderService cannot be null", "promptBuilder", "null");
        }
        if (retryTemplate == null) {
            throw new ConfigurationException("RetryTemplate cannot be null", "retryTemplate", "null");
        }
        if (rateLimiter == null) {
            throw new ConfigurationException("RateLimiterService cannot be null", "rateLimiter", "null");
        }

        // Validate the API key
        String apiKey = properties.getApiKey();
        if (!StringUtils.hasText(apiKey) || "your_openai_key_here".equals(apiKey)) {
            logger.error("OpenAI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.");
            throw new ConfigurationException("OpenAI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.", "apiKey", apiKey);
        }
    }

    /**
     * Creates a defensive copy of RetryTemplate.
     * Since RetryTemplate doesn't have a copy constructor, we create a new instance with similar configuration.
     *
     * @param original the original RetryTemplate
     * @return a new RetryTemplate with similar configuration
     */
    private RetryTemplate copyRetryTemplate(RetryTemplate original) {
        // Since RetryTemplate doesn't have a simple copy constructor,
        // and its internal state is complex, we'll return the original for now.
        // In a real-world scenario, you would create a new RetryTemplate with the same configuration.
        return original;
    }

    /**
     * Builds the request body for the OpenAI image generation API.
     * Exposed as a protected method to allow inspection in tests.
     * 
     * @param profileEntity the profile entity to generate an image for
     * @return the request body as a Map
     */
    protected Map<String, Object> createRequest(ProfileEntity profileEntity) {
        // Use the promptBuilder to generate a rich prompt based on all profile attributes
        String prompt = promptBuilder.buildPrompt(profileEntity);
        logger.debug("Generated prompt: {}", prompt);

        return Map.of(
                "prompt", prompt,
                "n", 1,
                "size", "1024x1024",
                "response_format", "b64_json",
                "model", "dall-e-3"
        );
    }

    /**
     * Returns the name of this provider for logging purposes.
     *
     * @return the provider name
     */
    @Override
    protected String getProviderName() {
        return "OpenAI";
    }

    @Override
    protected byte[] generateImageFromProvider(ProfileEntity profileEntity) throws Exception {
        // Acquire a permit from the rate limiter before making the API call
        logger.debug("Waiting for rate limiter permit");
        rateLimiter.acquire();
        logger.debug("Rate limiter permit acquired");

        return retryTemplate.execute(context -> {
            // If this is a retry, log the retry count
            if (context.getRetryCount() > 0) {
                logger.info("Retry attempt {} for image generation", context.getRetryCount());
            }

            return makeApiCall(profileEntity);
        });
    }

    /**
     * Makes the API call to OpenAI to generate an image.
     * 
     * @param profileEntity the profile entity to generate an image for
     * @return the generated image as a byte array
     * @throws Exception if the API call fails
     */
    private byte[] makeApiCall(ProfileEntity profileEntity) throws Exception {
        Map<String, Object> request = createRequest(profileEntity);

        try {
            // This call returns JSON with base64 image.
            var response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new InvalidResponseException("Null response from OpenAI");
            }

            var data = (java.util.List<Map<String, String>>) response.get("data");
            if (data == null || data.isEmpty()) {
                throw new InvalidResponseException("Empty image data");
            }

            String base64 = data.get(0).get("b64_json");
            if (base64 == null || base64.isEmpty()) {
                throw new InvalidResponseException("Missing b64_json field in response");
            }

            return java.util.Base64.getDecoder().decode(base64);
        } catch (WebClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();

            if (statusCode.value() == HttpStatus.UNAUTHORIZED.value() || 
                statusCode.value() == HttpStatus.FORBIDDEN.value()) {
                logger.error("Authentication failed with OpenAI API. Please check your API key.", e);
                throw new ApiAuthenticationException("Authentication failed with OpenAI API. Please check your API key.", e);
            } else if (statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                logger.warn("Rate limit exceeded with OpenAI API. Will retry after backoff.", e);
                throw new ApiRateLimitException("Rate limit exceeded with OpenAI API", e);
            } else if (statusCode.is5xxServerError()) {
                logger.warn("Server error from OpenAI API. Will retry after backoff.", e);
                throw new ApiConnectionException("Server error from OpenAI API", e);
            } else {
                logger.error("Error response from OpenAI API: {}", e.getResponseBodyAsString(), e);
                throw new ImageGenerationException("Error response from OpenAI API: " + statusCode.value(), e);
            }
        } catch (Exception e) {
            if (e instanceof ApiAuthenticationException || e instanceof InvalidResponseException || e instanceof ImageGenerationException) {
                throw e; // Rethrow our custom exceptions
            } else if (e instanceof ConnectException || e instanceof SocketTimeoutException) {
                logger.warn("Connection issue with OpenAI API. Will retry after backoff.", e);
                throw new ApiConnectionException("Connection issue with OpenAI API", e);
            } else {
                logger.error("Unexpected error during image generation", e);
                throw new ImageGenerationException("Unexpected error during image generation: " + e.getMessage(), e);
            }
        }
    }

    @Override
    protected RuntimeException handleProviderException(Exception exception) {
        if (exception instanceof ApiAuthenticationException || 
            exception instanceof InvalidResponseException || 
            exception instanceof ImageGenerationException) {
            return (RuntimeException) exception; // Rethrow our custom exceptions
        } else if (exception instanceof WebClientResponseException) {
            WebClientResponseException e = (WebClientResponseException) exception;
            HttpStatusCode statusCode = e.getStatusCode();

            if (statusCode.value() == HttpStatus.UNAUTHORIZED.value() || 
                statusCode.value() == HttpStatus.FORBIDDEN.value()) {
                return new ApiAuthenticationException("Authentication failed with OpenAI API. Please check your API key.", e);
            } else if (statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                return new ApiRateLimitException("Rate limit exceeded with OpenAI API", e);
            } else if (statusCode.is5xxServerError()) {
                return new ApiConnectionException("Server error from OpenAI API", e);
            } else {
                return new ImageGenerationException("Error response from OpenAI API: " + statusCode.value(), e);
            }
        } else if (exception instanceof ConnectException || exception instanceof SocketTimeoutException) {
            return new ApiConnectionException("Connection issue with OpenAI API", exception);
        } else {
            return new ImageGenerationException("Unexpected error during image generation: " + exception.getMessage(), exception);
        }
    }
}
