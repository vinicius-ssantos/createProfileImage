package com.example.matchapp.exception;

/**
 * Exception thrown when the image generation API returns an invalid or unexpected response.
 * This could be due to malformed JSON, missing fields, or other response parsing issues.
 */
public class InvalidResponseException extends ImageGenerationException {
    
    public InvalidResponseException(String message) {
        super(message);
    }
    
    public InvalidResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}