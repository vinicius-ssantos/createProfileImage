package com.example.matchapp.exception;

/**
 * Exception thrown when there are connection issues with the image generation API.
 * This includes network timeouts, connection refused errors, and server errors.
 * This is a transient error that can be retried.
 */
public class ApiConnectionException extends ImageGenerationException {

    /**
     * Constructs a new API connection exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ApiConnectionException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new API connection exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ApiConnectionException(String message, Throwable cause) {
        super(message, cause, "Image Generation", 500, "CONNECTION_ERROR", true); // Connection errors are transient
    }

    /**
     * Constructs a new API connection exception with the specified detail message, cause, and service information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param serviceName the name of the image generation service
     * @param statusCode the HTTP status code returned by the service
     */
    public ApiConnectionException(String message, Throwable cause, String serviceName, Integer statusCode) {
        super(message, cause, serviceName, statusCode, "CONNECTION_ERROR", true); // Connection errors are transient
    }
}
