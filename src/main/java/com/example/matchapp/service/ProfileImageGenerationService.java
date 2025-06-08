package com.example.matchapp.service;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.exception.ConfigurationException;
import com.example.matchapp.exception.FileOperationException;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
import com.example.matchapp.util.LoggingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Service responsible for generating images for profiles.
 * This service follows the Single Responsibility Principle by focusing only on
 * image generation operations.
 */
@Service
public class ProfileImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileImageGenerationService.class);

    private final ImageGenerationService imageGenerationService;
    private final ProfileRepository profileRepository;
    private final ImageBackupService imageBackupService;
    private final BackupProperties backupProperties;
    private final ImageCacheService imageCacheService;

    public ProfileImageGenerationService(
            ImageGenerationService imageGenerationService,
            ProfileRepository profileRepository,
            ImageBackupService imageBackupService,
            BackupProperties backupProperties,
            ImageCacheService imageCacheService) {
        // Validate all parameters before assigning to fields
        validateConstructorParameters(imageGenerationService, profileRepository,
                imageBackupService, backupProperties, imageCacheService);

        // These are interfaces/services, not mutable objects that need defensive copying
        this.imageGenerationService = imageGenerationService;
        this.profileRepository = profileRepository;
        this.imageBackupService = imageBackupService;

        // Create defensive copy to prevent external modification
        this.backupProperties = copyBackupProperties(backupProperties);

        // This is an interface/service, not a mutable object that needs defensive copying
        this.imageCacheService = imageCacheService;
    }

    /**
     * Validates that all constructor parameters are non-null.
     * Centralizing validation helps avoid partial initialization issues.
     */
    private void validateConstructorParameters(
            ImageGenerationService imageGenerationService,
            ProfileRepository profileRepository,
            ImageBackupService imageBackupService,
            BackupProperties backupProperties,
            ImageCacheService imageCacheService) {
        if (imageGenerationService == null) {
            throw new ConfigurationException("ImageGenerationService cannot be null", "imageGenerationService", "null");
        }
        if (profileRepository == null) {
            throw new ConfigurationException("ProfileRepository cannot be null", "profileRepository", "null");
        }
        if (imageBackupService == null) {
            throw new ConfigurationException("ImageBackupService cannot be null", "imageBackupService", "null");
        }
        if (backupProperties == null) {
            throw new ConfigurationException("BackupProperties cannot be null", "backupProperties", "null");
        }
        if (imageCacheService == null) {
            throw new ConfigurationException("ImageCacheService cannot be null", "imageCacheService", "null");
        }
    }

    /**
     * Creates a defensive copy of BackupProperties.
     *
     * @param original the original BackupProperties object
     * @return a new BackupProperties object with the same properties
     */
    private BackupProperties copyBackupProperties(BackupProperties original) {
        BackupProperties copy = new BackupProperties();
        copy.setBackupDir(original.getBackupDir());
        copy.setAutoBackup(original.isAutoBackup());
        copy.setMaxBackups(original.getMaxBackups());
        copy.setDefaultOverwrite(original.isDefaultOverwrite());
        return copy;
    }

    /**
     * Generate an image for a profile entity.
     *
     * @param entity the profile entity
     * @param imagesDir the directory to save the image to
     * @return the generated image bytes
     * @throws FileOperationException if there's an error writing the image file
     */
    public byte[] generateImageForEntity(ProfileEntity entity, Path imagesDir) {
        try {
            LoggingUtils.setProfileId(entity.getId());
            logger.info("Generating image for profile: {}", entity.getId());

            // Create directories if they don't exist
            try {
                Files.createDirectories(imagesDir);
            } catch (IOException e) {
                logger.error("Failed to create directories: {}", imagesDir, e);
                throw new FileOperationException("Failed to create directories: " + imagesDir, e, 
                    imagesDir, "createDirectories", false);
            }

            byte[] image;

            // Check if image exists in cache
            try {
                if (imageCacheService.hasImageInCache(entity, imagesDir)) {
                    logger.info("Using cached image for profile: {}", entity.getId());
                    Optional<byte[]> cachedImage = imageCacheService.getImageFromCache(entity, imagesDir);
                    if (cachedImage.isPresent()) {
                        image = cachedImage.get();
                    } else {
                        // This should not happen if hasImageInCache returned true, but just in case
                        logger.warn("Cache inconsistency detected for profile: {}", entity.getId());
                        image = generateAndCacheImage(entity, imagesDir);
                    }
                } else {
                    // Generate new image if not in cache
                    image = generateAndCacheImage(entity, imagesDir);
                }
            } catch (IOException e) {
                logger.error("Failed to access image cache for profile: {}", entity.getId(), e);
                throw new FileOperationException("Failed to access image cache for profile: " + entity.getId(), e, 
                    imagesDir, "getImageFromCache", true);
            }

            return image;
        } finally {
            LoggingUtils.clearMDC();
        }
    }

    /**
     * Generates an image for a profile and caches it.
     *
     * @param entity the profile entity to generate an image for
     * @param imagesDir the directory to save the image to
     * @return the generated image bytes
     * @throws FileOperationException if there's an error generating or caching the image
     */
    private byte[] generateAndCacheImage(ProfileEntity entity, Path imagesDir) {
        logger.info("Generating new image for profile: {}", entity.getId());
        byte[] image = imageGenerationService.generateImage(entity);

        // Cache the image
        try {
            imageCacheService.putImageInCache(entity, image, imagesDir);
        } catch (IOException e) {
            logger.error("Failed to cache image for profile: {}", entity.getId(), e);
            throw new FileOperationException("Failed to cache image for profile: " + entity.getId(), e, 
                imagesDir, "putImageInCache", true);
        }

        return image;
    }

    /**
     * Create a backup of the images directory.
     *
     * @param imagesDir the directory to backup
     * @return the number of files backed up
     * @throws FileOperationException if there's an error creating the backup
     */
    public int createBackup(Path imagesDir) {
        if (backupProperties.isAutoBackup()) {
            logger.info("Creating backup of images directory: {}", imagesDir);
            try {
                return imageBackupService.createBackup(imagesDir);
            } catch (IOException e) {
                logger.error("Failed to create backup of images directory: {}", imagesDir, e);
                throw new FileOperationException("Failed to create backup of images directory: " + imagesDir, e, 
                    imagesDir, "createBackup", true);
            }
        } else {
            logger.info("Auto-backup is disabled. Skipping backup creation.");
            return 0;
        }
    }
}
