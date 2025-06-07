package com.example.matchapp.exception;

/**
 * Base exception for all image generation related errors.
 */
public class ImageGenerationException extends RuntimeException {
    
    public ImageGenerationException(String message) {
        super(message);
    }
    
    public ImageGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}