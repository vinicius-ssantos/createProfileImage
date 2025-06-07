package com.example.matchapp.service;

import com.example.matchapp.model.Profile;
import com.example.matchapp.service.impl.DefaultPromptBuilderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DefaultPromptBuilderServiceTest {

    private final PromptBuilderService service = new DefaultPromptBuilderService();

    @Test
    void buildPrompt_combinesAllProfileAttributes() {
        Profile profile = new Profile(
                "id",
                "Valentina",
                "Rodriguez",
                26,
                "Hispanic",
                "FEMALE",
                "Loves hiking and photography",
                "img.jpg",
                "ENFP"
        );

        String prompt = service.buildPrompt(profile);

        // Check that all profile attributes are included
        assertTrue(prompt.contains("26-year-old"), "Age should be included in prompt");
        assertTrue(prompt.contains("Hispanic"), "Ethnicity should be included in prompt");
        assertTrue(prompt.contains("woman"), "Gender should be included in prompt");
        assertTrue(prompt.contains("Valentina Rodriguez"), "Name should be included in prompt");
        assertTrue(prompt.contains("Loves hiking and photography"), "Bio should be included in prompt");

        // Check that personality traits are included
        assertTrue(prompt.contains("enthusiastic"), "Personality traits should be included in prompt");

        // Check that technical specifications are included
        assertTrue(prompt.contains("natural soft lighting"), "Technical specifications should be included");
        assertTrue(prompt.contains("photorealistic"), "Image quality specifications should be included");

        // Check that the prompt is in English, not Portuguese
        assertFalse(prompt.contains("mulher"), "Prompt should be in English, not Portuguese");
    }

    @Test
    void buildPrompt_handlesNullBio() {
        Profile profile = new Profile(
                "id",
                "John",
                "Smith",
                30,
                "Caucasian",
                "MALE",
                null,
                "img.jpg",
                "INTJ"
        );

        String prompt = service.buildPrompt(profile);

        // Should not throw exception and should include other attributes
        assertTrue(prompt.contains("30-year-old"), "Age should be included in prompt");
        assertTrue(prompt.contains("Caucasian"), "Ethnicity should be included in prompt");
        assertTrue(prompt.contains("man"), "Gender should be included in prompt");
        assertTrue(prompt.contains("John Smith"), "Name should be included in prompt");
        assertTrue(prompt.contains("thoughtful"), "Personality traits should be included in prompt");

        // Bio should not be included
        assertFalse(prompt.contains("Bio details:"), "Null bio should not be included");
    }

    @Test
    void buildPrompt_handlesUnknownPersonalityType() {
        Profile profile = new Profile(
                "id",
                "Alex",
                "Johnson",
                25,
                "Asian",
                "MALE",
                "Software developer",
                "img.jpg",
                "UNKNOWN"
        );

        String prompt = service.buildPrompt(profile);

        // Should use default personality description
        assertTrue(prompt.contains("natural and authentic expression"), 
                "Unknown personality type should use default description");
    }
}
