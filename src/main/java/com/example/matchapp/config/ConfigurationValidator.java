package com.example.matchapp.config;

import com.example.matchapp.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Validates application configuration at startup to ensure all required settings are present and valid.
 * This helps prevent runtime errors due to missing or invalid configuration.
 */
@Component
public class ConfigurationValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationValidator.class);

    private final ImageGenProperties imageGenProperties;
    private final BackupProperties backupProperties;

    /**
     * Creates a new configuration validator with the specified properties.
     *
     * @param imageGenProperties the image generation properties to validate
     * @param backupProperties the backup properties to validate
     */
    @Autowired
    public ConfigurationValidator(ImageGenProperties imageGenProperties, BackupProperties backupProperties) {
        // Create defensive copies to prevent external modification
        this.imageGenProperties = copyImageGenProperties(imageGenProperties);
        this.backupProperties = copyBackupProperties(backupProperties);
    }

    /**
     * Creates a defensive copy of ImageGenProperties.
     *
     * @param original the original ImageGenProperties object
     * @return a new ImageGenProperties object with the same properties
     */
    private ImageGenProperties copyImageGenProperties(ImageGenProperties original) {
        if (original == null) {
            return null;
        }
        ImageGenProperties copy = new ImageGenProperties();
        copy.setApiKey(original.getApiKey());
        copy.setBaseUrl(original.getBaseUrl());
        copy.setModel(original.getModel());
        copy.setImageSize(original.getImageSize());
        copy.setMaxRetries(original.getMaxRetries());
        copy.setRetryDelay(original.getRetryDelay());
        copy.setUseMock(original.isUseMock());
        copy.setRequestsPerMinute(original.getRequestsPerMinute());
        copy.setBurstCapacity(original.getBurstCapacity());
        copy.setProvider(original.getProvider());
        copy.setSpringAiBaseUrl(original.getSpringAiBaseUrl());
        copy.setSpringAiModel(original.getSpringAiModel());
        return copy;
    }

    /**
     * Creates a defensive copy of BackupProperties.
     *
     * @param original the original BackupProperties object
     * @return a new BackupProperties object with the same properties
     */
    private BackupProperties copyBackupProperties(BackupProperties original) {
        if (original == null) {
            return null;
        }
        BackupProperties copy = new BackupProperties();
        copy.setBackupDir(original.getBackupDir());
        copy.setAutoBackup(original.isAutoBackup());
        copy.setMaxBackups(original.getMaxBackups());
        copy.setDefaultOverwrite(original.isDefaultOverwrite());
        return copy;
    }

    /**
     * Validates all configuration properties when the application is ready.
     * Throws a ConfigurationException if any required property is missing or invalid.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        logger.info("Validating application configuration...");

        validateImageGenProperties();
        validateBackupProperties();

        logger.info("Configuration validation completed successfully");
    }

    /**
     * Validates image generation properties.
     * Throws a ConfigurationException if any required property is missing or invalid.
     */
    private void validateImageGenProperties() {
        // Validate API key (required for non-mock mode)
        if (!imageGenProperties.isUseMock() && !StringUtils.hasText(imageGenProperties.getApiKey())) {
            throw new ConfigurationException("API key is required when not in mock mode");
        }

        // Validate base URL based on provider
        switch (imageGenProperties.getProvider()) {
            case OPENAI:
                if (!StringUtils.hasText(imageGenProperties.getBaseUrl())) {
                    throw new ConfigurationException("Base URL is required for OpenAI provider");
                }
                break;
            case SPRING_AI:
                if (!StringUtils.hasText(imageGenProperties.getSpringAiBaseUrl())) {
                    throw new ConfigurationException("Spring AI base URL is required for Spring AI provider");
                }
                break;
            default:
                // No validation needed for other providers or they have their own validation
                break;
        }

        // Validate image size format (should be in format WIDTHxHEIGHT)
        String imageSize = imageGenProperties.getImageSize();
        if (!imageSize.matches("\\d+x\\d+")) {
            throw new ConfigurationException("Invalid image size format: " + imageSize + ". Expected format: WIDTHxHEIGHT (e.g., 1024x1024)");
        }

        // Validate retry parameters
        if (imageGenProperties.getMaxRetries() < 0) {
            throw new ConfigurationException("Max retries cannot be negative: " + imageGenProperties.getMaxRetries());
        }
        if (imageGenProperties.getRetryDelay() < 0) {
            throw new ConfigurationException("Retry delay cannot be negative: " + imageGenProperties.getRetryDelay());
        }

        // Validate rate limiting parameters
        if (imageGenProperties.getRequestsPerMinute() <= 0) {
            throw new ConfigurationException("Requests per minute must be positive: " + imageGenProperties.getRequestsPerMinute());
        }
        if (imageGenProperties.getBurstCapacity() <= 0) {
            throw new ConfigurationException("Burst capacity must be positive: " + imageGenProperties.getBurstCapacity());
        }

        logger.debug("Image generation properties validated successfully");
    }

    /**
     * Validates backup properties.
     * Throws a ConfigurationException if any required property is missing or invalid.
     */
    private void validateBackupProperties() {
        // Validate backup directory
        String backupDir = backupProperties.getBackupDir();
        if (!StringUtils.hasText(backupDir)) {
            throw new ConfigurationException("Backup directory cannot be empty");
        }

        // Check if backup directory exists or can be created
        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
                logger.info("Created backup directory: {}", backupPath);
            } else if (!Files.isDirectory(backupPath)) {
                throw new ConfigurationException("Backup path exists but is not a directory: " + backupDir);
            }
        } catch (Exception e) {
            throw new ConfigurationException("Failed to access or create backup directory: " + backupDir, e);
        }

        // Validate max backups
        if (backupProperties.getMaxBackups() < 0) {
            throw new ConfigurationException("Max backups cannot be negative: " + backupProperties.getMaxBackups());
        }

        logger.debug("Backup properties validated successfully");
    }
}
