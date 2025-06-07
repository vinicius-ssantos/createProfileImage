package com.example.matchapp.service;

import com.example.matchapp.model.Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Service for caching generated images to avoid redundant API calls.
 */
public interface ImageCacheService {
    
    /**
     * Checks if an image exists in the cache for the given profile.
     *
     * @param profile the profile to check
     * @param imagesDir the directory where images are stored
     * @return true if an image exists in the cache, false otherwise
     */
    boolean hasImageInCache(Profile profile, Path imagesDir);
    
    /**
     * Gets an image from the cache for the given profile.
     *
     * @param profile the profile to get the image for
     * @param imagesDir the directory where images are stored
     * @return an Optional containing the image bytes if found, or empty if not found
     * @throws IOException if there's an error reading the image file
     */
    Optional<byte[]> getImageFromCache(Profile profile, Path imagesDir) throws IOException;
    
    /**
     * Puts an image in the cache for the given profile.
     *
     * @param profile the profile to cache the image for
     * @param imageBytes the image bytes to cache
     * @param imagesDir the directory where images are stored
     * @throws IOException if there's an error writing the image file
     */
    void putImageInCache(Profile profile, byte[] imageBytes, Path imagesDir) throws IOException;
    
    /**
     * Invalidates the cache for the given profile.
     *
     * @param profile the profile to invalidate the cache for
     * @param imagesDir the directory where images are stored
     * @return true if the cache was invalidated, false otherwise
     * @throws IOException if there's an error deleting the image file
     */
    boolean invalidateCache(Profile profile, Path imagesDir) throws IOException;
}