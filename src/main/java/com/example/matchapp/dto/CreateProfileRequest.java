package com.example.matchapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.example.matchapp.model.Gender;
import com.example.matchapp.validation.MyersBriggs;

/**
 * DTO for creating a new profile.
 */
public record CreateProfileRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,

        @NotNull(message = "Age is required")
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 120, message = "Age must be at most 120")
        Integer age,

        @NotBlank(message = "Ethnicity is required")
        @Size(min = 2, max = 50, message = "Ethnicity must be between 2 and 50 characters")
        String ethnicity,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotBlank(message = "Bio is required")
        @Size(min = 10, max = 500, message = "Bio must be between 10 and 500 characters")
        String bio,

        @Pattern(regexp = "^$|^[a-zA-Z0-9_-]+\\.(jpg|jpeg|png)$", 
                message = "Image URL must be empty or a valid filename with jpg, jpeg, or png extension")
        String imageUrl,

        @NotBlank(message = "Myers-Briggs personality type is required")
        @MyersBriggs
        String myersBriggsPersonalityType
) {
}
