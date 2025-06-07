package com.example.matchapp.repository.impl;

import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of ProfileRepository.
 * Uses Spring Data JPA to provide database persistence for profiles.
 */
@Repository
@Primary
public class JpaProfileRepository implements ProfileRepository {

    private static final Logger logger = LoggerFactory.getLogger(JpaProfileRepository.class);

    private final ProfileJpaRepository jpaRepository;

    /**
     * Creates a new JPA profile repository with the specified JPA repository.
     *
     * @param jpaRepository the Spring Data JPA repository to use
     */
    public JpaProfileRepository(ProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Find all profiles in the database.
     *
     * @return a list of all profiles
     */
    @Override
    public List<ProfileEntity> findAll() {
        logger.debug("Finding all profiles from database");
        return jpaRepository.findAll();
    }

    /**
     * Find all profiles in the database with pagination.
     *
     * @param pageable pagination information including page number, page size, and sorting
     * @return a page of profiles
     */
    @Override
    public Page<ProfileEntity> findAll(Pageable pageable) {
        logger.debug("Finding profiles from database with pagination: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        return jpaRepository.findAll(pageable);
    }

    /**
     * Find a profile by its ID in the database.
     *
     * @param id the profile ID
     * @return an Optional containing the profile if found, or empty if not found
     */
    @Override
    public Optional<ProfileEntity> findById(String id) {
        logger.debug("Finding profile with ID {} from database", id);
        return jpaRepository.findById(id);
    }

    /**
     * Save a profile to the database.
     * If the profile has an ID that already exists, it will be updated.
     * If the profile has no ID or the ID doesn't exist, it will be created.
     *
     * @param profile the profile to save
     * @return the saved profile
     */
    @Override
    public ProfileEntity save(ProfileEntity profile) {
        logger.debug("Saving profile with ID {} to database", profile.getId());
        return jpaRepository.save(profile);
    }

    /**
     * Delete a profile by its ID from the database.
     *
     * @param id the profile ID
     * @return true if the profile was deleted, false if it wasn't found
     */
    @Override
    public boolean deleteById(String id) {
        logger.debug("Deleting profile with ID {} from database", id);
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
