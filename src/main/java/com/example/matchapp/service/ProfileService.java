package com.example.matchapp.service;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.model.Profile;
import com.example.matchapp.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.example.matchapp.util.LoggingUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public ProfileService(
            ImageGenerationService imageGenerationService, 
            ProfileRepository profileRepository,
            ImageBackupService imageBackupService,
            BackupProperties backupProperties) {
        this.imageGenerationService = imageGenerationService;
        this.profileRepository = profileRepository;
        this.imageBackupService = imageBackupService;
        this.backupProperties = backupProperties;
    }

    /**
     * Get all profiles.
     *
     * @return a list of all profiles
     */
    public List<Profile> getAllProfiles() {
        logger.info("Retrieving all profiles");
        return profileRepository.findAll();
    }

    /**
     * Get a profile by ID.
     *
     * @param id the profile ID
     * @return an Optional containing the profile if found, or empty if not found
     */
    public Optional<Profile> getProfileById(String id) {
        logger.info("Retrieving profile with ID: {}", id);
        return profileRepository.findById(id);
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

        // Create a new profile with the generated ID and imageGenerated set to false
        Profile newProfile = new Profile(
            id,
            profile.firstName(),
            profile.lastName(),
            profile.age(),
            profile.ethnicity(),
            profile.gender(),
            profile.bio(),
            profile.imageUrl(),
            profile.myersBriggsPersonalityType(),
            false
        );

        logger.info("Creating new profile with ID: {}", newProfile.id());
        return profileRepository.save(newProfile);
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
            .map(existingProfile -> {
                // Create a new profile with the updated fields but keep the same ID
                Profile updatedProfile = new Profile(
                    id,
                    profile.firstName(),
                    profile.lastName(),
                    profile.age(),
                    profile.ethnicity(),
                    profile.gender(),
                    profile.bio(),
                    profile.imageUrl(),
                    profile.myersBriggsPersonalityType(),
                    existingProfile.imageGenerated()
                );

                return profileRepository.save(updatedProfile);
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
                    LoggingUtils.setProfileId(profile.id());

                    // Create directories if they don't exist
                    Files.createDirectories(imagesDir);

                    // Generate the image
                    byte[] image = imageGenerationService.generateImage(profile);

                    // Write the image to a file
                    Files.write(imagesDir.resolve(profile.imageUrl()), image);

                    // Update the profile to mark the image as generated
                    Profile updatedProfile = new Profile(
                        profile.id(),
                        profile.firstName(),
                        profile.lastName(),
                        profile.age(),
                        profile.ethnicity(),
                        profile.gender(),
                        profile.bio(),
                        profile.imageUrl(),
                        profile.myersBriggsPersonalityType(),
                        true
                    );

                    return profileRepository.save(updatedProfile);
                } catch (IOException e) {
                    logger.error("Error generating image for profile: {}", profile.id(), e);
                    throw new RuntimeException("Failed to generate image for profile: " + profile.id(), e);
                } finally {
                    LoggingUtils.clearMDC();
                }
            });
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

        List<Profile> profiles = profileRepository.findAll();

        for (Profile profile : profiles) {
            generateImageForProfile(profile.id(), imagesDir);
        }

        // Get the updated profiles with image generation status
        List<Profile> updatedProfiles = profileRepository.findAll();

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
