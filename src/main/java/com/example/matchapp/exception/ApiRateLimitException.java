package com.example.matchapp.exception;

/**
 * Exception thrown when the image generation API rate limit is exceeded.
 * This occurs when too many requests are made to the API in a short period of time.
 * This is a transient error that can be retried after a delay.
 */
public class ApiRateLimitException extends ImageGenerationException {

    /**
     * The number of seconds to wait before retrying, if provided by the API.
     */
    private final Integer retryAfterSeconds;

    /**
     * Constructs a new API rate limit exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ApiRateLimitException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new API rate limit exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ApiRateLimitException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * Constructs a new API rate limit exception with the specified detail message, cause, and retry information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param retryAfterSeconds the number of seconds to wait before retrying
     */
    public ApiRateLimitException(String message, Throwable cause, Integer retryAfterSeconds) {
        super(message, cause, "Image Generation", 429, "RATE_LIMIT_EXCEEDED", true); // Rate limit errors are transient
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * Constructs a new API rate limit exception with the specified detail message, cause, service information, and retry information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param serviceName the name of the image generation service
     * @param retryAfterSeconds the number of seconds to wait before retrying
     */
    public ApiRateLimitException(String message, Throwable cause, String serviceName, Integer retryAfterSeconds) {
        super(message, cause, serviceName, 429, "RATE_LIMIT_EXCEEDED", true); // Rate limit errors are transient
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * Returns the number of seconds to wait before retrying, if provided by the API.
     *
     * @return the retry after seconds, or null if not provided
     */
    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
