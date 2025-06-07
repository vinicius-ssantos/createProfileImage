package com.example.matchapp.service.impl;

import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.service.PromptBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced implementation of PromptBuilderService that creates rich, detailed prompts
 * using all profile attributes to generate high-quality, personalized images.
 */
@Service
public class DefaultPromptBuilderService implements PromptBuilderService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPromptBuilderService.class);

    // Mapping of Myers-Briggs personality types to visual characteristics
    private static final Map<String, String> PERSONALITY_TRAITS = new HashMap<>();

    static {
        // Analysts
        PERSONALITY_TRAITS.put("INTJ", "thoughtful, reserved, with a confident and analytical expression");
        PERSONALITY_TRAITS.put("INTP", "contemplative, with a curious and intellectual demeanor");
        PERSONALITY_TRAITS.put("ENTJ", "confident, with a commanding presence and determined expression");
        PERSONALITY_TRAITS.put("ENTP", "animated, with an expressive face showing intellectual curiosity");

        // Diplomats
        PERSONALITY_TRAITS.put("INFJ", "gentle, with a compassionate expression and thoughtful gaze");
        PERSONALITY_TRAITS.put("INFP", "dreamy, with a gentle and idealistic expression");
        PERSONALITY_TRAITS.put("ENFJ", "warm, with an engaging and charismatic smile");
        PERSONALITY_TRAITS.put("ENFP", "enthusiastic, with a bright, expressive face and warm smile");

        // Sentinels
        PERSONALITY_TRAITS.put("ISTJ", "serious, with a composed and reliable appearance");
        PERSONALITY_TRAITS.put("ISFJ", "kind, with a nurturing and attentive expression");
        PERSONALITY_TRAITS.put("ESTJ", "organized, with a practical and decisive appearance");
        PERSONALITY_TRAITS.put("ESFJ", "friendly, with a sociable and caring expression");

        // Explorers
        PERSONALITY_TRAITS.put("ISTP", "calm, with an observant and practical demeanor");
        PERSONALITY_TRAITS.put("ISFP", "gentle, with an artistic and sensitive expression");
        PERSONALITY_TRAITS.put("ESTP", "energetic, with a bold and adventurous appearance");
        PERSONALITY_TRAITS.put("ESFP", "lively, with a playful and expressive face");
    }

    @Override
    public String buildPrompt(ProfileEntity profile) {
        // Get personality traits based on Myers-Briggs type
        String personalityTraits = PERSONALITY_TRAITS.getOrDefault(
                profile.getMyersBriggsPersonalityType(),
                "with a natural and authentic expression"
        );

        // Convert gender to English for better OpenAI results
        String genderTerm = genderToEnglish(profile.getGender());

        // Build a comprehensive prompt that includes all relevant profile attributes
        StringBuilder promptBuilder = new StringBuilder();

        // Main subject description
        promptBuilder.append(String.format(
                "Create a realistic photographic portrait (1024×1024) of a %d-year-old %s %s, %s. ",
                profile.getAge(),
                profile.getEthnicity(),
                genderTerm,
                personalityTraits
        ));

        // Add name for context
        promptBuilder.append(String.format("Their name is %s %s. ", profile.getFirstName(), profile.getLastName()));

        // Include bio if available
        if (profile.getBio() != null && !profile.getBio().isEmpty()) {
            promptBuilder.append(String.format("Bio details: %s. ", profile.getBio()));
        }

        // Technical specifications
        promptBuilder.append("The image should have natural soft lighting, a neutral background, ");
        promptBuilder.append("and professional photography quality with realistic skin texture and details. ");
        promptBuilder.append("The subject should be looking slightly off-camera with a natural pose. ");
        promptBuilder.append("Use a shallow depth of field for a professional portrait effect. ");
        promptBuilder.append("The image should be high resolution, photorealistic, with no watermarks or text.");

        String prompt = promptBuilder.toString();
        logger.debug("Generated prompt for profile {}: {}", profile.getId(), prompt);

        return prompt;
    }

    /**
     * Converts gender string to English terms for better OpenAI results.
     * 
     * @param gender the gender string from the profile
     * @return the English term for the gender
     */
    private String genderToEnglish(com.example.matchapp.model.Gender gender) {
        if (gender == null) {
            return "person";
        }
        return switch (gender) {
            case MALE -> "man";
            case FEMALE -> "woman";
            default -> "person";
        };
    }
}
