package com.example.matchapp.service.impl;

import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.service.ImageCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Implementation of ImageCacheService that uses the file system to store cached images.
 */
@Service
public class FileSystemImageCacheService implements ImageCacheService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemImageCacheService.class);

    /**
     * Checks if an image exists in the file system cache for the given profile.
     * The image is considered to be in cache if a file with the profile's imageUrl exists
     * in the specified images directory.
     *
     * @param profile the profile to check
     * @param imagesDir the directory where images are stored
     * @return true if an image exists in the cache, false otherwise
     */
    @Override
    public boolean hasImageInCache(ProfileEntity profile, Path imagesDir) {
        if (profile == null || profile.getImageUrl() == null || profile.getImageUrl().isEmpty()) {
            return false;
        }

        Path imagePath = imagesDir.resolve(profile.getImageUrl());
        boolean exists = Files.exists(imagePath) && Files.isRegularFile(imagePath);

        if (exists) {
            logger.debug("Image found in cache for profile: {}", profile.getId());
        } else {
            logger.debug("Image not found in cache for profile: {}", profile.getId());
        }

        return exists;
    }

    /**
     * Gets an image from the file system cache for the given profile.
     * Reads the image file from the specified images directory using the profile's imageUrl.
     *
     * @param profile the profile to get the image for
     * @param imagesDir the directory where images are stored
     * @return an Optional containing the image bytes if found, or empty if not found
     * @throws IOException if there's an error reading the image file
     */
    @Override
    public Optional<byte[]> getImageFromCache(ProfileEntity profile, Path imagesDir) throws IOException {
        if (!hasImageInCache(profile, imagesDir)) {
            logger.debug("Cannot get image from cache for profile: {} - not in cache", profile.getId());
            return Optional.empty();
        }

        Path imagePath = imagesDir.resolve(profile.getImageUrl());
        logger.info("Reading image from cache for profile: {}", profile.getId());
        return Optional.of(Files.readAllBytes(imagePath));
    }

    /**
     * Puts an image in the file system cache for the given profile.
     * Writes the image bytes to a file in the specified images directory using the profile's imageUrl.
     * Creates the directory if it doesn't exist.
     *
     * @param profile the profile to cache the image for
     * @param imageBytes the image bytes to cache
     * @param imagesDir the directory where images are stored
     * @throws IOException if there's an error writing the image file
     */
    @Override
    public void putImageInCache(ProfileEntity profile, byte[] imageBytes, Path imagesDir) throws IOException {
        if (profile == null || profile.getImageUrl() == null || profile.getImageUrl().isEmpty() || imageBytes == null) {
            logger.warn("Cannot cache image - invalid profile or image data");
            return;
        }

        // Create directories if they don't exist
        Files.createDirectories(imagesDir);

        Path imagePath = imagesDir.resolve(profile.getImageUrl());
        logger.info("Caching image for profile: {}", profile.getId());
        Files.write(imagePath, imageBytes);
    }

    /**
     * Invalidates the file system cache for the given profile by deleting the image file.
     *
     * @param profile the profile to invalidate the cache for
     * @param imagesDir the directory where images are stored
     * @return true if the cache was invalidated (file deleted), false otherwise
     * @throws IOException if there's an error deleting the image file
     */
    @Override
    public boolean invalidateCache(ProfileEntity profile, Path imagesDir) throws IOException {
        if (!hasImageInCache(profile, imagesDir)) {
            logger.debug("Cannot invalidate cache for profile: {} - not in cache", profile.getId());
            return false;
        }

        Path imagePath = imagesDir.resolve(profile.getImageUrl());
        logger.info("Invalidating cached image for profile: {}", profile.getId());
        return Files.deleteIfExists(imagePath);
    }
}
