package com.example.matchapp.exception;

/**
 * Exception thrown when there are connection issues with the image generation API.
 * This is a transient error that can be retried.
 */
public class ApiConnectionException extends ImageGenerationException {
    
    public ApiConnectionException(String message) {
        super(message);
    }
    
    public ApiConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}