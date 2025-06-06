package com.example.matchapp.dto;

import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
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
     * Create a ProfileResponse from a Profile record.
     * This method is kept for backward compatibility during the transition.
     *
     * @param profile the profile record
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

    /**
     * Create a ProfileResponse from a ProfileEntity.
     *
     * @param entity the profile entity
     * @return a new ProfileResponse
     */
    public static ProfileResponse fromEntity(ProfileEntity entity) {
        return new ProfileResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAge(),
                entity.getEthnicity(),
                entity.getGender(),
                entity.getBio(),
                entity.getImageUrl(),
                entity.getMyersBriggsPersonalityType(),
                entity.isImageGenerated()
        );
    }
}
