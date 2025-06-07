package com.example.matchapp.exception;

/**
 * Exception thrown when authentication with the image generation API fails.
 */
public class ApiAuthenticationException extends ImageGenerationException {
    
    public ApiAuthenticationException(String message) {
        super(message);
    }
    
    public ApiAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}