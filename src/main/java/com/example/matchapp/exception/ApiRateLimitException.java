package com.example.matchapp.exception;

/**
 * Exception thrown when the image generation API rate limit is exceeded.
 * This is a transient error that can be retried after a delay.
 */
public class ApiRateLimitException extends ImageGenerationException {
    
    public ApiRateLimitException(String message) {
        super(message);
    }
    
    public ApiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}