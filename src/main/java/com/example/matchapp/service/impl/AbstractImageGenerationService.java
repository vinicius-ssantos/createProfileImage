package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.ImageGenerationException;
import com.example.matchapp.metrics.ImageGenerationMetrics;
import com.example.matchapp.model.Profile;
import com.example.matchapp.service.ImageCacheService;
import com.example.matchapp.service.ImageGenerationService;
import com.example.matchapp.service.PromptBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Abstract base class for image generation services.
 * Provides common functionality like caching, metrics, and logging.
 */
public abstract class AbstractImageGenerationService implements ImageGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractImageGenerationService.class);

    protected final ImageGenProperties properties;
    protected final PromptBuilderService promptBuilder;
    protected final ImageCacheService imageCacheService;
    protected final ImageGenerationMetrics metrics;

    protected AbstractImageGenerationService(
            ImageGenProperties properties,
            PromptBuilderService promptBuilder,
            ImageCacheService imageCacheService,
            ImageGenerationMetrics metrics) {
        this.properties = properties;
        this.promptBuilder = promptBuilder;
        this.imageCacheService = imageCacheService;
        this.metrics = metrics;
    }

    @Override
    public byte[] generateImage(Profile profile) {
        MDC.put("profileId", profile.id());
        // Start timer for response time measurement
        var timerSample = metrics.startTimer();

        // Record the request
        metrics.recordRequest();

        try {
            // Check if image is already in cache
            if (imageCacheService.hasImage(profile)) {
                logger.info("Image found in cache for profile {}", profile.id());
                byte[] cachedImage = imageCacheService.getImage(profile);
                if (cachedImage != null) {
                    // Record cache hit
                    metrics.recordCacheHit();
                    return cachedImage;
                }
                logger.warn("Cache reported image exists but returned null for profile {}", profile.id());
            }

            logger.info("Requesting image generation from provider: {}", getProviderName());

            try {
                // Call the provider-specific implementation
                byte[] imageData = generateImageFromProvider(profile);

                // Store the image in the cache
                logger.debug("Storing image in cache for profile {}", profile.id());
                imageCacheService.putImage(profile, imageData);

                // Record successful API call
                metrics.recordSuccess();

                return imageData;
            } catch (Exception e) {
                logger.error("Error generating image with provider: {}", getProviderName(), e);
                metrics.recordFailure();
                throw handleProviderException(e);
            }
        } finally {
            // Always stop the timer and remove MDC context
            metrics.stopTimer(timerSample);
            MDC.remove("profileId");
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
    protected abstract byte[] generateImageFromProvider(Profile profile) throws Exception;

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
