package com.example.matchapp.service;

/**
 * Service to limit the rate of outgoing requests to the image generation API.
 */
public interface RateLimiterService {
    /**
     * Acquire permission to perform a request.
     * @throws com.example.matchapp.exception.RateLimitExceededException if the limit is reached
     */
    void acquire();
}
