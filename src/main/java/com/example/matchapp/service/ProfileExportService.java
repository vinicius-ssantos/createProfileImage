package com.example.matchapp.service;

import com.example.matchapp.exception.ConfigurationException;
import com.example.matchapp.model.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service responsible for exporting profiles to various formats.
 * This service follows the Single Responsibility Principle by focusing only on
 * profile export operations.
 */
@Service
public class ProfileExportService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileExportService.class);
    private final ObjectMapper objectMapper;

    public ProfileExportService(ObjectMapper objectMapper) {
        if (objectMapper == null) {
            throw new ConfigurationException("ObjectMapper cannot be null", "objectMapper", "null");
        }
        this.objectMapper = objectMapper;
    }

    /**
     * Export profiles to a JSON file.
     *
     * @param profiles the profiles to export
     * @param outputPath the path to write the JSON file to
     * @throws IOException if there's an error writing the file
     */
    public void exportProfilesToJson(List<Profile> profiles, Path outputPath) throws IOException {
        logger.info("Exporting {} profiles to JSON file: {}", profiles.size(), outputPath);
        objectMapper.writeValue(outputPath.toFile(), profiles);
        logger.info("Successfully exported profiles to JSON file");
    }

    /**
     * Export profiles with images to a JSON file.
     * This method is specifically for the profiles_with_images.json file.
     *
     * @param profiles the profiles to export
     * @param imagesDir the directory containing the images
     * @throws IOException if there's an error writing the file
     */
    public void exportProfilesWithImages(List<Profile> profiles, Path imagesDir) throws IOException {
        Path outputPath = imagesDir.resolve("profiles_with_images.json");
        logger.info("Exporting {} profiles with images to JSON file: {}", profiles.size(), outputPath);
        objectMapper.writeValue(outputPath.toFile(), profiles);
        logger.info("Successfully exported profiles with images to JSON file");
    }
}
