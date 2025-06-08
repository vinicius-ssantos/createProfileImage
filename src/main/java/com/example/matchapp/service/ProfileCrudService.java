package com.example.matchapp.service;

import com.example.matchapp.exception.ConfigurationException;
import com.example.matchapp.mapper.ProfileMapper;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service responsible for CRUD operations on profiles.
 * This service follows the Single Responsibility Principle by focusing only on
 * profile data management operations.
 */
@Service
@Transactional(readOnly = true)
public class ProfileCrudService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileCrudService.class);

    private final ProfileRepository profileRepository;

    public ProfileCrudService(ProfileRepository profileRepository) {
        if (profileRepository == null) {
            throw new ConfigurationException("ProfileRepository cannot be null", "profileRepository", "null");
        }
        this.profileRepository = profileRepository;
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
     * Update the image generation status for a profile.
     *
     * @param id the profile ID
     * @param imageGenerated whether the image has been generated
     * @return an Optional containing the updated profile if found, or empty if not found
     */
    @Transactional
    public Optional<Profile> updateImageGenerationStatus(String id, boolean imageGenerated) {
        logger.info("Updating image generation status for profile with ID: {}", id);

        return profileRepository.findById(id)
            .map(entity -> {
                entity.setImageGenerated(imageGenerated);
                ProfileEntity savedEntity = profileRepository.save(entity);
                return ProfileMapper.toProfile(savedEntity);
            });
    }
}
