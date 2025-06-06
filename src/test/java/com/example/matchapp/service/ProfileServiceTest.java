package com.example.matchapp.service;

import com.example.matchapp.model.Profile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void generateImages_createsFiles() throws Exception {
        ImageGenerationService imageGenerationService = Mockito.mock(ImageGenerationService.class);
        Mockito.when(imageGenerationService.generateImage(Mockito.any(Profile.class)))
                .thenReturn(new byte[] {1, 2, 3});

        ProfileService profileService = new ProfileService(imageGenerationService);

        List<Profile> result = profileService.generateImages(tempDir);

        for (Profile profile : result) {
            Path imagePath = tempDir.resolve(profile.imageUrl());
            assertTrue(Files.exists(imagePath), "Image not created: " + imagePath);
        }

        Path jsonPath = tempDir.resolve("profiles_with_images.json");
        assertTrue(Files.exists(jsonPath), "Result JSON not created");
    }
}

