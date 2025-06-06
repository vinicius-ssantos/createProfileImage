package com.example.matchapp.service;

import com.example.matchapp.model.Profile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProfileServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void generatesImages() throws IOException {
        ImageGenerationService imageService = Mockito.mock(ImageGenerationService.class);
        when(imageService.generateImage(any())).thenReturn(new byte[] {1,2,3});

        ProfileService service = new ProfileService(imageService);
        var profiles = service.generateImages(tempDir);

        for (Profile profile : profiles) {
            assertTrue(Files.exists(tempDir.resolve(profile.imageUrl())));
            assertTrue(profile.imageGenerated());
        }

        assertTrue(Files.exists(tempDir.resolve("profiles_with_images.json")));
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        Profile[] read = mapper.readValue(tempDir.resolve("profiles_with_images.json").toFile(), Profile[].class);
        assertTrue(read.length > 0);
    }
}
