package com.example.matchapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "imagegen")
public class ImageGenProperties {
    private String apiKey;
    private String baseUrl = "https://api.openai.com/v1/images/generations";
    /**
     * Maximum number of image generation requests allowed per minute.
     * Defaults to 60 which is conservative for most API quotas.
     */
    private int requestsPerMinute = 60;

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

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }
}
