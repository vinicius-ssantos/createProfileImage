package com.example.matchapp.service;

import com.example.matchapp.config.BackupProperties;
import com.example.matchapp.mapper.ProfileMapper;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class ProfileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void generateImages_createsFiles() throws Exception {
        // Mock the image generation service
        ImageGenerationService imageGenerationService = Mockito.mock(ImageGenerationService.class);
        Mockito.when(imageGenerationService.generateImage(Mockito.any(ProfileEntity.class)))
                .thenReturn(new byte[] {1, 2, 3});

        // Mock the profile repository
        ProfileRepository profileRepository = Mockito.mock(ProfileRepository.class);

        // Create a test profile entity
        ProfileEntity testProfileEntity = new ProfileEntity(
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

        // Create a corresponding Profile for assertions
        Profile testProfile = ProfileMapper.toProfile(testProfileEntity);

        // Configure the mock repository to return our test profile entity
        Mockito.when(profileRepository.findAll()).thenReturn(List.of(testProfileEntity));
        Mockito.when(profileRepository.findById(Mockito.anyString())).thenReturn(java.util.Optional.of(testProfileEntity));
        Mockito.when(profileRepository.save(Mockito.any(ProfileEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Mock the image backup service
        ImageBackupService imageBackupService = Mockito.mock(ImageBackupService.class);

        // Create a real BackupProperties instance
        BackupProperties backupProperties = new BackupProperties();

        // Mock the image cache service
        ImageCacheService imageCacheService = Mockito.mock(ImageCacheService.class);
        // Configure the cache service to indicate the image is not in cache
        Mockito.when(imageCacheService.hasImageInCache(Mockito.any(ProfileEntity.class), Mockito.any(Path.class))).thenReturn(false);

        // Mock the putImageInCache method to write the file to the file system
        Mockito.doAnswer(invocation -> {
            ProfileEntity profileEntity = invocation.getArgument(0);
            byte[] imageBytes = invocation.getArgument(1);
            Path dir = invocation.getArgument(2);

            // Create directories if they don't exist
            Files.createDirectories(dir);

            // Write the image to a file
            Files.write(dir.resolve(profileEntity.getImageUrl()), imageBytes);

            return null;
        }).when(imageCacheService).putImageInCache(Mockito.any(ProfileEntity.class), Mockito.any(byte[].class), Mockito.any(Path.class));

        // Add debug logging to see what's happening
        System.out.println("[DEBUG_LOG] Test directory: " + tempDir.toString());
        System.out.println("[DEBUG_LOG] Test profile image URL: " + testProfile.imageUrl());

        // Create the profile service with all required dependencies
        ProfileService profileService = new ProfileService(
            imageGenerationService, 
            profileRepository, 
            imageBackupService, 
            backupProperties,
            imageCacheService);

        List<Profile> result = profileService.generateImages(tempDir);

        for (Profile profile : result) {
            Path imagePath = tempDir.resolve(profile.imageUrl());
            assertTrue(Files.exists(imagePath), "Image not created: " + imagePath);
        }

        Path jsonPath = tempDir.resolve("profiles_with_images.json");
        assertTrue(Files.exists(jsonPath), "Result JSON not created");
    }

    @Test
    void generateImageForProfile_usesCachedImage_whenAvailable() throws Exception {
        // Mock the image generation service
        ImageGenerationService imageGenerationService = mock(ImageGenerationService.class);
        when(imageGenerationService.generateImage(any(ProfileEntity.class)))
                .thenReturn(new byte[] {1, 2, 3});

        // Mock the profile repository
        ProfileRepository profileRepository = mock(ProfileRepository.class);

        // Create a test profile entity
        ProfileEntity testProfileEntity = new ProfileEntity(
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

        // Create a corresponding Profile for assertions
        Profile testProfile = ProfileMapper.toProfile(testProfileEntity);

        // Configure the mock repository
        when(profileRepository.findById(anyString())).thenReturn(Optional.of(testProfileEntity));
        when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Mock the image backup service
        ImageBackupService imageBackupService = mock(ImageBackupService.class);

        // Create a real BackupProperties instance
        BackupProperties backupProperties = new BackupProperties();

        // Mock the image cache service
        ImageCacheService imageCacheService = mock(ImageCacheService.class);

        // Configure the cache service to return a cached image
        byte[] cachedImage = new byte[] {4, 5, 6};
        when(imageCacheService.hasImageInCache(any(ProfileEntity.class), any(Path.class))).thenReturn(true);
        when(imageCacheService.getImageFromCache(any(ProfileEntity.class), any(Path.class))).thenReturn(Optional.of(cachedImage));

        // Create the profile service
        ProfileService profileService = new ProfileService(
            imageGenerationService,
            profileRepository,
            imageBackupService,
            backupProperties,
            imageCacheService
        );

        // Generate image for the profile
        profileService.generateImageForProfile(testProfile.id(), tempDir);

        // Verify that the image generation service was not called
        verify(imageGenerationService, never()).generateImage(any(ProfileEntity.class));

        // Verify that the cache service was called
        verify(imageCacheService).hasImageInCache(any(ProfileEntity.class), any(Path.class));
        verify(imageCacheService).getImageFromCache(any(ProfileEntity.class), any(Path.class));
    }

    @Test
    void generateImageForProfile_generatesNewImage_whenNotInCache() throws Exception {
        // Mock the image generation service
        ImageGenerationService imageGenerationService = mock(ImageGenerationService.class);
        byte[] generatedImage = new byte[] {1, 2, 3};
        when(imageGenerationService.generateImage(any(ProfileEntity.class)))
                .thenReturn(generatedImage);

        // Mock the profile repository
        ProfileRepository profileRepository = mock(ProfileRepository.class);

        // Create a test profile entity
        ProfileEntity testProfileEntity = new ProfileEntity(
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

        // Create a corresponding Profile for assertions
        Profile testProfile = ProfileMapper.toProfile(testProfileEntity);

        // Configure the mock repository
        when(profileRepository.findById(anyString())).thenReturn(Optional.of(testProfileEntity));
        when(profileRepository.save(any(ProfileEntity.class))).thenAnswer(i -> i.getArgument(0));

        // Mock the image backup service
        ImageBackupService imageBackupService = mock(ImageBackupService.class);

        // Create a real BackupProperties instance
        BackupProperties backupProperties = new BackupProperties();

        // Mock the image cache service
        ImageCacheService imageCacheService = mock(ImageCacheService.class);

        // Configure the cache service to indicate the image is not in cache
        when(imageCacheService.hasImageInCache(any(ProfileEntity.class), any(Path.class))).thenReturn(false);

        // Create the profile service
        ProfileService profileService = new ProfileService(
            imageGenerationService,
            profileRepository,
            imageBackupService,
            backupProperties,
            imageCacheService
        );

        // Generate image for the profile
        profileService.generateImageForProfile(testProfile.id(), tempDir);

        // Verify that the image generation service was called
        verify(imageGenerationService).generateImage(any(ProfileEntity.class));

        // Verify that the cache service was called to check for the image
        verify(imageCacheService).hasImageInCache(any(ProfileEntity.class), any(Path.class));

        // Verify that the cache service was called to store the generated image
        verify(imageCacheService).putImageInCache(any(ProfileEntity.class), eq(generatedImage), any(Path.class));
    }
}
