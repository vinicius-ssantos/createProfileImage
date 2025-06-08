package com.example.matchapp.exception;

/**
 * Exception thrown when the image generation API returns an invalid or unexpected response.
 * This could be due to malformed JSON, missing fields, or other response parsing issues.
 * This is generally not a transient error and may require investigation.
 */
public class InvalidResponseException extends ImageGenerationException {

    /**
     * The raw response content, if available.
     */
    private final String responseContent;

    /**
     * Constructs a new invalid response exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidResponseException(String message) {
        this(message, null, null);
    }

    /**
     * Constructs a new invalid response exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidResponseException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * Constructs a new invalid response exception with the specified detail message, cause, and response content.
     *
     * @param message the detail message
     * @param cause the cause
     * @param responseContent the raw response content
     */
    public InvalidResponseException(String message, Throwable cause, String responseContent) {
        super(message, cause, "Image Generation", 200, "INVALID_RESPONSE", false); // Invalid response errors are not transient
        this.responseContent = responseContent;
    }

    /**
     * Constructs a new invalid response exception with the specified detail message, cause, service information, and response content.
     *
     * @param message the detail message
     * @param cause the cause
     * @param serviceName the name of the image generation service
     * @param statusCode the HTTP status code returned by the service
     * @param responseContent the raw response content
     */
    public InvalidResponseException(String message, Throwable cause, String serviceName, Integer statusCode, String responseContent) {
        super(message, cause, serviceName, statusCode, "INVALID_RESPONSE", false); // Invalid response errors are not transient
        this.responseContent = responseContent;
    }

    /**
     * Returns the raw response content, if available.
     *
     * @return the response content, or null if not available
     */
    public String getResponseContent() {
        return responseContent;
    }
}
