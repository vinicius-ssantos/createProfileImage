package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.ApiAuthenticationException;
import com.example.matchapp.exception.ApiConnectionException;
import com.example.matchapp.exception.ApiRateLimitException;
import com.example.matchapp.exception.ImageGenerationException;
import com.example.matchapp.exception.InvalidResponseException;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.ImageGenerationService;
import com.example.matchapp.service.PromptBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
public class OpenAIImageGenerationService implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIImageGenerationService.class);

    private final WebClient webClient;
    private final String apiKey;
    private final PromptBuilderService promptBuilder;
    private final RetryTemplate retryTemplate;
    private final com.example.matchapp.service.RateLimiterService rateLimiter;
    private final Map<String, String> cookies = new HashMap<>();

    public OpenAIImageGenerationService(
            ImageGenProperties properties,
            PromptBuilderService promptBuilder,
            RetryTemplate retryTemplate,
            com.example.matchapp.service.RateLimiterService rateLimiter) {
        this.apiKey = properties.getApiKey();
        this.promptBuilder = promptBuilder;
        this.retryTemplate = retryTemplate;
        this.rateLimiter = rateLimiter;

        if (!StringUtils.hasText(apiKey) || "your_openai_key_here".equals(apiKey)) {
            logger.error("OpenAI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.");
            throw new IllegalStateException("OpenAI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.");
        }

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

        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB buffer for large responses
                .filter(cookieFilter)
                .build();
    }

    /**
     * Builds the request body for the OpenAI image generation API.
     * Exposed as a protected method to allow inspection in tests.
     * 
     * @param profile the profile to generate an image for
     * @return the request body as a Map
     */
    protected Map<String, Object> createRequest(Profile profile) {
        // Use the promptBuilder to generate a rich prompt based on all profile attributes
        String prompt = promptBuilder.buildPrompt(profile);
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
     * Generates an image for the given profile using the OpenAI API.
     * This method uses a retry mechanism for transient errors.
     *
     * @param profile the profile to generate an image for
     * @return the generated image as a byte array
     * @throws ImageGenerationException if image generation fails after retries
     */
    @Override
    @Retryable(
        value = {ApiConnectionException.class, ApiRateLimitException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 30000)
    )
    public byte[] generateImage(Profile profile) {
        MDC.put("profileId", profile.id());
        try {
            logger.info("Requesting image generation");
            rateLimiter.acquire();

            return retryTemplate.execute(context -> {
                // If this is a retry, log the retry count
                if (context.getRetryCount() > 0) {
                    logger.info("Retry attempt {} for image generation", context.getRetryCount());
                }

                Map<String, Object> request = createRequest(profile);

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
            });
        } finally {
            MDC.remove("profileId");
        }
    }
}
