package com.example.matchapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * DTO for updating an existing profile.
 * All fields are optional to allow partial updates.
 */
public record UpdateProfileRequest(
        String firstName,
        String lastName,
        
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 120, message = "Age must be at most 120")
        Integer age,
        
        String ethnicity,
        String gender,
        String bio,
        String imageUrl,
        String myersBriggsPersonalityType
) {
}