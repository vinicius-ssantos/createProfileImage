package com.example.matchapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for image generation requests.
 * Contains the necessary information to generate an image.
 */
public record GenerateImageRequest(
        @NotBlank(message = "Profile ID is required")
        String id,
        
        @NotBlank(message = "First name is required")
        String firstName,
        
        @NotBlank(message = "Last name is required")
        String lastName,
        
        @NotNull(message = "Age is required")
        Integer age,
        
        String ethnicity,
        
        String gender,
        
        @NotBlank(message = "Bio is required for image generation")
        String bio,
        
        String myersBriggsPersonalityType
) {
    /**
     * Convert this request to a Profile object.
     * 
     * @return a new Profile object
     */
    public com.example.matchapp.model.Profile toProfile() {
        return new com.example.matchapp.model.Profile(
                id,
                firstName,
                lastName,
                age,
                ethnicity != null ? ethnicity : "Not specified",
                gender != null ? gender : "Not specified",
                bio,
                id + ".jpg",
                myersBriggsPersonalityType != null ? myersBriggsPersonalityType : "Not specified",
                false
        );
    }
}