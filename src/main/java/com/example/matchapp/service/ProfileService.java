package com.example.matchapp.service;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.mapper.ProfileMapper;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
import com.example.matchapp.util.LoggingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing profiles and generating profile images.
 */
@Service
@Transactional(readOnly = true)
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final ImageGenerationService imageGenerationService;
    private final ProfileRepository profileRepository;
    private final ImageBackupService imageBackupService;
    private final BackupProperties backupProperties;
    private final ImageCacheService imageCacheService;

    public ProfileService(
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
            throw new NullPointerException("ImageGenerationService cannot be null");
        }
        if (profileRepository == null) {
            throw new NullPointerException("ProfileRepository cannot be null");
        }
        if (imageBackupService == null) {
            throw new NullPointerException("ImageBackupService cannot be null");
        }
        if (backupProperties == null) {
            throw new NullPointerException("BackupProperties cannot be null");
        }
        if (imageCacheService == null) {
            throw new NullPointerException("ImageCacheService cannot be null");
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
     * Get all profiles.
     *
     * @return a list of all profiles
     */
    public List<Profile> getAllProfiles() {
        logger.info("Retrieving all profiles");
        return profileRepository.findAll().stream()
                .map(ProfileMapper::toProfile)
                .collect(Collectors.toList());
    }

    /**
     * Get all profiles with pagination.
     *
     * @param pageable pagination information including page number, page size, and sorting
     * @return a page of profiles
     */
    public Page<Profile> getAllProfiles(Pageable pageable) {
        logger.info("Retrieving profiles with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return profileRepository.findAll(pageable)
                .map(ProfileMapper::toProfile);
    }

    /**
     * Get a profile by ID.
     *
     * @param id the profile ID
     * @return an Optional containing the profile if found, or empty if not found
     */
    public Optional<Profile> getProfileById(String id) {
        logger.info("Retrieving profile with ID: {}", id);
        return profileRepository.findById(id)
                .map(ProfileMapper::toProfile);
    }

    /**
     * Create a new profile.
     *
     * @param profile the profile to create
     * @return the created profile
     */
    @Transactional
    public Profile createProfile(Profile profile) {
        // Generate a new ID if one isn't provided
        String id = (profile.id() == null || profile.id().isEmpty()) 
            ? UUID.randomUUID().toString() 
            : profile.id();

        // Convert to entity
        ProfileEntity entity = ProfileMapper.toProfileEntity(profile);

        // Set the ID and ensure imageGenerated is false
        entity.setId(id);
        entity.setImageGenerated(false);

        logger.info("Creating new profile with ID: {}", entity.getId());
        ProfileEntity savedEntity = profileRepository.save(entity);
        return ProfileMapper.toProfile(savedEntity);
    }

    /**
     * Update an existing profile.
     *
     * @param id the profile ID
     * @param profile the updated profile data
     * @return an Optional containing the updated profile if found, or empty if not found
     */
    @Transactional
    public Optional<Profile> updateProfile(String id, Profile profile) {
        logger.info("Updating profile with ID: {}", id);

        return profileRepository.findById(id)
            .map(existingEntity -> {
                // Convert profile to entity and update fields
                ProfileEntity updatedEntity = ProfileMapper.toProfileEntity(profile);
                updatedEntity.setId(id);
                updatedEntity.setImageGenerated(existingEntity.isImageGenerated());

                // Save the updated entity
                ProfileEntity savedEntity = profileRepository.save(updatedEntity);
                return ProfileMapper.toProfile(savedEntity);
            });
    }

    /**
     * Delete a profile by ID.
     *
     * @param id the profile ID
     * @return true if the profile was deleted, false if it wasn't found
     */
    @Transactional
    public boolean deleteProfile(String id) {
        logger.info("Deleting profile with ID: {}", id);
        return profileRepository.deleteById(id);
    }

    /**
     * Generate an image for a profile.
     *
     * @param id the profile ID
     * @param imagesDir the directory to save the image to
     * @return an Optional containing the updated profile if found, or empty if not found
     * @throws IOException if there's an error writing the image file
     */
    @Transactional
    public Optional<Profile> generateImageForProfile(String id, Path imagesDir) throws IOException {
        logger.info("Generating image for profile with ID: {}", id);

        return profileRepository.findById(id)
            .map(entity -> {
                try {
                    LoggingUtils.setProfileId(entity.getId());

                    // Create directories if they don't exist
                    Files.createDirectories(imagesDir);

                    byte[] image;

                    // Check if image exists in cache
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

                    // Update the profile to mark the image as generated
                    entity.setImageGenerated(true);
                    ProfileEntity updatedEntity = profileRepository.save(entity);
                    return ProfileMapper.toProfile(updatedEntity);
                } catch (IOException e) {
                    logger.error("Error generating image for profile: {}", entity.getId(), e);
                    throw new RuntimeException("Failed to generate image for profile: " + entity.getId(), e);
                } finally {
                    LoggingUtils.clearMDC();
                }
            });
    }

    /**
     * Generates an image for a profile and caches it.
     *
     * @param entity the profile entity to generate an image for
     * @param imagesDir the directory to save the image to
     * @return the generated image bytes
     * @throws IOException if there's an error generating or caching the image
     */
    private byte[] generateAndCacheImage(ProfileEntity entity, Path imagesDir) throws IOException {
        logger.info("Generating new image for profile: {}", entity.getId());
        byte[] image = imageGenerationService.generateImage(entity);

        // Cache the image
        imageCacheService.putImageInCache(entity, image, imagesDir);

        return image;
    }

    /**
     * Generate images for all profiles.
     *
     * @param imagesDir the directory to save the images to
     * @return a list of profiles with generated images
     * @throws IOException if there's an error writing the image files
     */
    @Transactional
    public List<Profile> generateImages(Path imagesDir) throws IOException {
        logger.info("Generating images for all profiles");

        // Create directories if they don't exist
        Files.createDirectories(imagesDir);

        List<ProfileEntity> entities = profileRepository.findAll();

        // Use parallel stream to process profiles concurrently
        entities.parallelStream().forEach(entity -> {
            try {
                generateImageForProfile(entity.getId(), imagesDir);
            } catch (IOException e) {
                logger.error("Error generating image for profile: {}", entity.getId(), e);
                // Continue processing other profiles even if one fails
            }
        });

        // Get the updated profiles with image generation status
        List<Profile> updatedProfiles = profileRepository.findAll().stream()
                .map(ProfileMapper::toProfile)
                .collect(Collectors.toList());

        // Write the updated profiles to a JSON file
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(imagesDir.resolve("profiles_with_images.json").toFile(), updatedProfiles);

        // Perform automatic backup if configured
        if (backupProperties.isAutoBackup()) {
            try {
                logger.info("Auto-backup is enabled. Creating backup of generated images.");
                int backedUpFiles = imageBackupService.createBackup(imagesDir);
                logger.info("Auto-backup completed. {} files backed up.", backedUpFiles);
            } catch (IOException e) {
                logger.error("Failed to create automatic backup of images", e);
                // Don't throw the exception as this is a secondary operation
            }
        }

        return updatedProfiles;
    }
}