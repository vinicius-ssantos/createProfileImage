package com.example.matchapp.config;

import com.example.matchapp.model.ImageProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for image generation.
 * These properties can be set differently for each environment (dev, test, prod)
 * using the appropriate application-{profile}.properties file.
 */
@Configuration
@ConfigurationProperties(prefix = "imagegen")
public class ImageGenProperties {
    /**
     * The image generation provider to use.
     * Defaults to OpenAI.
     */
    private ImageProvider provider = ImageProvider.OPENAI;

    // Common properties
    /**
     * API key for the image generation service.
     * This should be set in the environment variables or application properties.
     */
    private String apiKey;

    /**
     * Size of the generated images in pixels (width x height).
     * Defaults to 1024x1024.
     */
    private String imageSize = "1024x1024";

    /**
     * Maximum number of retry attempts for failed API calls.
     * Defaults to 3.
     */
    private int maxRetries = 3;

    /**
     * Delay between retry attempts in milliseconds.
     * Defaults to 1000 (1 second).
     */
    private int retryDelay = 1000;

    /**
     * Flag to use mock implementations for testing.
     * Defaults to false.
     */
    private boolean useMock = false;

    // OpenAI specific properties
    /**
     * Base URL for the OpenAI API.
     * Defaults to the OpenAI image generations endpoint.
     */
    private String baseUrl = "https://api.openai.com/v1/images/generations";

    /**
     * OpenAI model to use for image generation.
     * Defaults to "dall-e-2".
     */
    private String model = "dall-e-2";

    // Spring AI specific properties
    /**
     * Base URL for the Spring AI API.
     * Defaults to the OpenAI image generations endpoint.
     */
    private String springAiBaseUrl = "https://api.openai.com/v1/images/generations";

    /**
     * Spring AI model to use for image generation.
     * Defaults to "dall-e-2".
     */
    private String springAiModel = "dall-e-2";

    // Rate limiting properties
    /**
     * Maximum number of requests allowed per minute.
     * Used for rate limiting to prevent API quota exhaustion.
     * Defaults to 20 requests per minute.
     */
    private int requestsPerMinute = 20;

    /**
     * Maximum number of concurrent requests allowed.
     * Used for rate limiting to prevent API quota exhaustion.
     * Defaults to 5 concurrent requests.
     */
    private int burstCapacity = 5;

    /**
     * Gets the API key for the image generation service.
     *
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key for the image generation service.
     *
     * @param apiKey the API key to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    public boolean isUseMock() {
        return useMock;
    }

    public void setUseMock(boolean useMock) {
        this.useMock = useMock;
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public int getBurstCapacity() {
        return burstCapacity;
    }

    public void setBurstCapacity(int burstCapacity) {
        this.burstCapacity = burstCapacity;
    }

    public ImageProvider getProvider() {
        return provider;
    }

    public void setProvider(ImageProvider provider) {
        this.provider = provider;
    }

    public String getSpringAiBaseUrl() {
        return springAiBaseUrl;
    }

    public void setSpringAiBaseUrl(String springAiBaseUrl) {
        this.springAiBaseUrl = springAiBaseUrl;
    }

    public String getSpringAiModel() {
        return springAiModel;
    }

    public void setSpringAiModel(String springAiModel) {
        this.springAiModel = springAiModel;
    }
}
