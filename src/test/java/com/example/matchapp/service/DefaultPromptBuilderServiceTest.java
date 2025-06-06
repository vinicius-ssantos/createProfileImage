package com.example.matchapp.service;

import com.example.matchapp.model.Profile;
import com.example.matchapp.service.impl.DefaultPromptBuilderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPromptBuilderServiceTest {

    private final PromptBuilderService service = new DefaultPromptBuilderService();

    @Test
    void buildPrompt_combinesProfileFields() {
        Profile profile = new Profile(
                "id",
                "Valentina",
                "Rodriguez",
                26,
                "Hispanic",
                "FEMALE",
                "Bio",
                "img.jpg",
                "ENFP"
        );

        String prompt = service.buildPrompt(profile);

        assertTrue(prompt.contains("mulher hispanic"));
        assertTrue(prompt.contains("26"));
        assertTrue(prompt.contains("Valentina Rodriguez"));
        assertTrue(prompt.contains("ENFP"));
        assertTrue(prompt.contains("img.jpg"));
    }
}
