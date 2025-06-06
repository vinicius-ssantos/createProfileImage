package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.ImageGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OpenAIImageGenerationService implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIImageGenerationService.class);

    private final WebClient webClient;

    public OpenAIImageGenerationService(ImageGenProperties properties) {
        this.webClient = WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
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
        } finally {
            MDC.remove("profileId");
        }
    }
}
