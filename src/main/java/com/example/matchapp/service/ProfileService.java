package com.example.matchapp.service;

import com.example.matchapp.model.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@Service
public class ProfileService {

    private final ImageGenerationService imageGenerationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProfileService(ImageGenerationService imageGenerationService) {
        this.imageGenerationService = imageGenerationService;
    }

    public List<Profile> generateImages(Path imagesDir) throws IOException {
        var resource = new ClassPathResource("profile.json");
        Profile[] profiles = objectMapper.readValue(resource.getInputStream(), Profile[].class);

        Files.createDirectories(imagesDir);

        List<Profile> processed = Arrays.stream(profiles)
                .map(p -> new Profile(p.id(), p.firstName(), p.lastName(), p.age(), p.ethnicity(), p.gender(), p.bio(), p.imageUrl(), p.myersBriggsPersonalityType(), true))
                .toList();

        for (Profile profile : processed) {
            byte[] image = imageGenerationService.generateImage(profile);
            Files.write(imagesDir.resolve(profile.imageUrl()), image);
        }

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(imagesDir.resolve("profiles_with_images.json").toFile(), processed);

        return processed;
    }
}
