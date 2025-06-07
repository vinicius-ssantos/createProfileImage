package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.model.Gender;
import com.example.matchapp.model.ImageProvider;
import com.example.matchapp.model.Profile;
import com.example.matchapp.model.ProfileEntity;
import com.example.matchapp.service.ImageGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Removed MockitoExtension to avoid Java 24 compatibility issues
class ImageGenerationServiceFactoryTest {

    // Using a real ApplicationContext mock created manually
    private ApplicationContext applicationContext;

    // Using a real instance instead of a mock to avoid Java 24 compatibility issues with Mockito
    private ImageGenProperties properties;

    // Using test implementations instead of mocks
    private TestOpenAIImageGenerationService openAIService;
    private TestSpringAIImageGenerationService springAIService;

    private ImageGenerationServiceFactory factory;

    // Test implementation of OpenAIImageGenerationService that bypasses the API key check
    private static class TestOpenAIImageGenerationService extends OpenAIImageGenerationService {
        // Static method to create properties with a dummy API key
        private static ImageGenProperties createTestProperties() {
            ImageGenProperties props = new ImageGenProperties();
            props.setApiKey("test-api-key-for-unit-tests");
            return props;
        }

        // Use a constructor that calls super with pre-configured properties
        public TestOpenAIImageGenerationService() {
            // Call the parent constructor with pre-configured properties and null for other dependencies
            super(createTestProperties(), null, null, null);
        }

        // Override WebClient creation and other initialization that might cause issues
        @Override
        public byte[] generateImage(ProfileEntity profile) {
            // Return dummy image data
            return new byte[] {1, 2, 3, 4, 5};
        }

        // Override the method that would normally make API calls
        @Override
        protected byte[] generateImageFromProvider(ProfileEntity profile) throws Exception {
            // Return dummy image data without making any API calls
            return new byte[] {1, 2, 3, 4, 5};
        }
    }

    // Test implementation of SpringAIImageGenerationService that avoids initialization issues
    private static class TestSpringAIImageGenerationService extends SpringAIImageGenerationService {
        // Static method to create properties with a dummy API key
        private static ImageGenProperties createTestProperties() {
            ImageGenProperties props = new ImageGenProperties();
            props.setApiKey("test-api-key-for-unit-tests");
            return props;
        }

        // Use a constructor that calls super with pre-configured properties
        public TestSpringAIImageGenerationService() {
            // Call the parent constructor with pre-configured properties and null for other dependencies
            super(createTestProperties(), null, null);
        }

        // Override methods that would normally make API calls
        @Override
        public byte[] generateImage(ProfileEntity profileEntity) {
            // Return dummy image data
            return new byte[] {1, 2, 3, 4, 5};
        }

        @Override
        protected byte[] generateImageFromProvider(ProfileEntity profileEntity) {
            // Return dummy image data without making any API calls
            return new byte[] {1, 2, 3, 4, 5};
        }
    }

    @BeforeEach
    void setUp() {
        // Create a real properties object
        properties = new ImageGenProperties();

        // Create test implementations instead of mocks
        openAIService = new TestOpenAIImageGenerationService();
        springAIService = new TestSpringAIImageGenerationService();

        // Create a real ApplicationContext mock using Mockito.mock() instead of @Mock
        applicationContext = mock(ApplicationContext.class);

        // Configure the ApplicationContext mock to return our test implementations
        when(applicationContext.getBean(OpenAIImageGenerationService.class)).thenReturn(openAIService);
        when(applicationContext.getBean(SpringAIImageGenerationService.class)).thenReturn(springAIService);

        // Create the factory with the real properties and mocked context
        factory = new ImageGenerationServiceFactory(applicationContext, properties);
    }

    @Test
    void getImageGenerationService_returnsOpenAIService_whenProviderIsOpenAI() {
        // Arrange - set the provider directly on the real properties object
        properties.setProvider(ImageProvider.OPENAI);

        // Act
        ImageGenerationService service = factory.getImageGenerationService();

        // Assert
        assertInstanceOf(OpenAIImageGenerationService.class, service);
    }

    @Test
    void getImageGenerationService_returnsSpringAIService_whenProviderIsSpringAI() {
        // Arrange - set the provider directly on the real properties object
        properties.setProvider(ImageProvider.SPRING_AI);

        // Act
        ImageGenerationService service = factory.getImageGenerationService();

        // Assert
        assertInstanceOf(SpringAIImageGenerationService.class, service);
    }

    @Test
    void getImageGenerationService_returnsMockImplementation_whenProviderIsMock() {
        // Arrange - set the provider directly on the real properties object
        properties.setProvider(ImageProvider.MOCK);

        // Act
        ImageGenerationService service = factory.getImageGenerationService();

        // Assert
        // The mock implementation is a lambda, so we can't easily check its type
        // Instead, we'll verify it's not one of the other implementations
        assert !(service instanceof OpenAIImageGenerationService);
        assert !(service instanceof SpringAIImageGenerationService);
    }

    @Test
    void getImageGenerationService_returnsOpenAIService_whenProviderIsUnknown() {
        // Arrange - set the provider to null directly on the real properties object
        properties.setProvider(null);

        // Act
        ImageGenerationService service = factory.getImageGenerationService();

        // Assert
        assertInstanceOf(OpenAIImageGenerationService.class, service);
    }
}
