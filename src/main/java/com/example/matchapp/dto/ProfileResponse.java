package com.example.matchapp.dto;

import com.example.matchapp.model.Profile;
import com.example.matchapp.model.Gender;

/**
 * DTO for profile responses.
 * Used to standardize the API responses when returning profile data.
 */
public record ProfileResponse(
        String id,
        String firstName,
        String lastName,
        int age,
        String ethnicity,
        Gender gender,
        String bio,
        String imageUrl,
        String myersBriggsPersonalityType,
        boolean imageGenerated
) {
    /**
     * Create a ProfileResponse from a Profile entity.
     *
     * @param profile the profile entity
     * @return a new ProfileResponse
     */
    public static ProfileResponse fromProfile(Profile profile) {
        return new ProfileResponse(
                profile.id(),
                profile.firstName(),
                profile.lastName(),
                profile.age(),
                profile.ethnicity(),
                profile.gender(),
                profile.bio(),
                profile.imageUrl(),
                profile.myersBriggsPersonalityType(),
                profile.imageGenerated()
        );
    }
}