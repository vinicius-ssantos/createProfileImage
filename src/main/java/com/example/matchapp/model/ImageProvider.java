package com.example.matchapp.model;

/**
 * Enum representing different image generation providers.
 * This allows for easy selection of the provider in configuration.
 */
public enum ImageProvider {
    /**
     * OpenAI's DALL-E image generation service.
     */
    OPENAI,
    
    /**
     * Spring AI image generation service.
     */
    SPRING_AI,
    
    /**
     * Mock provider for testing purposes.
     */
    MOCK
}