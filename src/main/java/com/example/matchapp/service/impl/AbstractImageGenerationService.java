package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.ImageGenerationException;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.service.ImageGenerationService;
import com.example.matchapp.service.PromptBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import com.example.matchapp.util.LoggingUtils;

/**
 * Abstract base class for image generation services.
 * Provides common functionality and structure for different providers.
 */
public abstract class AbstractImageGenerationService implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractImageGenerationService.class);

    protected final ImageGenProperties properties;
    protected final PromptBuilderService promptBuilder;

    protected AbstractImageGenerationService(
            ImageGenProperties properties,
            PromptBuilderService promptBuilder) {
        this.properties = properties;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public byte[] generateImage(ProfileEntity profile) {
        LoggingUtils.setProfileId(profile.getId());

        try {
            logger.info("Requesting image generation from provider: {}", getProviderName());

            // Call the provider-specific implementation
            return generateImageFromProvider(profile);
        } catch (Exception e) {
            logger.error("Error generating image with provider: {}", getProviderName(), e);
            throw handleProviderException(e);
        } finally {
            LoggingUtils.clearMDC();
        }
    }

    /**
     * Generate an image using the specific provider implementation.
     * This method must be implemented by concrete subclasses.
     *
     * @param profile the profile to generate an image for
     * @return the generated image as a byte array
     * @throws Exception if image generation fails
     */
    protected abstract byte[] generateImageFromProvider(ProfileEntity profile) throws Exception;

    /**
     * Handle provider-specific exceptions and convert them to appropriate application exceptions.
     * This method should be overridden by concrete subclasses to provide provider-specific error handling.
     *
     * @param exception the exception thrown by the provider
     * @return an appropriate runtime exception
     */
    protected RuntimeException handleProviderException(Exception exception) {
        // Default implementation wraps the exception in an ImageGenerationException
        return new ImageGenerationException("Error generating image: " + exception.getMessage(), exception);
    }

    /**
     * Get the name of the provider for logging purposes.
     *
     * @return the provider name
     */
    protected abstract String getProviderName();
}
