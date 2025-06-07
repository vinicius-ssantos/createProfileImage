package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.ImageProvider;
import com.example.matchapp.service.ImageGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Factory for creating ImageGenerationService instances based on configuration.
 * This allows the application to switch between different image generation providers.
 */
@Component
public class ImageGenerationServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationServiceFactory.class);

    private final ApplicationContext applicationContext;
    private final ImageGenProperties properties;

    @Autowired
    public ImageGenerationServiceFactory(
            ApplicationContext applicationContext,
            @Qualifier("imageGenProperties") ImageGenProperties properties) {
        if (applicationContext == null) {
            throw new NullPointerException("ApplicationContext cannot be null");
        }
        if (properties == null) {
            throw new NullPointerException("ImageGenProperties cannot be null");
        }
        this.applicationContext = applicationContext;
        // Create defensive copy to prevent external modification
        this.properties = copyImageGenProperties(properties);
    }

    /**
     * Creates a defensive copy of ImageGenProperties.
     *
     * @param original the original ImageGenProperties object
     * @return a new ImageGenProperties object with the same properties
     */
    private ImageGenProperties copyImageGenProperties(ImageGenProperties original) {
        ImageGenProperties copy = new ImageGenProperties();
        copy.setApiKey(original.getApiKey());
        copy.setBaseUrl(original.getBaseUrl());
        copy.setProvider(original.getProvider());
        copy.setMaxRetries(original.getMaxRetries());
        copy.setRetryDelay(original.getRetryDelay());
        return copy;
    }

    /**
     * Get the appropriate ImageGenerationService based on the configured provider.
     *
     * @return the configured ImageGenerationService
     */
    public ImageGenerationService getImageGenerationService() {
        ImageProvider provider = properties.getProvider();
        logger.info("Using image generation provider: {}", provider);

        // Handle null provider gracefully
        if (provider == null) {
            logger.warn("Provider is null. Falling back to OpenAI.");
            return applicationContext.getBean(OpenAIImageGenerationService.class);
        }

        switch (provider) {
            case OPENAI:
                return applicationContext.getBean(OpenAIImageGenerationService.class);
            case SPRING_AI:
                return applicationContext.getBean(SpringAIImageGenerationService.class);
            case MOCK:
                // For testing purposes, return a mock implementation
                return profile -> new byte[0]; // Empty image
            default:
                logger.warn("Unknown provider: {}. Falling back to OpenAI.", provider);
                return applicationContext.getBean(OpenAIImageGenerationService.class);
        }
    }
}
