package com.example.matchapp.mapper;

import com.example.matchapp.dto.CreateProfileRequest;
import com.example.matchapp.dto.ProfileResponse;
import com.example.matchapp.dto.UpdateProfileRequest;
import com.example.matchapp.model.Gender;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;

import java.util.UUID;

/**
 * Mapper class for converting between Profile domain entities and DTOs.
 * This class provides methods to convert between different representations of profiles.
 */
public class ProfileMapper {

    /**
     * Convert a ProfileEntity to a ProfileResponse DTO.
     *
     * @param entity the profile entity to convert
     * @return a ProfileResponse DTO
     */
    public static ProfileResponse toProfileResponse(ProfileEntity entity) {
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

    /**
     * Convert a CreateProfileRequest DTO to a ProfileEntity.
     *
     * @param request the request DTO to convert
     * @return a new ProfileEntity
     */
    public static ProfileEntity toProfileEntity(CreateProfileRequest request) {
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
    public static ProfileEntity updateProfileEntity(ProfileEntity entity, UpdateProfileRequest request) {
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

    /**
     * Convert a Profile record to a ProfileEntity.
     * This is used for backward compatibility during the transition.
     *
     * @param profile the profile record to convert
     * @return a new ProfileEntity
     */
    public static ProfileEntity toProfileEntity(Profile profile) {
        return new ProfileEntity(
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
     * Convert a ProfileEntity to a Profile record.
     * This is used for backward compatibility during the transition.
     *
     * @param entity the profile entity to convert
     * @return a new Profile record
     */
    public static Profile toProfile(ProfileEntity entity) {
        return new Profile(
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