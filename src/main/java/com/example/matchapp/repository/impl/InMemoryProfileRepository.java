package com.example.matchapp.repository.impl;

import com.example.matchapp.model.Profile;
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
    private final Map<String, Profile> profiles = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initialize the repository with data from profile.json.
     */
    @PostConstruct
    public void init() {
        try {
            var resource = new ClassPathResource("profile.json");
            Profile[] initialProfiles = objectMapper.readValue(resource.getInputStream(), Profile[].class);
            
            Arrays.stream(initialProfiles).forEach(profile -> 
                profiles.put(profile.id(), profile)
            );
            
            logger.info("Loaded {} profiles from profile.json", profiles.size());
        } catch (IOException e) {
            logger.error("Failed to load profiles from profile.json", e);
            throw new RuntimeException("Failed to initialize profile repository", e);
        }
    }

    @Override
    public List<Profile> findAll() {
        return new ArrayList<>(profiles.values());
    }

    @Override
    public Optional<Profile> findById(String id) {
        return Optional.ofNullable(profiles.get(id));
    }

    @Override
    public Profile save(Profile profile) {
        profiles.put(profile.id(), profile);
        return profile;
    }

    @Override
    public boolean deleteById(String id) {
        return profiles.remove(id) != null;
    }
}