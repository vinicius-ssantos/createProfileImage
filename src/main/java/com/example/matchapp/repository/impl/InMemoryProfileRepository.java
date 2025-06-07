package com.example.matchapp.repository.impl;

import com.example.matchapp.mapper.ProfileMapper;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ProfileRepository.
 * Stores profiles in a ConcurrentHashMap for thread safety.
 * Loads initial data from profile.json on startup.
 */
@Repository
public class InMemoryProfileRepository implements ProfileRepository {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryProfileRepository.class);
    private final Map<String, ProfileEntity> profiles = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initialize the repository with data from profile.json.
     */
    @PostConstruct
    public void init() {
        try {
            var resource = new ClassPathResource("profile.json");
            Profile[] initialProfiles = objectMapper.readValue(resource.getInputStream(), Profile[].class);

            Arrays.stream(initialProfiles).forEach(profile -> {
                ProfileEntity entity = ProfileMapper.toProfileEntity(profile);
                profiles.put(entity.getId(), entity);
            });

            logger.info("Loaded {} profiles from profile.json", profiles.size());
        } catch (IOException e) {
            logger.error("Failed to load profiles from profile.json", e);
            throw new RuntimeException("Failed to initialize profile repository", e);
        }
    }

    /**
     * Find all profiles stored in memory.
     *
     * @return a list of all profiles in the repository
     */
    @Override
    public List<ProfileEntity> findAll() {
        return new ArrayList<>(profiles.values());
    }

    /**
     * Find a profile by its ID in the in-memory store.
     *
     * @param id the profile ID
     * @return an Optional containing the profile if found, or empty if not found
     */
    @Override
    public Optional<ProfileEntity> findById(String id) {
        return Optional.ofNullable(profiles.get(id));
    }

    /**
     * Save a profile to the in-memory store.
     * If the profile has an ID that already exists, it will be updated.
     * If the profile has no ID or the ID doesn't exist, it will be created.
     *
     * @param profile the profile to save
     * @return the saved profile
     */
    @Override
    public ProfileEntity save(ProfileEntity profile) {
        profiles.put(profile.getId(), profile);
        return profile;
    }

    /**
     * Delete a profile by its ID from the in-memory store.
     *
     * @param id the profile ID
     * @return true if the profile was deleted, false if it wasn't found
     */
    @Override
    public boolean deleteById(String id) {
        return profiles.remove(id) != null;
    }
}
