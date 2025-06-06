package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAIImageGenerationServiceTest {

    private static class TestOpenAIImageGenerationService extends OpenAIImageGenerationService {
        private Map<String, Object> capturedRequest;

        TestOpenAIImageGenerationService(ImageGenProperties properties) {
            super(properties, new DefaultPromptBuilderService());
        }

        @Override
        public byte[] generateImage(Profile profile) {
            capturedRequest = createRequest(profile);
            return new byte[] {1, 2, 3};
        }

        Map<String, Object> getCapturedRequest() {
            return capturedRequest;
        }
    }

    private TestOpenAIImageGenerationService service;

    @BeforeEach
    void setUp() {
        ImageGenProperties props = new ImageGenProperties();
        props.setApiKey("test-key");
        props.setBaseUrl("https://api.openai.com");
        service = new TestOpenAIImageGenerationService(props);
    }

    @Test
    void generateImage_includesModelField() {
        Profile profile = new Profile(
                "id",
                "First",
                "Last",
                30,
                "Ethnicity",
                "MALE",
                "Sample bio",
                "img.jpg",
                "INTJ"
        );

        service.generateImage(profile);
        Map<String, Object> request = service.getCapturedRequest();
        assertEquals("dall-e-3", request.get("model"));
    }
}
