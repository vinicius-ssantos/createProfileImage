package com.example.matchapp.exception;

/**
 * Exception thrown when authentication with the image generation API fails.
 * This typically occurs due to invalid API keys, expired tokens, or insufficient permissions.
 */
public class ApiAuthenticationException extends ImageGenerationException {

    /**
     * Constructs a new API authentication exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ApiAuthenticationException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new API authentication exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ApiAuthenticationException(String message, Throwable cause) {
        super(message, cause, "Image Generation", 401, "AUTHENTICATION_FAILED", false); // Authentication errors are not transient
    }

    /**
     * Constructs a new API authentication exception with the specified detail message, cause, and service information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param serviceName the name of the image generation service
     */
    public ApiAuthenticationException(String message, Throwable cause, String serviceName) {
        super(message, cause, serviceName, 401, "AUTHENTICATION_FAILED", false); // Authentication errors are not transient
    }
}
