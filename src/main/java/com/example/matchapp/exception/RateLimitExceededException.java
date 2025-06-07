package com.example.matchapp.exception;

/**
 * Thrown when the client-side rate limit is exceeded.
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
