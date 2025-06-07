package com.example.matchapp.health;

import com.example.matchapp.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that checks the status of the profile repository.
 * This indicator will be included in the health endpoint response.
 */
@Component
public class ProfileRepositoryHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(ProfileRepositoryHealthIndicator.class);
    
    private final ProfileRepository profileRepository;
    
    public ProfileRepositoryHealthIndicator(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }
    
    @Override
    public Health health() {
        try {
            // Check if the repository is accessible and contains profiles
            int profileCount = profileRepository.findAll().size();
            
            if (profileCount > 0) {
                return Health.up()
                        .withDetail("profileCount", profileCount)
                        .withDetail("status", "Repository is operational")
                        .build();
            } else {
                logger.warn("Profile repository is empty");
                return Health.status("WARNING")
                        .withDetail("profileCount", 0)
                        .withDetail("warning", "Repository is empty")
                        .build();
            }
        } catch (Exception e) {
            logger.error("Error checking profile repository health: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("exception", e.getClass().getName())
                    .build();
        }
    }
}