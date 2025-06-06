package com.example.matchapp.controller;

import com.example.matchapp.dto.CreateProfileRequest;
import com.example.matchapp.dto.ProfileResponse;
import com.example.matchapp.dto.UpdateProfileRequest;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.ProfileService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for profile management.
 * Provides endpoints for CRUD operations on profiles.
 */
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Get all profiles.
     *
     * @return a list of all profiles
     */
    @GetMapping
    public ResponseEntity<List<ProfileResponse>> getAllProfiles() {
        logger.info("GET request to fetch all profiles");
        List<ProfileResponse> responses = profileService.getAllProfiles().stream()
                .map(ProfileResponse::fromProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a profile by ID.
     *
     * @param id the profile ID
     * @return the profile if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable String id) {
        logger.info("GET request to fetch profile with ID: {}", id);
        return profileService.getProfileById(id)
                .map(ProfileResponse::fromProfile)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found with ID: " + id));
    }

    /**
     * Create a new profile.
     *
     * @param request the profile creation request
     * @return the created profile
     */
    @PostMapping
    public ResponseEntity<ProfileResponse> createProfile(@Valid @RequestBody CreateProfileRequest request) {
        logger.info("POST request to create a new profile");

        // Convert DTO to domain model
        Profile profile = new Profile(
                null, // ID will be generated
                request.firstName(),
                request.lastName(),
                request.age(),
                request.ethnicity(),
                request.gender(),
                request.bio(),
                request.imageUrl() != null ? request.imageUrl() : UUID.randomUUID().toString() + ".jpg",
                request.myersBriggsPersonalityType(),
                false
        );

        Profile createdProfile = profileService.createProfile(profile);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfileResponse.fromProfile(createdProfile));
    }

    /**
     * Update an existing profile.
     *
     * @param id the profile ID
     * @param request the profile update request
     * @return the updated profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProfileResponse> updateProfile(@PathVariable String id, @Valid @RequestBody UpdateProfileRequest request) {
        logger.info("PUT request to update profile with ID: {}", id);

        return profileService.getProfileById(id)
                .map(existingProfile -> {
                    // Create a new profile with updated fields, keeping existing values for null fields
                    Profile updatedProfile = new Profile(
                            id,
                            request.firstName() != null ? request.firstName() : existingProfile.firstName(),
                            request.lastName() != null ? request.lastName() : existingProfile.lastName(),
                            request.age() != null ? request.age() : existingProfile.age(),
                            request.ethnicity() != null ? request.ethnicity() : existingProfile.ethnicity(),
                            request.gender() != null ? request.gender() : existingProfile.gender(),
                            request.bio() != null ? request.bio() : existingProfile.bio(),
                            request.imageUrl() != null ? request.imageUrl() : existingProfile.imageUrl(),
                            request.myersBriggsPersonalityType() != null ? request.myersBriggsPersonalityType() : existingProfile.myersBriggsPersonalityType(),
                            existingProfile.imageGenerated()
                    );

                    return profileService.updateProfile(id, updatedProfile)
                            .map(ProfileResponse::fromProfile)
                            .map(ResponseEntity::ok)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update profile"));
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found with ID: " + id));
    }

    /**
     * Delete a profile by ID.
     *
     * @param id the profile ID
     * @return no content if successful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        logger.info("DELETE request to delete profile with ID: {}", id);
        boolean deleted = profileService.deleteProfile(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found with ID: " + id);
        }
    }

    /**
     * Generate an image for a profile.
     *
     * @param id the profile ID
     * @return the updated profile with image generated
     */
    @PostMapping("/{id}/generate-image")
    public ResponseEntity<ProfileResponse> generateImageForProfile(@PathVariable String id) {
        logger.info("POST request to generate image for profile with ID: {}", id);
        try {
            return profileService.generateImageForProfile(id, Paths.get("src/main/resources/static/images"))
                    .map(ProfileResponse::fromProfile)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found with ID: " + id));
        } catch (IOException e) {
            logger.error("Error generating image for profile: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating image: " + e.getMessage());
        }
    }

    /**
     * Generate images for all profiles.
     *
     * @return a list of profiles with generated images
     */
    @PostMapping("/generate-images")
    public ResponseEntity<List<ProfileResponse>> generateImagesForAllProfiles() {
        logger.info("POST request to generate images for all profiles");
        try {
            List<ProfileResponse> responses = profileService.generateImages(Paths.get("src/main/resources/static/images"))
                    .stream()
                    .map(ProfileResponse::fromProfile)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        } catch (IOException e) {
            logger.error("Error generating images for all profiles", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating images: " + e.getMessage());
        }
    }

}
