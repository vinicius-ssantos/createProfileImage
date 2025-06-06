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
}