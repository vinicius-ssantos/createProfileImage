package com.example.matchapp.controller;

import com.example.matchapp.dto.GenerateImageRequest;
import com.example.matchapp.mapper.GenerateImageRequestMapper;
import com.example.matchapp.mapper.ProfileMapper;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.service.ImageGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test for the ImageGenerationController.
 * 
 * This test uses standalone MockMvc to test the controller without starting the Spring context.
 * It mocks the ImageGenerationService to avoid making real API calls.
 */
class ImageGenerationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ImageGenerationService imageGenerationService;

    @Mock
    private GenerateImageRequestMapper generateImageRequestMapper;

    @Mock
    private ProfileMapper profileMapper;

    @InjectMocks
    private ImageGenerationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    /**
     * Tests that the controller returns the image generated by the service.
     */
    @Test
    void generateImage_returnsImageFromService() throws Exception {
        // Arrange
        GenerateImageRequest request = new GenerateImageRequest(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity",
            com.example.matchapp.model.Gender.MALE,
            "A professional portrait photo", 
            "INTJ"
        );

        // Create a sample image
        byte[] sampleImage = createSampleImage();

        // Create a Profile object that would be returned by the mapper
        Profile profile = new Profile(
            "test-id", 
            "Test", 
            "User", 
            30, 
            "Test Ethnicity",
            com.example.matchapp.model.Gender.MALE,
            "A professional portrait photo", 
            null, 
            "INTJ"
        );

        // Create a ProfileEntity object that would be returned by the mapper
        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setId("test-id");

        // Mock the mappers to return the test objects
        when(generateImageRequestMapper.toProfile(any(GenerateImageRequest.class))).thenReturn(profile);
        when(profileMapper.toEntity(any(Profile.class))).thenReturn(profileEntity);

        // Mock the service to return the sample image
        when(imageGenerationService.generateImage(any(ProfileEntity.class))).thenReturn(sampleImage);

        String requestJson = objectMapper.writeValueAsString(request);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/images/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andReturn();

        byte[] responseBytes = result.getResponse().getContentAsByteArray();

        // Verify the response contains the image
        assertNotNull(responseBytes);
        assertArrayEquals(sampleImage, responseBytes);
    }

    /**
     * Creates a sample image for testing.
     */
    private byte[] createSampleImage() throws IOException {
        // Create a simple 100x100 image
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        // Fill with a color
        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                image.setRGB(x, y, 0xFF0000); // Red
            }
        }

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
