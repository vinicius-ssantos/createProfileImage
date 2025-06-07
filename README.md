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
   # Environment Configuration
   ENVIRONMENT=dev  # Options: dev, test, prod

   # OpenAI API Configuration
   OPENAI_API_KEY=your_openai_api_key
   OPENAI_BASE_URL=https://api.openai.com/v1/images/generations
   ```

2. The application supports three environments:
   - `dev`: Development environment with verbose logging and development-specific settings
   - `test`: Testing environment with minimal logging and test-specific settings
   - `prod`: Production environment with minimal logging and production-specific settings

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
    "gender": "MALE",
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

## CI/CD Pipeline

This project uses GitHub Actions for continuous integration and deployment.

### CI Pipeline

The CI pipeline runs on every push to the main/master branch and on pull requests. It performs the following steps:

1. Builds the application
2. Runs all tests
3. Performs code quality checks (SpotBugs, PMD, SonarCloud)

### CD Pipeline

The CD pipeline runs when a new version tag (v*) is pushed or manually triggered. It performs the following steps:

1. Builds the application
2. Runs all tests
3. Creates a deployable JAR artifact
4. Builds and pushes a Docker image to GitHub Container Registry
5. Deploys to staging environment
6. Deploys to production environment (if manually selected or after staging)

### Docker Containerization

The application is containerized using Docker. The Docker image is built and pushed to GitHub Container Registry during the CD pipeline.

To run the Docker image locally:

```bash
# Pull the image
docker pull ghcr.io/your-username/create_ia_profiles:latest

# Run the container
docker run -d \
  --name create-ia-profiles \
  -p 8080:8080 \
  -e ENVIRONMENT=prod \
  -e OPENAI_API_KEY=your_openai_api_key \
  -e OPENAI_BASE_URL=https://api.openai.com/v1/images/generations \
  ghcr.io/your-username/create_ia_profiles:latest
```

Replace `your-username` with your GitHub username and `your_openai_api_key` with your OpenAI API key.

### Setting Up GitHub Secrets

To use the CI/CD pipeline, you need to set up the following secrets in your GitHub repository:

1. `OPENAI_API_KEY`: Your OpenAI API key for running tests
2. `SONAR_TOKEN`: Your SonarCloud token for code quality analysis (optional)

To add these secrets:
1. Go to your GitHub repository
2. Click on "Settings" > "Secrets and variables" > "Actions"
3. Click "New repository secret"
4. Add each secret with its name and value

### Environment Configuration

#### Application Environments

The application supports three environments: `dev`, `test`, and `prod`. Each environment has its own configuration settings defined in the corresponding `application-{env}.properties` file.

To specify which environment to use:

1. Set the `ENVIRONMENT` variable in your `.env` file:
   ```
   ENVIRONMENT=dev  # Options: dev, test, prod
   ```

2. Or set it as a system environment variable:
   ```bash
   # Linux/macOS
   export ENVIRONMENT=prod

   # Windows (Command Prompt)
   set ENVIRONMENT=prod

   # Windows (PowerShell)
   $env:ENVIRONMENT = "prod"
   ```

3. Or pass it as a command-line argument when running the application:
   ```bash
   mvn spring-boot:run -Dspring.profiles.active=prod
   ```

#### Environment-Specific Settings

Each environment has different settings optimized for its purpose:

| Setting | Dev | Test | Prod |
|---------|-----|------|------|
| Logging Level | DEBUG | INFO | WARN |
| OpenAI Model | dall-e-2 | dall-e-2 | dall-e-3 |
| Image Size | 512x512 | 256x256 | 1024x1024 |
| Error Details | Full | Partial | Minimal |
| Swagger UI | Enabled | Enabled | Disabled |

#### CI/CD Environments

For deployment, the CD pipeline uses GitHub Environments for staging and production. To set these up:

1. Go to your GitHub repository
2. Click on "Settings" > "Environments"
3. Create "staging" and "production" environments
4. Add environment-specific secrets if needed
5. Configure environment protection rules if desired (e.g., required reviewers)

## Example HTTP Request

The `openai_requests.http` file contains an example of calling the image generation API using the configured variables.

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
