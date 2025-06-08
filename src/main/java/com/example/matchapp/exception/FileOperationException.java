package com.example.matchapp.exception;

import java.nio.file.Path;

/**
 * Base exception for all file operation-related errors.
 * This exception is thrown when there are issues with file operations such as
 * reading, writing, or deleting files.
 */
public class FileOperationException extends ApplicationException {
    
    /**
     * The path of the file that was being operated on when the error occurred.
     */
    private final Path filePath;
    
    /**
     * The type of operation that was being performed (e.g., "read", "write", "delete").
     */
    private final String operation;
    
    /**
     * Constructs a new file operation exception with the specified detail message.
     *
     * @param message the detail message
     * @param isTransientError whether this exception represents a transient error
     */
    public FileOperationException(String message, boolean isTransientError) {
        this(message, null, null, isTransientError);
    }
    
    /**
     * Constructs a new file operation exception with the specified detail message and file information.
     *
     * @param message the detail message
     * @param filePath the path of the file that was being operated on
     * @param operation the type of operation that was being performed
     * @param isTransientError whether this exception represents a transient error
     */
    public FileOperationException(String message, Path filePath, String operation, boolean isTransientError) {
        super(message, isTransientError);
        this.filePath = filePath;
        this.operation = operation;
    }
    
    /**
     * Constructs a new file operation exception with the specified detail message, cause, and file information.
     *
     * @param message the detail message
     * @param cause the cause
     * @param filePath the path of the file that was being operated on
     * @param operation the type of operation that was being performed
     * @param isTransientError whether this exception represents a transient error
     */
    public FileOperationException(String message, Throwable cause, Path filePath, String operation, boolean isTransientError) {
        super(message, cause, isTransientError);
        this.filePath = filePath;
        this.operation = operation;
    }
    
    /**
     * Returns the path of the file that was being operated on when the error occurred.
     *
     * @return the file path, or null if not applicable
     */
    public Path getFilePath() {
        return filePath;
    }
    
    /**
     * Returns the type of operation that was being performed when the error occurred.
     *
     * @return the operation, or null if not applicable
     */
    public String getOperation() {
        return operation;
    }
}