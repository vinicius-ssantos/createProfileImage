package com.example.matchapp.service.impl;

import com.example.matchapp.model.Profile;
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

    @Override
    public boolean hasImageInCache(Profile profile, Path imagesDir) {
        if (profile == null || profile.imageUrl() == null || profile.imageUrl().isEmpty()) {
            return false;
        }

        Path imagePath = imagesDir.resolve(profile.imageUrl());
        boolean exists = Files.exists(imagePath) && Files.isRegularFile(imagePath);
        
        if (exists) {
            logger.debug("Image found in cache for profile: {}", profile.id());
        } else {
            logger.debug("Image not found in cache for profile: {}", profile.id());
        }
        
        return exists;
    }

    @Override
    public Optional<byte[]> getImageFromCache(Profile profile, Path imagesDir) throws IOException {
        if (!hasImageInCache(profile, imagesDir)) {
            logger.debug("Cannot get image from cache for profile: {} - not in cache", profile.id());
            return Optional.empty();
        }

        Path imagePath = imagesDir.resolve(profile.imageUrl());
        logger.info("Reading image from cache for profile: {}", profile.id());
        return Optional.of(Files.readAllBytes(imagePath));
    }

    @Override
    public void putImageInCache(Profile profile, byte[] imageBytes, Path imagesDir) throws IOException {
        if (profile == null || profile.imageUrl() == null || profile.imageUrl().isEmpty() || imageBytes == null) {
            logger.warn("Cannot cache image - invalid profile or image data");
            return;
        }

        // Create directories if they don't exist
        Files.createDirectories(imagesDir);

        Path imagePath = imagesDir.resolve(profile.imageUrl());
        logger.info("Caching image for profile: {}", profile.id());
        Files.write(imagePath, imageBytes);
    }

    @Override
    public boolean invalidateCache(Profile profile, Path imagesDir) throws IOException {
        if (!hasImageInCache(profile, imagesDir)) {
            logger.debug("Cannot invalidate cache for profile: {} - not in cache", profile.id());
            return false;
        }

        Path imagePath = imagesDir.resolve(profile.imageUrl());
        logger.info("Invalidating cached image for profile: {}", profile.id());
        return Files.deleteIfExists(imagePath);
    }
}