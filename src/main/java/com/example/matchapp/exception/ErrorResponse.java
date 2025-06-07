package com.example.matchapp.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response for the API.
 * Used to provide consistent error responses across the application.
 */
public record ErrorResponse(
        int status,
        String message,
        Map<String, String> errors,
        LocalDateTime timestamp
) {
    /**
     * Custom constructor that creates a defensive copy of the errors map.
     *
     * @param status the HTTP status code
     * @param message the error message
     * @param errors the map of field errors
     * @param timestamp the timestamp when the error occurred
     */
    public ErrorResponse(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.errors = errors != null ? Map.copyOf(errors) : null;
        this.timestamp = timestamp;
    }

    /**
     * Custom accessor method that returns a defensive copy of the errors map.
     *
     * @return a defensive copy of the errors map
     */
    @Override
    public Map<String, String> errors() {
        return errors != null ? Map.copyOf(errors) : null;
    }
}
