package com.example.matchapp.service;

import com.example.matchapp.model.ProfileEntity;

/**
 * Service for generating profile images.
 */
public interface ImageGenerationService {
    /**
     * Generates an image for a profile.
     *
     * @param profile the profile to generate an image for
     * @return the generated image bytes
     */
    byte[] generateImage(ProfileEntity profile);
}
