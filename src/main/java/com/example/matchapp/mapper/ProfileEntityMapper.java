package com.example.matchapp.mapper;

import com.example.matchapp.dto.CreateProfileRequest;
import com.example.matchapp.dto.ProfileResponse;
import com.example.matchapp.dto.UpdateProfileRequest;
import com.example.matchapp.model.ProfileEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper class for converting between ProfileEntity and DTOs.
 * This class provides methods to convert between different representations of profiles.
 */
@Component
public class ProfileEntityMapper {

    /**
     * Convert a ProfileEntity to a ProfileResponse DTO.
     *
     * @param entity the profile entity to convert
     * @return a ProfileResponse DTO
     */
    public ProfileResponse entityToResponse(ProfileEntity entity) {
        return ProfileResponse.fromEntity(entity);
    }

    /**
     * Convert a CreateProfileRequest DTO to a ProfileEntity.
     *
     * @param request the request DTO to convert
     * @return a new ProfileEntity
     */
    public ProfileEntity createRequestToEntity(CreateProfileRequest request) {
        return new ProfileEntity(
                null, // ID will be generated
                request.firstName(),
                request.lastName(),
                request.age(),
                request.ethnicity(),
                request.gender(),
                request.bio(),
                request.imageUrl() != null ? request.imageUrl() : UUID.randomUUID().toString() + ".jpg",
                request.myersBriggsPersonalityType(),
                false
        );
    }

    /**
     * Update a ProfileEntity with data from an UpdateProfileRequest DTO.
     *
     * @param entity the entity to update
     * @param request the request containing the updated data
     * @return the updated ProfileEntity
     */
    public ProfileEntity updateEntityFromRequest(ProfileEntity entity, UpdateProfileRequest request) {
        if (request.firstName() != null) {
            entity.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            entity.setLastName(request.lastName());
        }
        if (request.age() != null) {
            entity.setAge(request.age());
        }
        if (request.ethnicity() != null) {
            entity.setEthnicity(request.ethnicity());
        }
        if (request.gender() != null) {
            entity.setGender(request.gender());
        }
        if (request.bio() != null) {
            entity.setBio(request.bio());
        }
        if (request.imageUrl() != null) {
            entity.setImageUrl(request.imageUrl());
        }
        if (request.myersBriggsPersonalityType() != null) {
            entity.setMyersBriggsPersonalityType(request.myersBriggsPersonalityType());
        }
        return entity;
    }
}