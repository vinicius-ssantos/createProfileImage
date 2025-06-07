package com.example.matchapp.exception;

/**
 * Exception thrown when there is an issue with application configuration.
 * This can include missing required properties, invalid property values,
 * or configuration that cannot be applied.
 */
public class ConfigurationException extends RuntimeException {

    /**
     * Constructs a new configuration exception with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new configuration exception with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}