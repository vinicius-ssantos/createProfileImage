package com.example.matchapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.example.matchapp.model.Gender;
import com.example.matchapp.validation.MyersBriggs;

/**
 * DTO for updating an existing profile.
 * All fields are optional to allow partial updates.
 */
public record UpdateProfileRequest(
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 120, message = "Age must be at most 120")
        Integer age,

        @Size(min = 2, max = 50, message = "Ethnicity must be between 2 and 50 characters")
        String ethnicity,

        Gender gender,

        @Size(min = 10, max = 500, message = "Bio must be between 10 and 500 characters")
        String bio,

        @Pattern(regexp = "^$|^[a-zA-Z0-9_-]+\\.(jpg|jpeg|png)$", 
                message = "Image URL must be empty or a valid filename with jpg, jpeg, or png extension")
        String imageUrl,

        @MyersBriggs
        String myersBriggsPersonalityType
) {
}
