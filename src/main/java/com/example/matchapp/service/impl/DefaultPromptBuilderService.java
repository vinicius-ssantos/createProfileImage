package com.example.matchapp.service.impl;

import com.example.matchapp.model.Profile;
import com.example.matchapp.service.PromptBuilderService;
import org.springframework.stereotype.Service;

/**
 * Default implementation that combines profile fields in a simple text prompt.
 */
@Service
public class DefaultPromptBuilderService implements PromptBuilderService {

    @Override
    public String buildPrompt(Profile profile) {
        String genderPt = genderToPortuguese(profile.gender());
        return String.format(
            "Crie um retrato fotogr\u00e1fico realista (1024\u00d71024) de %s %s, %d anos.%n" +
            "Nome: %s %s.%n" +
            "Personalidade (%s).%n" +
            "Arquivo de sa\u00edda: %s",
            genderPt,
            profile.ethnicity().toLowerCase(),
            profile.age(),
            profile.firstName(),
            profile.lastName(),
            profile.myersBriggsPersonalityType(),
            profile.imageUrl()
        );
    }

    private String genderToPortuguese(String gender) {
        if (gender == null) {
            return "";
        }
        return switch (gender.toUpperCase()) {
            case "MALE" -> "homem";
            case "FEMALE" -> "mulher";
            default -> gender.toLowerCase();
        };
    }
}
