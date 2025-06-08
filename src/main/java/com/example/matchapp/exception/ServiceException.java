package com.example.matchapp.exception;

/**
 * Base exception for all service-specific errors.
 * This exception is thrown when there are issues with business logic or service operations.
 */
public class ServiceException extends ApplicationException {
    
    /**
     * The name of the service where the error occurred.
     */
    private final String serviceName;
    
    /**
     * The operation that was being performed when the error occurred.
     */
    private final String operation;
    
    /**
     * Constructs a new service exception with the specified detail message.
     *
     * @param message the detail message
     * @param isTransientError whether this exception represents a transient error
     */
    public ServiceException(String message, boolean isTransientError) {
        this(message, null, null, isTransientError);
    }
    
    /**
     * Constructs a new service exception with the specified detail message and service information.
     *
     * @param message the detail message
     * @param serviceName the name of the service where the error occurred
     * @param operation the operation that was being performed
     * @param isTransientError whether this exception represents a transient error
     */
    public ServiceException(String message, String serviceName, String operation, boolean isTransientError) {
        super(message, isTransientError);
        this.serviceName = serviceName;
        this.operation = operation;
    }
    
    /**
     * Constructs a new service exception with the specified detail message, cause, and service information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param serviceName the name of the service where the error occurred
     * @param operation the operation that was being performed
     * @param isTransientError whether this exception represents a transient error
     */
    public ServiceException(String message, Throwable cause, String serviceName, String operation, boolean isTransientError) {
        super(message, cause, isTransientError);
        this.serviceName = serviceName;
        this.operation = operation;
    }
    
    /**
     * Returns the name of the service where the error occurred.
     *
     * @return the service name, or null if not applicable
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Returns the operation that was being performed when the error occurred.
     *
     * @return the operation, or null if not applicable
     */
    public String getOperation() {
        return operation;
    }
}