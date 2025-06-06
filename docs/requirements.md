# Project Requirements: Profile Image Generator

## Overview
This document outlines the requirements for the Profile Image Generator project, which generates realistic portrait images for user profiles using the OpenAI API.

## Technical Requirements

### Technology Stack
- Java 17 or higher
- Spring Boot 3
- Maven 3
- OpenAI API integration

### Architecture and Design Principles
- Follow SOLID principles, particularly interface segregation and dependency inversion
- Implement clean code practices
- Use immutable DTOs
- Structured logging with SLF4J + Logback
- Proper resource management with try-with-resources

## Functional Requirements

### Core Functionality
1. Load profile data from a JSON file
2. Generate realistic portrait images for each profile using OpenAI's image generation API
3. Save generated images to a specified directory
4. Create an updated JSON file with image generation status

### Profile Processing
- Each profile in the input JSON must be processed
- The system must generate one image per profile
- Images must be saved with the filename specified in the profile's `imageUrl` field
- The output JSON must include all original profile data plus an `imageGenerated` flag set to true

### Image Generation Rules
1. **Appearance**: Combine ethnicity and age for realistic facial traits and skin tone
2. **Expression & Posture**: Reflect personality traits from bio and Myers-Briggs type
3. **Scenario**: Include subtle background related to hobby/profession
4. **Lighting**: Use natural soft light or studio soft-box as appropriate
5. **Clothing**: Elegant casual attire consistent with the profile description
6. **Technical Specifications**: 1024×1024 px, 1:1 ratio, JPEG format, natural colors, no watermarks
7. **Filename**: Use exactly the value from the `imageUrl` field

## Non-Functional Requirements

### Security
- API keys must be stored in environment variables, not hardcoded
- Use a .env file for local development (not committed to version control)

### Performance
- Efficient handling of API calls
- Proper error handling and retries for API failures

### Maintainability
- Well-documented code
- Comprehensive test coverage
- Separation of concerns through proper service interfaces

### Ethical Considerations
- Generated images must not resemble real individuals
- No sensitive, violent, or sexual content
- Respectful representation of diversity

## Integration Requirements

### OpenAI API Integration
- Use the OpenAI API for image generation
- Support configuration of API endpoint and authentication
- Handle API responses and errors appropriately

### Configuration
- Support configuration through application.properties and environment variables
- Allow customization of API endpoints and timeouts