package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.PromptBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for SpringAIImageGenerationService that makes real API calls.
 * 
 * This test will only run if the OPENAI_API_KEY environment variable is set and
 * the RUN_API_TESTS environment variable is set to "true".
 * 
 * To run this test:
 * 1. Set the OPENAI_API_KEY environment variable to your OpenAI API key
 * 2. Set the RUN_API_TESTS environment variable to "true"
 * 3. Run the test
 */
@SpringBootTest
@ActiveProfiles("test")
class SpringAIImageGenerationServiceIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIImageGenerationServiceIntegrationTest.class);

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("imageGenProperties")
    private ImageGenProperties properties;

    @Autowired
    private PromptBuilderService promptBuilderService;

    private SpringAIImageGenerationService service;

    @BeforeEach
    void setUp() {
        service = new SpringAIImageGenerationService(properties, promptBuilderService, () -> { /* no-op */ });
    }

    /**
     * Tests that the service can generate an image by making a real API call.
     * This test will only run if the OPENAI_API_KEY and RUN_API_TESTS environment variables are set.
     */
    @Test
    void generateImage_makesRealApiCall_returnsValidImage() throws IOException {
        // Skip test if environment variables aren't set
        String apiKey = System.getenv("OPENAI_API_KEY");
        String runApiTests = System.getenv("RUN_API_TESTS");

        if (apiKey == null || apiKey.isEmpty() || !"true".equals(runApiTests)) {
            logger.info("Skipping test because OPENAI_API_KEY is not set or RUN_API_TESTS is not 'true'");
            return;
        }
        // Arrange
        Profile profile = new Profile(
            "test-integration-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity",
            com.example.matchapp.model.Gender.MALE,
            "A professional portrait photo of a smiling man with short brown hair", 
            "test-integration.jpg", 
            "INTJ"
        );

        logger.info("Starting real API test with profile: {}", profile.id());

        // Act
        byte[] imageBytes = service.generateImage(profile);

        // Assert
        assertNotNull(imageBytes, "Image bytes should not be null");
        assertTrue(imageBytes.length > 0, "Image bytes should not be empty");

        // Verify the bytes can be read as an image
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        assertNotNull(image, "Should be able to read bytes as an image");
        assertEquals(1024, image.getWidth(), "Image width should be 1024 pixels");
        assertEquals(1024, image.getHeight(), "Image height should be 1024 pixels");

        // Save the image to a file for manual inspection (optional)
        Path testOutputDir = Paths.get("target", "test-output");
        Files.createDirectories(testOutputDir);
        Path imagePath = testOutputDir.resolve("test-integration-image.jpg");
        Files.write(imagePath, imageBytes);

        logger.info("Successfully generated and verified image. Saved to: {}", imagePath);
    }
}
