package com.example.matchapp.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Custom health indicator that checks the available disk space for image storage.
 * This indicator will be included in the health endpoint response.
 */
@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(DiskSpaceHealthIndicator.class);

    // Minimum free space required (100 MB in bytes)
    private static final long MIN_FREE_SPACE_BYTES = 100 * 1024 * 1024;

    // Warning threshold (500 MB in bytes)
    private static final long WARNING_THRESHOLD_BYTES = 500 * 1024 * 1024;

    @Value("${spring.application.image-storage-path:src/main/resources/static/images}")
    private String imageStoragePath;

    @Override
    public Health health() {
        try {
            Path storagePath = Paths.get(imageStoragePath);

            // Create directory if it doesn't exist
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
                logger.info("Created image storage directory: {}", storagePath);
            }

            File storageDir = storagePath.toFile();
            long totalSpace = storageDir.getTotalSpace();
            long freeSpace = storageDir.getFreeSpace();
            long usableSpace = storageDir.getUsableSpace();

            // Calculate percentage of free space
            double freeSpacePercent = (double) freeSpace / totalSpace * 100;

            // Common details for all health statuses
            String path = storageDir.getAbsolutePath();
            String totalFormatted = formatBytes(totalSpace);
            String freeFormatted = formatBytes(freeSpace);
            String usableFormatted = formatBytes(usableSpace);
            String percentFormatted = String.format("%.2f%%", freeSpacePercent);

            // Check if free space is below minimum threshold
            if (freeSpace < MIN_FREE_SPACE_BYTES) {
                logger.warn("Critical disk space: only {} available at {}", 
                           freeFormatted, path);
                return Health.down()
                        .withDetail("path", path)
                        .withDetail("total", totalFormatted)
                        .withDetail("free", freeFormatted)
                        .withDetail("usable", usableFormatted)
                        .withDetail("freePercent", percentFormatted)
                        .withDetail("error", "Insufficient disk space for image storage")
                        .withDetail("minimumRequired", formatBytes(MIN_FREE_SPACE_BYTES))
                        .build();
            } 
            // Check if free space is below warning threshold
            else if (freeSpace < WARNING_THRESHOLD_BYTES) {
                logger.warn("Low disk space: only {} available at {}", 
                           freeFormatted, path);
                return Health.status("WARNING")
                        .withDetail("path", path)
                        .withDetail("total", totalFormatted)
                        .withDetail("free", freeFormatted)
                        .withDetail("usable", usableFormatted)
                        .withDetail("freePercent", percentFormatted)
                        .withDetail("warning", "Low disk space for image storage")
                        .withDetail("warningThreshold", formatBytes(WARNING_THRESHOLD_BYTES))
                        .build();
            } 
            // Sufficient disk space
            else {
                return Health.up()
                        .withDetail("path", path)
                        .withDetail("total", totalFormatted)
                        .withDetail("free", freeFormatted)
                        .withDetail("usable", usableFormatted)
                        .withDetail("freePercent", percentFormatted)
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error checking disk space: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("path", imageStoragePath)
                    .build();
        }
    }

    /**
     * Formats bytes into a human-readable string (KB, MB, GB, etc.)
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
