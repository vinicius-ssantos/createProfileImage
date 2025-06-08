package com.example.matchapp.exception;

/**
 * Exception thrown when there is an issue with application configuration.
 * This can include missing required properties, invalid property values,
 * or configuration that cannot be applied.
 */
public class ConfigurationException extends ApplicationException {

    /**
     * The name of the configuration property that caused the error, if applicable.
     */
    private final String propertyName;

    /**
     * The value of the configuration property that caused the error, if applicable.
     */
    private final String propertyValue;

    /**
     * Constructs a new configuration exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ConfigurationException(String message) {
        this(message, null, null);
    }

    /**
     * Constructs a new configuration exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public ConfigurationException(String message, Throwable cause) {
        this(message, cause, null, null);
    }

    /**
     * Constructs a new configuration exception with the specified detail message and property information.
     *
     * @param message the detail message
     * @param propertyName the name of the configuration property that caused the error
     * @param propertyValue the value of the configuration property that caused the error
     */
    public ConfigurationException(String message, String propertyName, String propertyValue) {
        super(message, false); // Configuration errors are not transient
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    /**
     * Constructs a new configuration exception with the specified detail message, cause, and property information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param propertyName the name of the configuration property that caused the error
     * @param propertyValue the value of the configuration property that caused the error
     */
    public ConfigurationException(String message, Throwable cause, String propertyName, String propertyValue) {
        super(message, cause, false); // Configuration errors are not transient
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    /**
     * Returns the name of the configuration property that caused the error.
     *
     * @return the property name, or null if not applicable
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns the value of the configuration property that caused the error.
     *
     * @return the property value, or null if not applicable
     */
    public String getPropertyValue() {
        return propertyValue;
    }
}
