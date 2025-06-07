package com.example.matchapp;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.config.ImageGenProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Main application class for the Profile Image Generator.
 * This Spring Boot application generates profile images using AI image generation services.
 * It loads profile data, generates images for each profile, and saves the images to the file system.
 */
@SpringBootApplication(scanBasePackages = {"com.example.matchapp"})
@EnableConfigurationProperties({ImageGenProperties.class, BackupProperties.class})
public class CreateIaProfilesApplication {

    /**
     * Main method that starts the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CreateIaProfilesApplication.class, args);
    }
}
