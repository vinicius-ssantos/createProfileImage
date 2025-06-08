package com.example.matchapp.mapper;

import com.example.matchapp.dto.GenerateImageRequest;
import com.example.matchapp.model.Gender;
import com.example.matchapp.model.Profile;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between GenerateImageRequest and Profile.
 */
@Component
public class GenerateImageRequestMapper {

    /**
     * Convert a GenerateImageRequest to a Profile.
     *
     * @param request the request to convert
     * @return a new Profile
     */
    public Profile toProfile(GenerateImageRequest request) {
        return new Profile(
                request.id(),
                request.firstName(),
                request.lastName(),
                request.age(),
                request.ethnicity() != null ? request.ethnicity() : "Not specified",
                request.gender() != null ? request.gender() : Gender.OTHER,
                request.bio(),
                request.id() + ".jpg",
                request.myersBriggsPersonalityType() != null ? request.myersBriggsPersonalityType() : "Not specified",
                false
        );
    }
}