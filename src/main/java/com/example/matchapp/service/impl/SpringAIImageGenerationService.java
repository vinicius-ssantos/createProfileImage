package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.ApiAuthenticationException;
import com.example.matchapp.exception.ApiConnectionException;
import com.example.matchapp.exception.ApiRateLimitException;
import com.example.matchapp.exception.ConfigurationException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class SpringAIImageGenerationService extends AbstractImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIImageGenerationService.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;
    private final RateLimiterService rateLimiter;
    private final RetryTemplate retryTemplate;
    private final Map<String, String> cookies = new HashMap<>();

    /**
     * Creates a WebClient with cookie handling.
     * 
     * @param apiKey the API key to use for authentication
     * @param baseUrl the base URL for the API
     * @return a configured WebClient
     */
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
            logger.error("Spring AI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.");
            throw new ConfigurationException("Spring AI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.", "apiKey", apiKey);
        }
    }

    @Autowired
    public SpringAIImageGenerationService(
            @org.springframework.beans.factory.annotation.Qualifier("imageGenProperties") ImageGenProperties properties, 
            PromptBuilderService promptBuilder,
            RateLimiterService rateLimiter,
            RetryTemplate retryTemplate) {
        super(properties, promptBuilder);

        // Validate parameters before assigning to fields
        validateConstructorParameters(properties, promptBuilder, retryTemplate, rateLimiter);

        this.apiKey = properties.getApiKey();
        this.baseUrl = properties.getSpringAiBaseUrl();
        this.rateLimiter = rateLimiter;
        this.retryTemplate = retryTemplate;

        // Create the WebClient
        this.webClient = createWebClient(apiKey, baseUrl);
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
        validateApiKey(apiKey);

        // Validate the base URL
        String baseUrl = properties.getSpringAiBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            throw new ConfigurationException("Spring AI base URL is missing", "springAiBaseUrl", "null");
        }
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

        try {
            return retryTemplate.execute(context -> {
                // If this is a retry, log the retry count
                if (context.getRetryCount() > 0) {
                    logger.info("Retry attempt {} for image generation", context.getRetryCount());
                }

                try {
                    return makeApiCall(profile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw handleProviderException(e);
        }
    }

    /**
     * Makes the API call to Spring AI to generate an image.
     * 
     * @param profile the profile entity to generate an image for
     * @return the generated image as a byte array
     * @throws Exception if the API call fails
     */
    private byte[] makeApiCall(ProfileEntity profile) throws Exception {
        // Create request body
        logger.debug("Creating request body for image generation");
        Map<String, Object> requestBody = createRequest(profile);

        try {
            // This call returns JSON with base64 image.
            var response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) {
                throw new InvalidResponseException("Null response from Spring AI API");
            }

            var data = (java.util.List<Map<String, Object>>) response.get("data");
            if (data == null || data.isEmpty()) {
                throw new InvalidResponseException("Empty image data");
            }

            String base64 = (String) data.get(0).get("b64_json");
            if (base64 == null || base64.isEmpty()) {
                throw new InvalidResponseException("Missing b64_json field in response");
            }

            logger.debug("Successfully extracted base64 image data, length: {}", base64.length());
            byte[] imageBytes = Base64.getDecoder().decode(base64);
            logger.info("Successfully generated image, size: {} bytes", imageBytes.length);
            return imageBytes;
        } catch (WebClientResponseException e) {
            HttpStatusCode statusCode = e.getStatusCode();

            if (statusCode.value() == HttpStatus.UNAUTHORIZED.value() || 
                statusCode.value() == HttpStatus.FORBIDDEN.value()) {
                logger.error("Authentication failed with Spring AI API. Please check your API key.", e);
                throw new ApiAuthenticationException("Authentication failed with Spring AI API. Please check your API key.", e, "Spring AI");
            } else if (statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                logger.warn("Rate limit exceeded with Spring AI API. Will retry after backoff.", e);
                throw new ApiRateLimitException("Rate limit exceeded with Spring AI API", e, "Spring AI", null);
            } else if (statusCode.is5xxServerError()) {
                logger.warn("Server error from Spring AI API. Will retry after backoff.", e);
                throw new ApiConnectionException("Server error from Spring AI API", e, "Spring AI", statusCode.value());
            } else {
                logger.error("Error response from Spring AI API: {}", e.getResponseBodyAsString(), e);
                throw new ImageGenerationException("Error response from Spring AI API: " + statusCode.value(), e, "Spring AI", statusCode.value(), "API_ERROR", true);
            }
        } catch (Exception e) {
            if (e instanceof ApiAuthenticationException || e instanceof InvalidResponseException || e instanceof ImageGenerationException) {
                throw e; // Rethrow our custom exceptions
            } else if (e instanceof ConnectException || e instanceof SocketTimeoutException) {
                logger.warn("Connection issue with Spring AI API. Will retry after backoff.", e);
                throw new ApiConnectionException("Connection issue with Spring AI API", e, "Spring AI", null);
            } else {
                logger.error("Unexpected error during image generation", e);
                throw new ImageGenerationException("Unexpected error during image generation: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Builds the request body for the Spring AI image generation API.
     * 
     * @param profileEntity the profile entity to generate an image for
     * @return the request body as a Map
     */
    protected Map<String, Object> createRequest(ProfileEntity profileEntity) {
        // Use the promptBuilder to generate a rich prompt based on all profile attributes
        String prompt = promptBuilder.buildPrompt(profileEntity);
        logger.debug("Generated prompt: {}", prompt);

        String model = properties.getSpringAiModel();
        logger.debug("Using model: {}", model);

        return Map.of(
                "prompt", prompt,
                "n", 1,
                "size", "1024x1024",
                "response_format", "b64_json",
                "model", model
        );
    }

    @Override
    protected RuntimeException handleProviderException(Exception exception) {
        // Use the same exception types as OpenAIImageGenerationService for consistency
        if (exception instanceof ImageGenerationException) {
            // Just log and rethrow our custom exceptions
            logger.error("Error in Spring AI image generation: {}", exception.getMessage(), exception);
            return (RuntimeException) exception;
        } else if (exception instanceof WebClientResponseException) {
            WebClientResponseException webClientException = (WebClientResponseException) exception;
            HttpStatusCode statusCode = webClientException.getStatusCode();

            if (statusCode.value() == HttpStatus.UNAUTHORIZED.value() || 
                statusCode.value() == HttpStatus.FORBIDDEN.value()) {
                logger.error("Authentication failed with Spring AI API. Please check your API key.", exception);
                return new ApiAuthenticationException("Authentication failed with Spring AI API. Please check your API key.", exception, "Spring AI");
            } else if (statusCode.value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
                logger.warn("Rate limit exceeded with Spring AI API. Will retry after backoff.", exception);
                return new ApiRateLimitException("Rate limit exceeded with Spring AI API", exception, "Spring AI", null);
            } else if (statusCode.is5xxServerError()) {
                logger.warn("Server error from Spring AI API. Will retry after backoff.", exception);
                return new ApiConnectionException("Server error from Spring AI API", exception, "Spring AI", statusCode.value());
            } else {
                logger.error("Error response from Spring AI API: {}", webClientException.getResponseBodyAsString(), exception);
                return new ImageGenerationException("Error response from Spring AI API: " + statusCode.value(), exception, "Spring AI", statusCode.value(), "API_ERROR", true);
            }
        } else if (exception instanceof java.net.ConnectException || 
                   exception instanceof java.net.SocketTimeoutException) {
            logger.warn("Connection issue with Spring AI API. Will retry after backoff.", exception);
            return new ApiConnectionException("Connection issue with Spring AI API", exception, "Spring AI", null);
        } else if (exception instanceof InvalidResponseException) {
            logger.error("Invalid response from Spring AI API: {}", exception.getMessage(), exception);
            return (InvalidResponseException) exception;
        } else if (exception instanceof RuntimeException && exception.getCause() instanceof Exception) {
            // Unwrap RuntimeException to get the actual cause
            return handleProviderException((Exception) exception.getCause());
        } else {
            logger.error("Unexpected error during image generation with Spring AI: {}", exception.getMessage(), exception);
            return new ImageGenerationException("Error generating image with Spring AI: " + exception.getMessage(), exception);
        }
    }

    // Removed deprecated generateImage method as it's now properly overridden by the abstract class
}
