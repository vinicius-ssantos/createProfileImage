package com.example.matchapp.exception;

/**
 * Base exception for all data access or persistence-related errors.
 * This exception is thrown when there are issues with database operations,
 * repository access, or other data persistence concerns.
 */
public class DataAccessException extends ApplicationException {
    
    /**
     * The entity type that was being accessed when the error occurred.
     */
    private final String entityType;
    
    /**
     * The identifier of the entity that was being accessed, if applicable.
     */
    private final String entityId;
    
    /**
     * Constructs a new data access exception with the specified detail message.
     *
     * @param message the detail message
     * @param isTransientError whether this exception represents a transient error
     */
    public DataAccessException(String message, boolean isTransientError) {
        this(message, null, null, isTransientError);
    }
    
    /**
     * Constructs a new data access exception with the specified detail message and entity information.
     *
     * @param message the detail message
     * @param entityType the type of entity being accessed
     * @param entityId the identifier of the entity being accessed
     * @param isTransientError whether this exception represents a transient error
     */
    public DataAccessException(String message, String entityType, String entityId, boolean isTransientError) {
        super(message, isTransientError);
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    /**
     * Constructs a new data access exception with the specified detail message, cause, and entity information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param entityType the type of entity being accessed
     * @param entityId the identifier of the entity being accessed
     * @param isTransientError whether this exception represents a transient error
     */
    public DataAccessException(String message, Throwable cause, String entityType, String entityId, boolean isTransientError) {
        super(message, cause, isTransientError);
        this.entityType = entityType;
        this.entityId = entityId;
    }
    
    /**
     * Returns the entity type that was being accessed when the error occurred.
     *
     * @return the entity type, or null if not applicable
     */
    public String getEntityType() {
        return entityType;
    }
    
    /**
     * Returns the identifier of the entity that was being accessed.
     *
     * @return the entity identifier, or null if not applicable
     */
    public String getEntityId() {
        return entityId;
    }
}