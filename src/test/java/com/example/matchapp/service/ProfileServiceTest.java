package com.example.matchapp.service;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.model.Profile;
import com.example.matchapp.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void generateImages_createsFiles() throws Exception {
        // Mock the image generation service
        ImageGenerationService imageGenerationService = Mockito.mock(ImageGenerationService.class);
        Mockito.when(imageGenerationService.generateImage(Mockito.any(Profile.class)))
                .thenReturn(new byte[] {1, 2, 3});

        // Mock the profile repository
        ProfileRepository profileRepository = Mockito.mock(ProfileRepository.class);

        // Create a test profile
        Profile testProfile = new Profile(
            UUID.randomUUID().toString(),
            "Test",
            "User",
            30,
            "Test Ethnicity",
            com.example.matchapp.model.Gender.MALE,
            "Test bio for image generation",
            "test.jpg",
            "INTJ"
        );

        // Configure the mock repository to return our test profile
        Mockito.when(profileRepository.findAll()).thenReturn(List.of(testProfile));
        Mockito.when(profileRepository.findById(Mockito.anyString())).thenReturn(java.util.Optional.of(testProfile));
        Mockito.when(profileRepository.save(Mockito.any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        // Mock the image backup service
        ImageBackupService imageBackupService = Mockito.mock(ImageBackupService.class);

        // Mock the backup properties
        BackupProperties backupProperties = Mockito.mock(BackupProperties.class);

        // Create the profile service with all required dependencies
        ProfileService profileService = new ProfileService(
            imageGenerationService, 
            profileRepository, 
            imageBackupService, 
            backupProperties);

        List<Profile> result = profileService.generateImages(tempDir);

        for (Profile profile : result) {
            Path imagePath = tempDir.resolve(profile.imageUrl());
            assertTrue(Files.exists(imagePath), "Image not created: " + imagePath);
        }

        Path jsonPath = tempDir.resolve("profiles_with_images.json");
        assertTrue(Files.exists(jsonPath), "Result JSON not created");
    }
}
