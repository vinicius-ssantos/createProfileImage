package com.example.matchapp.exception;

/**
 * Base exception for all errors related to external services.
 * This exception is thrown when there are issues with external API calls,
 * third-party services, or other external dependencies.
 */
public class ExternalServiceException extends ApplicationException {
    
    /**
     * The name of the external service where the error occurred.
     */
    private final String serviceName;
    
    /**
     * The HTTP status code returned by the external service, if applicable.
     */
    private final Integer statusCode;
    
    /**
     * The error code or identifier returned by the external service, if applicable.
     */
    private final String errorCode;
    
    /**
     * Constructs a new external service exception with the specified detail message.
     *
     * @param message the detail message
     * @param isTransientError whether this exception represents a transient error
     */
    public ExternalServiceException(String message, boolean isTransientError) {
        this(message, null, null, null, isTransientError);
    }
    
    /**
     * Constructs a new external service exception with the specified detail message and service information.
     *
     * @param message the detail message
     * @param serviceName the name of the external service
     * @param statusCode the HTTP status code returned by the external service
     * @param errorCode the error code or identifier returned by the external service
     * @param isTransientError whether this exception represents a transient error
     */
    public ExternalServiceException(String message, String serviceName, Integer statusCode, String errorCode, boolean isTransientError) {
        super(message, isTransientError);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new external service exception with the specified detail message, cause, and service information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param serviceName the name of the external service
     * @param statusCode the HTTP status code returned by the external service
     * @param errorCode the error code or identifier returned by the external service
     * @param isTransientError whether this exception represents a transient error
     */
    public ExternalServiceException(String message, Throwable cause, String serviceName, Integer statusCode, String errorCode, boolean isTransientError) {
        super(message, cause, isTransientError);
        this.serviceName = serviceName;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
    
    /**
     * Returns the name of the external service where the error occurred.
     *
     * @return the service name, or null if not applicable
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Returns the HTTP status code returned by the external service.
     *
     * @return the status code, or null if not applicable
     */
    public Integer getStatusCode() {
        return statusCode;
    }
    
    /**
     * Returns the error code or identifier returned by the external service.
     *
     * @return the error code, or null if not applicable
     */
    public String getErrorCode() {
        return errorCode;
    }
}