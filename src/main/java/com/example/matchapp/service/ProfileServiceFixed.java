package com.example.matchapp.service;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.exception.ConfigurationException;
import com.example.matchapp.exception.FileOperationException;
import com.example.matchapp.exception.ServiceException;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
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

/**
 * Service for managing profiles and generating profile images.
 * This service delegates to specialized services for specific responsibilities,
 * following the Single Responsibility Principle.
 * 
 * The class maintains the same public API for backward compatibility with existing code.
 */
@Service
@Transactional(readOnly = true)
class ProfileServiceOld {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServiceOld.class);

    private final ProfileCrudService profileCrudService;
    private final ProfileImageGenerationService profileImageGenerationService;
    private final ProfileExportService profileExportService;
    private final ProfileRepository profileRepository;

    public ProfileServiceOld(
            ProfileCrudService profileCrudService,
            ProfileImageGenerationService profileImageGenerationService,
            ProfileExportService profileExportService,
            ProfileRepository profileRepository) {
        if (profileCrudService == null) {
            throw new ConfigurationException("ProfileCrudService cannot be null", "profileCrudService", "null");
        }
        if (profileImageGenerationService == null) {
            throw new ConfigurationException("ProfileImageGenerationService cannot be null", "profileImageGenerationService", "null");
        }
        if (profileExportService == null) {
            throw new ConfigurationException("ProfileExportService cannot be null", "profileExportService", "null");
        }
        if (profileRepository == null) {
            throw new ConfigurationException("ProfileRepository cannot be null", "profileRepository", "null");
        }

        this.profileCrudService = profileCrudService;
        this.profileImageGenerationService = profileImageGenerationService;
        this.profileExportService = profileExportService;
        this.profileRepository = profileRepository;
    }

    /**
     * Get all profiles.
     *
     * @return a list of all profiles
     */
    public List<Profile> getAllProfiles() {
        logger.info("Delegating to ProfileCrudService.getAllProfiles()");
        return profileCrudService.getAllProfiles();
    }

    /**
     * Get all profiles with pagination.
     *
     * @param pageable pagination information including page number, page size, and sorting
     * @return a page of profiles
     */
    public Page<Profile> getAllProfiles(Pageable pageable) {
        logger.info("Delegating to ProfileCrudService.getAllProfiles(Pageable)");
        return profileCrudService.getAllProfiles(pageable);
    }

    /**
     * Get a profile by ID.
     *
     * @param id the profile ID
     * @return an Optional containing the profile if found, or empty if not found
     */
    public Optional<Profile> getProfileById(String id) {
        logger.info("Delegating to ProfileCrudService.getProfileById()");
        return profileCrudService.getProfileById(id);
    }

    /**
     * Create a new profile.
     *
     * @param profile the profile to create
     * @return the created profile
     */
    @Transactional
    public Profile createProfile(Profile profile) {
        logger.info("Delegating to ProfileCrudService.createProfile()");
        return profileCrudService.createProfile(profile);
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
        logger.info("Delegating to ProfileCrudService.updateProfile()");
        return profileCrudService.updateProfile(id, profile);
    }

    /**
     * Delete a profile by ID.
     *
     * @param id the profile ID
     * @return true if the profile was deleted, false if it wasn't found
     */
    @Transactional
    public boolean deleteProfile(String id) {
        logger.info("Delegating to ProfileCrudService.deleteProfile()");
        return profileCrudService.deleteProfile(id);
    }

    /**
     * Generate an image for a profile.
     *
     * @param id the profile ID
     * @param imagesDir the directory to save the image to
     * @return an Optional containing the updated profile if found, or empty if not found
     * @throws ServiceException if there's an error writing the image file
     */
    @Transactional
    public Optional<Profile> generateImageForProfile(String id, Path imagesDir) {
        logger.info("Generating image for profile with ID: {}", id);

        // Create directories if they don't exist
        try {
            Files.createDirectories(imagesDir);
        } catch (IOException e) {
            logger.error("Failed to create directories: {}", imagesDir, e);
            throw new ServiceException("Failed to create directories: " + imagesDir, e, 
                    "ProfileServiceFixed", "generateImageForProfile", true);
        }

        return profileRepository.findById(id)
            .map(entity -> {
                // Generate the image
                byte[] image = profileImageGenerationService.generateImageForEntity(entity, imagesDir);

                // Update the profile to mark the image as generated
                return profileCrudService.updateImageGenerationStatus(id, true).orElse(null);
            });
    }

    /**
     * Generate images for all profiles.
     *
     * @param imagesDir the directory to save the images to
     * @return a list of profiles with generated images
     * @throws ServiceException if there's an error writing the image files
     */
    @Transactional
    public List<Profile> generateImages(Path imagesDir) {
        logger.info("Generating images for all profiles");

        // Create directories if they don't exist
        try {
            Files.createDirectories(imagesDir);
        } catch (IOException e) {
            logger.error("Failed to create directories: {}", imagesDir, e);
            throw new ServiceException("Failed to create directories: " + imagesDir, e, 
                    "ProfileServiceFixed", "generateImages", true);
        }

        List<ProfileEntity> entities = profileRepository.findAll();

        // Use parallel stream to process profiles concurrently
        entities.parallelStream().forEach(entity -> {
            try {
                generateImageForProfile(entity.getId(), imagesDir);
            } catch (ServiceException e) {
                logger.error("Error generating image for profile: {}", entity.getId(), e);
                // Continue processing other profiles even if one fails
            }
        });

        // Get the updated profiles with image generation status
        List<Profile> updatedProfiles = profileCrudService.getAllProfiles();

        // Export the profiles to a JSON file
        profileExportService.exportProfilesWithImages(updatedProfiles, imagesDir);

        // Create a backup if needed
        try {
            profileImageGenerationService.createBackup(imagesDir);
        } catch (FileOperationException e) {
            logger.error("Failed to create backup of images", e);
            // Don't throw the exception as this is a secondary operation
        }

        return updatedProfiles;
    }
}
