package com.example.matchapp.exception;

/**
 * Base exception for all application-specific exceptions.
 * This serves as the root of the exception hierarchy for the application.
 */
public abstract class ApplicationException extends RuntimeException {
    
    /**
     * Indicates whether this exception represents a transient error that might succeed if retried.
     */
    private final boolean isTransientError;
    
    /**
     * Constructs a new application exception with the specified detail message.
     *
     * @param message the detail message
     * @param isTransientError whether this exception represents a transient error
     */
    protected ApplicationException(String message, boolean isTransientError) {
        super(message);
        this.isTransientError = isTransientError;
    }
    
    /**
     * Constructs a new application exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     * @param isTransientError whether this exception represents a transient error
     */
    protected ApplicationException(String message, Throwable cause, boolean isTransientError) {
        super(message, cause);
        this.isTransientError = isTransientError;
    }
    
    /**
     * Returns whether this exception represents a transient error that might succeed if retried.
     *
     * @return true if this exception represents a transient error, false otherwise
     */
    public boolean isTransientError() {
        return isTransientError;
    }
}