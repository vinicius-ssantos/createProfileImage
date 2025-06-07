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
    // Provider selection
    private ImageProvider provider = ImageProvider.OPENAI; // Default to OpenAI

    // Common properties
    private String apiKey;
    private String imageSize = "1024x1024";
    private int maxRetries = 3;
    private int retryDelay = 1000;
    private boolean useMock = false;

    // OpenAI specific properties
    private String baseUrl = "https://api.openai.com/v1/images/generations";
    private String model = "dall-e-2";

    // Spring AI specific properties
    private String springAiBaseUrl = "https://api.openai.com/v1/images/generations";
    private String springAiModel = "dall-e-2";

    // Rate limiting properties
    private int requestsPerMinute = 20; // Default: 20 requests per minute
    private int burstCapacity = 5; // Default: 5 concurrent requests

    public String getApiKey() {
        return apiKey;
    }

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
