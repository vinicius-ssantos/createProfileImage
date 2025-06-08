package com.example.matchapp.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the API.
 * Provides consistent error responses for various exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation exceptions from Spring validation.
     *
     * @param ex the exception
     * @return a map of field errors
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.error("Validation error: {}", errors);
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                errors,
                LocalDateTime.now()
        );
    }

    /**
     * Handle custom validation exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ValidationException ex, WebRequest request) {
        logger.error("Validation exception: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("message", ex.getMessage());

        if (ex.hasFieldErrors()) {
            details.putAll(ex.getFieldErrors());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                details,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle ResponseStatusException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        logger.error("Response status exception: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getStatusCode().value(),
                ex.getReason(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    /**
     * Handle IllegalStateException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        logger.error("Illegal state exception: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle NoResourceFoundException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        logger.error("Resource not found: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                Map.of("resource", ex.getResourcePath()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle ConfigurationException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(ConfigurationException.class)
    public ResponseEntity<ErrorResponse> handleConfigurationException(ConfigurationException ex, WebRequest request) {
        logger.error("Configuration exception: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("message", ex.getMessage());

        if (ex.getPropertyName() != null) {
            details.put("propertyName", ex.getPropertyName());
            if (ex.getPropertyValue() != null) {
                details.put("propertyValue", ex.getPropertyValue());
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Configuration error",
                details,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle DataAccessException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex, WebRequest request) {
        logger.error("Data access exception: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("message", ex.getMessage());

        if (ex.getEntityType() != null) {
            details.put("entityType", ex.getEntityType());
            if (ex.getEntityId() != null) {
                details.put("entityId", ex.getEntityId());
            }
        }

        HttpStatus status = ex.isTransientError() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                "Data access error",
                details,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle ServiceException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(ServiceException ex, WebRequest request) {
        logger.error("Service exception: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("message", ex.getMessage());

        if (ex.getServiceName() != null) {
            details.put("serviceName", ex.getServiceName());
            if (ex.getOperation() != null) {
                details.put("operation", ex.getOperation());
            }
        }

        HttpStatus status = ex.isTransientError() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                "Service error",
                details,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle FileOperationException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(FileOperationException.class)
    public ResponseEntity<ErrorResponse> handleFileOperationException(FileOperationException ex, WebRequest request) {
        logger.error("File operation exception: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("message", ex.getMessage());

        if (ex.getFilePath() != null) {
            details.put("filePath", ex.getFilePath().toString());
            if (ex.getOperation() != null) {
                details.put("operation", ex.getOperation());
            }
        }

        HttpStatus status = ex.isTransientError() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                "File operation error",
                details,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle ExternalServiceException and its subclasses.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(ExternalServiceException ex, WebRequest request) {
        logger.error("External service exception: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("message", ex.getMessage());

        if (ex.getServiceName() != null) {
            details.put("serviceName", ex.getServiceName());
        }

        if (ex.getStatusCode() != null) {
            details.put("statusCode", ex.getStatusCode().toString());
        }

        if (ex.getErrorCode() != null) {
            details.put("errorCode", ex.getErrorCode());
        }

        // Special handling for specific subclasses
        if (ex instanceof ApiRateLimitException) {
            ApiRateLimitException rateLimitEx = (ApiRateLimitException) ex;
            if (rateLimitEx.getRetryAfterSeconds() != null) {
                details.put("retryAfterSeconds", rateLimitEx.getRetryAfterSeconds().toString());
            }
        } else if (ex instanceof InvalidResponseException) {
            InvalidResponseException invalidResponseEx = (InvalidResponseException) ex;
            if (invalidResponseEx.getResponseContent() != null) {
                // Truncate response content if it's too long
                String content = invalidResponseEx.getResponseContent();
                if (content.length() > 500) {
                    content = content.substring(0, 500) + "... (truncated)";
                }
                details.put("responseContent", content);
            }
        }

        HttpStatus status;
        if (ex.getStatusCode() != null && ex.getStatusCode() >= 400 && ex.getStatusCode() < 600) {
            status = HttpStatus.valueOf(ex.getStatusCode());
        } else {
            status = ex.isTransientError() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                "External service error",
                details,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle RateLimitExceededException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
        logger.error("Rate limit exceeded: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        details.put("message", ex.getMessage());

        if (ex.getMaxRequests() != null) {
            details.put("maxRequests", ex.getMaxRequests().toString());
        }

        if (ex.getTimeWindowSeconds() != null) {
            details.put("timeWindowSeconds", ex.getTimeWindowSeconds().toString());
        }

        if (ex.getRetryAfterSeconds() != null) {
            details.put("retryAfterSeconds", ex.getRetryAfterSeconds().toString());
        }

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Rate limit exceeded",
                details,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Handle ApplicationException for any custom exceptions not handled by more specific handlers.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex, WebRequest request) {
        logger.error("Application exception: {}", ex.getMessage());
        HttpStatus status = ex.isTransientError() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                "Application error",
                Map.of("message", ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle RuntimeException.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        logger.error("Runtime exception: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                Map.of("error", ex.getMessage()),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all other exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return the error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("Unhandled exception: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                Map.of("error", "Please contact the administrator"),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
