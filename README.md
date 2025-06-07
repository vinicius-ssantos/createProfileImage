# Profile Management API

This project provides a RESTful API for managing user profiles and generating profile images using OpenAI's API. It was originally a command-line application and has been restructured to function entirely as an API.

## Features

- CRUD operations for user profiles
- Image generation for profiles using OpenAI's API
- Validation of input data
- API documentation with Swagger UI

## Getting Started

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
   This file should **not** be committed to version control. The project
   `.gitignore` already excludes `.env` to keep your API key private.

### Obtaining an OpenAI API Key

1. Visit [OpenAI API Keys](https://platform.openai.com/api-keys)
2. Log in or create an account
3. Create a new API key
4. Copy the key and add it to your `.env` file

### Troubleshooting

If you encounter a "401 Unauthorized" error when running the application, check:

1. If the `.env` file exists in the project root
2. If the `OPENAI_API_KEY` variable is correctly set in the `.env` file
3. If the API key is valid and has not expired
4. If your OpenAI account has sufficient credits

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

The application will start on port 8080 with the context path `/api`.

## API Documentation

The API documentation is available at:
- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/api-docs

## API Endpoints

### Profiles

- `GET /api/profiles`: Get all profiles
- `GET /api/profiles/{id}`: Get a profile by ID
- `POST /api/profiles`: Create a new profile
- `PUT /api/profiles/{id}`: Update an existing profile
- `DELETE /api/profiles/{id}`: Delete a profile by ID
- `POST /api/profiles/{id}/generate-image`: Generate an image for a profile
- `POST /api/profiles/generate-images`: Generate images for all profiles

### Images

- `POST /api/images/generate`: Generate an image based on a profile

## Example Requests

### Create a Profile

```bash
curl -X POST http://localhost:8080/api/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "age": 30,
    "ethnicity": "White",
    "gender": "MALE", // MALE, FEMALE, or OTHER
    "bio": "Software developer with a passion for AI",
    "myersBriggsPersonalityType": "INTJ"
  }'
```

### Generate an Image for a Profile

```bash
curl -X POST http://localhost:8080/api/profiles/{id}/generate-image
```

## Error Handling

The API returns appropriate HTTP status codes and error messages:

- `400 Bad Request`: Invalid input data
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

## Testing

Run all tests with:

```bash
# Using Maven
mvn test

# Using Maven wrapper
./mvnw test  # Unix/Linux/macOS
mvnw.cmd test  # Windows
```

## Example HTTP Request

The `openai_requests.http` file contains an example of calling the image generation API using the configured variables.

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
