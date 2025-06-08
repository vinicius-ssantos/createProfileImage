package com.example.matchapp.exception;

/**
 * Base exception for all image generation related errors.
 * This exception is thrown when there are issues with generating images
 * through external image generation services.
 */
public class ImageGenerationException extends ExternalServiceException {

    /**
     * Constructs a new image generation exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ImageGenerationException(String message) {
        this(message, null);
    }

    /**
     * Constructs a new image generation exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ImageGenerationException(String message, Throwable cause) {
        super(message, cause, "Image Generation", null, null, true); // Image generation errors are typically transient
    }

    /**
     * Constructs a new image generation exception with the specified detail message, cause, and service information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param serviceName the name of the image generation service
     * @param statusCode the HTTP status code returned by the service
     * @param errorCode the error code returned by the service
     * @param isTransientError whether this exception represents a transient error
     */
    public ImageGenerationException(String message, Throwable cause, String serviceName, Integer statusCode, String errorCode, boolean isTransientError) {
        super(message, cause, serviceName, statusCode, errorCode, isTransientError);
    }
}
