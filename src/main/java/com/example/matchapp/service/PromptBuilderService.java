package com.example.matchapp.service;

import com.example.matchapp.model.Profile;

/**
 * Service for building image generation prompts from profile data.
 */
public interface PromptBuilderService {
    /**
     * Builds a prompt for image generation based on the given profile.
     *
     * @param profile user profile
     * @return prompt string
     */
    String buildPrompt(Profile profile);
}
