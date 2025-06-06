package com.example.matchapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a new profile.
 */
public record CreateProfileRequest(
        @NotBlank(message = "First name is required")
        String firstName,
        
        @NotBlank(message = "Last name is required")
        String lastName,
        
        @NotNull(message = "Age is required")
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 120, message = "Age must be at most 120")
        Integer age,
        
        @NotBlank(message = "Ethnicity is required")
        String ethnicity,
        
        @NotBlank(message = "Gender is required")
        String gender,
        
        @NotBlank(message = "Bio is required")
        String bio,
        
        String imageUrl,
        
        @NotBlank(message = "Myers-Briggs personality type is required")
        String myersBriggsPersonalityType
) {
}