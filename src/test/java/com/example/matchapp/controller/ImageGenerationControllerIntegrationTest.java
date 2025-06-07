package com.example.matchapp.controller;

import com.example.matchapp.model.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the ImageGenerationController.
 * 
 * This test makes real HTTP requests to the API endpoint and verifies that images are returned.
 * It will only run if the OPENAI_API_KEY environment variable is set and
 * the RUN_API_TESTS environment variable is set to "true".
 * 
 * To run this test:
 * 1. Set the OPENAI_API_KEY environment variable to your OpenAI API key
 * 2. Set the RUN_API_TESTS environment variable to "true"
 * 3. Run the test
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImageGenerationControllerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationControllerIntegrationTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Tests that the API endpoint can generate an image by making a real API call.
     * This test will only run if the OPENAI_API_KEY and RUN_API_TESTS environment variables are set.
     */
    @Test
    void generateImage_makesRealApiCall_returnsValidImage() throws Exception {
        // Skip test if environment variables aren't set
        String apiKey = System.getenv("OPENAI_API_KEY");
        String runApiTests = System.getenv("RUN_API_TESTS");

        if (apiKey == null || apiKey.isEmpty() || !"true".equals(runApiTests)) {
            logger.info("Skipping test because OPENAI_API_KEY is not set or RUN_API_TESTS is not 'true'");
            return;
        }
        // Arrange
        Profile profile = new Profile(
            "test-api-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity",
            com.example.matchapp.model.Gender.MALE,
            "A professional portrait photo of a smiling woman with long blonde hair", 
            "test-api.jpg", 
            "INTJ"
        );

        String profileJson = objectMapper.writeValueAsString(profile);
        logger.info("Starting real API test with profile: {}", profile.id());

        // Act
        MvcResult result = mockMvc.perform(post("/images/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(profileJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andReturn();

        // Get the response body as a byte array
        byte[] imageBytes = result.getResponse().getContentAsByteArray();

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
        Path imagePath = testOutputDir.resolve("test-api-image.jpg");
        Files.write(imagePath, imageBytes);

        logger.info("Successfully generated and verified image from API. Saved to: {}", imagePath);
    }
}
