# Development Guidelines for create_ia_profiles

This document provides essential information for developers working on the create_ia_profiles project.

## Build and Configuration Instructions

### Prerequisites
- Java 21 or higher
- Maven 3.8+ (or use the included Maven wrapper)
- OpenAI API key

### Environment Setup
1. Create a `.env` file in the project root with the following variables:
   ```
   OPENAI_API_KEY=your_openai_api_key
   OPENAI_BASE_URL=https://api.openai.com/v1/images/generations
   ```
   
   Note: You can use the `.env.exemplo` file as a template.

2. The application uses the `dotenv-java` library to load these environment variables at runtime.

### Building the Project
```bash
# Using Maven
mvn clean install

# Using Maven wrapper
./mvnw clean install  # Unix/Linux/macOS
mvnw.cmd clean install  # Windows
```

### Running the Application
```bash
# Using Maven
mvn spring-boot:run

# Using Maven wrapper
./mvnw spring-boot:run  # Unix/Linux/macOS
mvnw.cmd spring-boot:run  # Windows
```

The application will:
1. Load profile data from `src/main/resources/profile.json`
2. Generate images for each profile using OpenAI's API
3. Save the images to `src/main/resources/static/images`
4. Create a `profiles_with_images.json` file with updated profile data

## Testing Information

### Running Tests
```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ProfileServiceTest

# Run a specific test method
mvn test -Dtest=ProfileServiceTest#generatesImages
```

### Test Structure
The project uses:
- JUnit 5 for test frameworks
- Mockito for mocking dependencies
- Spring Boot Test for integration testing

### Writing Tests

#### Unit Tests
For unit tests, follow these guidelines:
1. Use the Arrange-Act-Assert pattern
2. Mock external dependencies
3. Test one behavior per test method
4. Use descriptive test method names (e.g., `generateImage_returnsImageBytes`)

Example unit test for a service:

```java
@Test
void generateImage_returnsImageBytes() {
    // Arrange
    Profile profile = new Profile(
        "test-id", 
        "Test", 
        "User", 
        30, 
        "Test Ethnicity", 
        "MALE", 
        "Test bio for image generation", 
        "test.jpg", 
        "INTJ"
    );
    
    // Act
    byte[] result = service.generateImage(profile);
    
    // Assert
    assertNotNull(result);
}
```

#### Testing Services with External Dependencies
For services that make API calls (like OpenAIImageGenerationService), you can:

1. Create a test subclass that overrides methods making external calls:

```java
private static class TestOpenAIImageGenerationService extends OpenAIImageGenerationService {
    public TestOpenAIImageGenerationService(ImageGenProperties properties) {
        super(properties);
    }
    
    @Override
    public byte[] generateImage(Profile profile) {
        // Return dummy image data
        return new byte[] {1, 2, 3, 4, 5};
    }
}
```

2. Use this test implementation in your tests:

```java
@BeforeEach
void setUp() {
    // Create a real properties object
    properties = new ImageGenProperties();
    properties.setApiKey("test-api-key");
    properties.setBaseUrl("https://test-url.com");
    
    // Create a test implementation that doesn't make real API calls
    service = new TestOpenAIImageGenerationService(properties);
}
```

#### Integration Tests
For integration tests:
1. Use `@SpringBootTest` to load the application context
2. Configure test properties as needed
3. Test the interaction between components

Example:
```java
@SpringBootTest
class CreateIaProfilesApplicationTests {
    @Test
    void contextLoads() {
        // Verifies that the Spring context loads successfully
    }
}
```

## Additional Development Information

### Project Structure
- `com.example.matchapp.model`: Contains data models (e.g., Profile)
- `com.example.matchapp.service`: Contains service interfaces and implementations
- `com.example.matchapp.config`: Contains configuration classes
- `io.deviceid.create_ia_profiles`: Contains the main application class

### Key Components
1. **Profile**: Data model representing a user profile
2. **ProfileService**: Service for loading profiles and generating images
3. **ImageGenerationService**: Interface for image generation
4. **OpenAIImageGenerationService**: Implementation using OpenAI's API
5. **ImageGenProperties**: Configuration properties for image generation

### Working with OpenAI API
The application uses OpenAI's image generation API:
- The profile's bio is used as the prompt for image generation
- Images are generated at 1024x1024 resolution
- The API response is received as base64-encoded data and decoded to bytes

### Logging
The application uses SLF4J for logging:
- MDC (Mapped Diagnostic Context) is used to include the profile ID in log messages
- Log messages include operation information (e.g., "Requesting image generation")

### Error Handling
The application includes basic error handling:
- Null responses from the API are handled with appropriate exceptions
- Empty data responses are checked and handled

### Code Style
- Follow standard Java conventions
- Use meaningful variable and method names
- Include comments for complex logic
- Use Java records for immutable data models
- Follow the Spring Boot conventions for service and configuration classes