package com.example.matchapp.service;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.mapper.ProfileMapper;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.example.matchapp.util.LoggingUtils;

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
        this.imageGenerationService = imageGenerationService;
        this.profileRepository = profileRepository;
        this.imageBackupService = imageBackupService;
        this.backupProperties = backupProperties;
        this.imageCacheService = imageCacheService;
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
    public Optional<Profile> generateImageForProfile(String id, Path imagesDir) throws IOException {
        logger.info("Generating image for profile with ID: {}", id);

        return profileRepository.findById(id)
            .map(profile -> {
                try {
                    LoggingUtils.setProfileId(profile.getId());

                    // Create directories if they don't exist
                    Files.createDirectories(imagesDir);

                    byte[] image;

                    // Check if image exists in cache
                    if (imageCacheService.hasImageInCache(profile, imagesDir)) {
                        logger.info("Using cached image for profile: {}", profile.getId());
                        Optional<byte[]> cachedImage = imageCacheService.getImageFromCache(profile, imagesDir);
                        if (cachedImage.isPresent()) {
                            image = cachedImage.get();
                        } else {
                            // This should not happen if hasImageInCache returned true, but just in case
                            logger.warn("Cache inconsistency detected for profile: {}", profile.getId());
                            image = generateAndCacheImage(profile, imagesDir);
                        }
                    } else {
                        // Generate new image if not in cache
                        image = generateAndCacheImage(profile, imagesDir);
                    }

                    // Update the profile to mark the image as generated
                    profile.setImageGenerated(true);
                    ProfileEntity updatedEntity = profileRepository.save(profile);
                    return ProfileMapper.toProfile(updatedEntity);
                } catch (IOException e) {
                    logger.error("Error generating image for profile: {}", profile.getId(), e);
                    throw new RuntimeException("Failed to generate image for profile: " + profile.getId(), e);
                } finally {
                    LoggingUtils.clearMDC();
                }
            });
    }

    /**
     * Generates an image for a profile and caches it.
     *
     * @param profile the profile to generate an image for
     * @param imagesDir the directory to save the image to
     * @return the generated image bytes
     * @throws IOException if there's an error generating or caching the image
     */
    private byte[] generateAndCacheImage(ProfileEntity profile, Path imagesDir) throws IOException {
        logger.info("Generating new image for profile: {}", profile.getId());

        byte[] image = imageGenerationService.generateImage(profile);

        // Cache the image
        imageCacheService.putImageInCache(profile, image, imagesDir);

        return image;
    }

    /**
     * Generate images for all profiles.
     *
     * @param imagesDir the directory to save the images to
     * @return a list of profiles with generated images
     * @throws IOException if there's an error writing the image files
     */
    public List<Profile> generateImages(Path imagesDir) throws IOException {
        logger.info("Generating images for all profiles");

        // Create directories if they don't exist
        Files.createDirectories(imagesDir);

        List<ProfileEntity> profileEntities = profileRepository.findAll();
        List<Profile> profiles = profileEntities.stream()
                .map(ProfileMapper::toProfile)
                .collect(Collectors.toList());

        for (Profile profile : profiles) {
            generateImageForProfile(profile.id(), imagesDir);
        }

        // Get the updated profiles with image generation status
        List<ProfileEntity> updatedProfileEntities = profileRepository.findAll();
        List<Profile> updatedProfiles = updatedProfileEntities.stream()
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
