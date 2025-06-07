package com.example.matchapp.config;

import com.example.matchapp.mapper.ProfileMapper;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration class that initializes the database with data from profile.json.
 * This ensures that the application has initial data to work with.
 */
@Configuration
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a CommandLineRunner bean that loads initial data from profile.json
     * into the database on application startup.
     *
     * @param profileRepository the repository to save profiles to
     * @return a CommandLineRunner that initializes the database
     */
    @Bean
    public CommandLineRunner initDatabase(ProfileRepository profileRepository) {
        return args -> {
            // Check if the database is already populated
            if (profileRepository.findAll().isEmpty()) {
                logger.info("Initializing database with data from profile.json");
                
                try {
                    var resource = new ClassPathResource("profile.json");
                    Profile[] initialProfiles = objectMapper.readValue(resource.getInputStream(), Profile[].class);
                    
                    List<ProfileEntity> entities = Arrays.stream(initialProfiles)
                            .map(ProfileMapper::toProfileEntity)
                            .toList();
                    
                    // Save each entity to the database
                    for (ProfileEntity entity : entities) {
                        profileRepository.save(entity);
                    }
                    
                    logger.info("Successfully loaded {} profiles into the database", entities.size());
                } catch (IOException e) {
                    logger.error("Failed to load profiles from profile.json", e);
                    throw new RuntimeException("Failed to initialize database with profile data", e);
                }
            } else {
                logger.info("Database already contains profiles, skipping initialization");
            }
        };
    }
}