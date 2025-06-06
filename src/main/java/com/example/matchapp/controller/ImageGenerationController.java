package com.example.matchapp.controller;

import com.example.matchapp.dto.GenerateImageRequest;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.ImageGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for image generation.
 * Provides endpoints to generate images using the OpenAI API.
 */
@RestController
@RequestMapping("/api/images")
public class ImageGenerationController {

    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationController.class);
    private final ImageGenerationService imageGenerationService;

    public ImageGenerationController(ImageGenerationService imageGenerationService) {
        this.imageGenerationService = imageGenerationService;
    }

    /**
     * Generates an image based on the provided profile information.
     * Uses the profile's bio as the prompt for image generation.
     *
     * @param request The request containing profile information for image generation
     * @return The generated image as a byte array
     */
    @Operation(summary = "Generate an image based on profile information", 
               description = "Uses the profile's bio as the prompt for image generation via OpenAI")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image generated successfully",
                     content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE)),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                     content = @Content(mediaType = "application/json", 
                                       schema = @Schema(implementation = com.example.matchapp.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error generating image",
                     content = @Content(mediaType = "application/json", 
                                       schema = @Schema(implementation = com.example.matchapp.exception.ErrorResponse.class)))
    })
    @PostMapping(value = "/generate", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> generateImage(@Valid @RequestBody GenerateImageRequest request) {
        logger.info("Received request to generate image for profile: {}", request.id());

        try {
            // Convert DTO to domain model
            Profile profile = request.toProfile();

            byte[] imageBytes = imageGenerationService.generateImage(profile);
            logger.info("Successfully generated image for profile: {}", profile.id());
            return ResponseEntity.ok(imageBytes);
        } catch (Exception e) {
            logger.error("Error generating image for profile: {}", request.id(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating image: " + e.getMessage(), e);
        }
    }
}
