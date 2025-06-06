package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.ImageGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OpenAIImageGenerationService implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIImageGenerationService.class);

    private final WebClient webClient;
    private final String apiKey;

    public OpenAIImageGenerationService(ImageGenProperties properties) {
        this.apiKey = properties.getApiKey();

        if (!StringUtils.hasText(apiKey) || "your_openai_key_here".equals(apiKey)) {
            logger.error("OpenAI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.");
            throw new IllegalStateException("OpenAI API key is missing or using the default placeholder value. Please set a valid OPENAI_API_KEY environment variable in your .env file or system environment variables.");
        }

        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public byte[] generateImage(Profile profile) {
        MDC.put("profileId", profile.id());
        try {
            logger.info("Requesting image generation");
            Map<String, Object> request = Map.of(
                    "prompt", profile.bio(),
                    "n", 1,
                    "size", "1024x1024",
                    "response_format", "b64_json"
            );

            try {
                // This call returns JSON with base64 image.
                var response = webClient.post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();

                if (response == null) {
                    throw new RuntimeException("Null response from OpenAI");
                }

                var data = (java.util.List<Map<String, String>>) response.get("data");
                if (data == null || data.isEmpty()) {
                    throw new RuntimeException("Empty image data");
                }

                String base64 = data.get(0).get("b64_json");
                return java.util.Base64.getDecoder().decode(base64);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("401 Unauthorized")) {
                    logger.error("Authentication failed with OpenAI API. Please check your API key in the .env file or system environment variables.", e);
                    throw new IllegalStateException("Authentication failed with OpenAI API. Please check your API key in the .env file or system environment variables.", e);
                } else {
                    logger.error("Error generating image with OpenAI API", e);
                    throw new RuntimeException("Error generating image with OpenAI API: " + e.getMessage(), e);
                }
            }
        } finally {
            MDC.remove("profileId");
        }
    }
}
