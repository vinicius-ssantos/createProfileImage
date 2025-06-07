package com.example.matchapp.repository;

import com.example.matchapp.model.ProfileEntity;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Profile entities.
 * Provides methods for CRUD operations on profiles.
 */
public interface ProfileRepository {

    /**
     * Find all profiles.
     *
     * @return a list of all profiles
     */
    List<ProfileEntity> findAll();

    /**
     * Find a profile by its ID.
     *
     * @param id the profile ID
     * @return an Optional containing the profile if found, or empty if not found
     */
    Optional<ProfileEntity> findById(String id);

    /**
     * Save a profile.
     * If the profile has an ID that already exists, it will be updated.
     * If the profile has no ID or the ID doesn't exist, it will be created.
     *
     * @param profile the profile to save
     * @return the saved profile
     */
    ProfileEntity save(ProfileEntity profile);

    /**
     * Delete a profile by its ID.
     *
     * @param id the profile ID
     * @return true if the profile was deleted, false if it wasn't found
     */
    boolean deleteById(String id);
}
