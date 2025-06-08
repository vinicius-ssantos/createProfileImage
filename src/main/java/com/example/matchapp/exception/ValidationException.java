package com.example.matchapp.exception;

import java.util.Collections;
import java.util.Map;

/**
 * Base exception for all validation-related errors.
 * This exception is thrown when input data fails validation checks.
 */
public class ValidationException extends ApplicationException {
    
    /**
     * Map of field names to error messages.
     */
    private final Map<String, String> fieldErrors;
    
    /**
     * Constructs a new validation exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ValidationException(String message) {
        this(message, Collections.emptyMap());
    }
    
    /**
     * Constructs a new validation exception with the specified detail message and field errors.
     *
     * @param message the detail message
     * @param fieldErrors map of field names to error messages
     */
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, false); // Validation errors are not transient
        this.fieldErrors = fieldErrors != null ? Collections.unmodifiableMap(fieldErrors) : Collections.emptyMap();
    }
    
    /**
     * Constructs a new validation exception with the specified detail message, cause, and field errors.
     *
     * @param message the detail message
     * @param cause the cause
     * @param fieldErrors map of field names to error messages
     */
    public ValidationException(String message, Throwable cause, Map<String, String> fieldErrors) {
        super(message, cause, false); // Validation errors are not transient
        this.fieldErrors = fieldErrors != null ? Collections.unmodifiableMap(fieldErrors) : Collections.emptyMap();
    }
    
    /**
     * Returns the map of field names to error messages.
     *
     * @return an unmodifiable map of field names to error messages
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    /**
     * Returns whether this exception has field-specific errors.
     *
     * @return true if this exception has field-specific errors, false otherwise
     */
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}