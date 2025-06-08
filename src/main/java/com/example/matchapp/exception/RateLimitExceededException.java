package com.example.matchapp.exception;

/**
 * Exception thrown when the client-side rate limit is exceeded.
 * This is used for internal rate limiting to prevent overwhelming external services.
 * This is a transient error that can be retried after a delay.
 */
public class RateLimitExceededException extends ApplicationException {

    /**
     * The maximum number of requests allowed in the time window.
     */
    private final Integer maxRequests;

    /**
     * The time window in seconds.
     */
    private final Integer timeWindowSeconds;

    /**
     * The number of seconds to wait before retrying.
     */
    private final Integer retryAfterSeconds;

    /**
     * Constructs a new rate limit exceeded exception with the specified detail message.
     *
     * @param message the detail message
     */
    public RateLimitExceededException(String message) {
        this(message, null, null, null);
    }

    /**
     * Constructs a new rate limit exceeded exception with the specified detail message and rate limit information.
     *
     * @param message the detail message
     * @param maxRequests the maximum number of requests allowed in the time window
     * @param timeWindowSeconds the time window in seconds
     * @param retryAfterSeconds the number of seconds to wait before retrying
     */
    public RateLimitExceededException(String message, Integer maxRequests, Integer timeWindowSeconds, Integer retryAfterSeconds) {
        super(message, true); // Rate limit errors are transient
        this.maxRequests = maxRequests;
        this.timeWindowSeconds = timeWindowSeconds;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * Constructs a new rate limit exceeded exception with the specified detail message, cause, and rate limit information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param maxRequests the maximum number of requests allowed in the time window
     * @param timeWindowSeconds the time window in seconds
     * @param retryAfterSeconds the number of seconds to wait before retrying
     */
    public RateLimitExceededException(String message, Throwable cause, Integer maxRequests, Integer timeWindowSeconds, Integer retryAfterSeconds) {
        super(message, cause, true); // Rate limit errors are transient
        this.maxRequests = maxRequests;
        this.timeWindowSeconds = timeWindowSeconds;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    /**
     * Returns the maximum number of requests allowed in the time window.
     *
     * @return the maximum requests, or null if not applicable
     */
    public Integer getMaxRequests() {
        return maxRequests;
    }

    /**
     * Returns the time window in seconds.
     *
     * @return the time window in seconds, or null if not applicable
     */
    public Integer getTimeWindowSeconds() {
        return timeWindowSeconds;
    }

    /**
     * Returns the number of seconds to wait before retrying.
     *
     * @return the retry after seconds, or null if not applicable
     */
    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
