# Deployment Guide for Profile Image Generator

This document provides comprehensive instructions for deploying the Profile Image Generator application in various environments.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Building the Application](#building-the-application)
4. [Deployment Options](#deployment-options)
   - [Local Deployment](#local-deployment)
   - [Docker Deployment](#docker-deployment)
   - [AWS Deployment](#aws-deployment)
5. [Configuration](#configuration)
6. [Monitoring and Logging](#monitoring-and-logging)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

Before deploying the application, ensure you have the following:

- Java 21 or higher
- Maven 3.8+ (or use the included Maven wrapper)
- OpenAI API key
- Docker (for containerized deployment)
- AWS CLI and Terraform (for AWS deployment)

## Environment Setup

### Environment Variables

The application requires the following environment variables:

1. Create a `.env` file in the project root with the following variables:
   ```
   # Environment Configuration
   ENVIRONMENT=dev  # Options: dev, test, prod

   # OpenAI API Configuration
   OPENAI_API_KEY=your_openai_api_key
   OPENAI_BASE_URL=https://api.openai.com/v1/images/generations
   IMAGEGEN_REQUESTS_PER_MINUTE=60
   ```

   Note: You can use the `.env.exemplo` file as a template.

2. The application uses the `dotenv-java` library to load these environment variables at runtime.

### Environment Profiles

The application supports three environments:

1. **Development (dev)**:
   - Verbose logging (DEBUG level)
   - Uses dall-e-2 model
   - 512x512 image size
   - Full error details
   - Swagger UI enabled

2. **Testing (test)**:
   - Moderate logging (INFO level)
   - Uses dall-e-2 model
   - 256x256 image size
   - Partial error details
   - Swagger UI enabled

3. **Production (prod)**:
   - Minimal logging (WARN level)
   - Uses dall-e-3 model
   - 1024x1024 image size
   - Minimal error details
   - Swagger UI disabled

## Building the Application

### Using Maven

```bash
# Using Maven
mvn clean install

# Using Maven wrapper
./mvnw clean install  # Unix/Linux/macOS
mvnw.cmd clean install  # Windows
```

The build process will:
1. Compile the source code
2. Run tests
3. Package the application into a JAR file in the `target` directory

### Building a Docker Image

```bash
# Build the Docker image
docker build -t profile-image-generator:latest .
```

## Deployment Options

### Local Deployment

#### Running with Java

```bash
# Using Maven
mvn spring-boot:run

# Using Maven wrapper
./mvnw spring-boot:run  # Unix/Linux/macOS
mvnw.cmd spring-boot:run  # Windows

# Using Java directly
java -jar target/create_ia_profiles-0.0.1-SNAPSHOT.jar
```

#### Setting the Environment Profile

```bash
# Using Maven
mvn spring-boot:run -Dspring.profiles.active=prod

# Using Java directly
java -jar target/create_ia_profiles-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Deployment

#### Running with Docker

```bash
# Run the Docker container
docker run -d \
  --name profile-image-generator \
  -p 8080:8080 \
  -e ENVIRONMENT=prod \
  -e OPENAI_API_KEY=your_openai_api_key \
  -e OPENAI_BASE_URL=https://api.openai.com/v1/images/generations \
  profile-image-generator:latest
```

#### Using Docker Compose

Create a `docker-compose.yml` file:

```yaml
version: '3.8'
services:
  app:
    image: profile-image-generator:latest
    ports:
      - "8080:8080"
    environment:
      - ENVIRONMENT=prod
      - OPENAI_API_KEY=your_openai_api_key
      - OPENAI_BASE_URL=https://api.openai.com/v1/images/generations
      - IMAGEGEN_REQUESTS_PER_MINUTE=60
    volumes:
      - ./images:/app/images
```

Run with:

```bash
docker-compose up -d
```

### AWS Deployment

The application can be deployed to AWS using Terraform. The Terraform configuration in the `terraform` directory sets up:

- ECS Fargate cluster
- Application Load Balancer
- Auto-scaling
- CloudWatch logging
- Secrets Manager for the OpenAI API key

#### Prerequisites for AWS Deployment

- AWS CLI configured with appropriate credentials
- Terraform installed
- VPC, subnets, and other network infrastructure already set up

#### Deployment Steps

1. Initialize Terraform:
   ```bash
   cd terraform
   terraform init
   ```

2. Create a `terraform.tfvars` file with your specific variables:
   ```
   app_name = "profile-image-generator"
   environment = "prod"
   aws_region = "us-west-2"
   vpc_id = "vpc-12345678"
   private_subnet_ids = ["subnet-12345678", "subnet-87654321"]
   public_subnet_ids = ["subnet-abcdefgh", "subnet-hgfedcba"]
   ecr_repository_url = "123456789012.dkr.ecr.us-west-2.amazonaws.com/profile-image-generator"
   app_version = "latest"
   certificate_arn = "arn:aws:acm:us-west-2:123456789012:certificate/abcdef-12345-67890"
   ```

3. Plan the deployment:
   ```bash
   terraform plan -out=tfplan
   ```

4. Apply the deployment:
   ```bash
   terraform apply tfplan
   ```

5. Store the OpenAI API key in AWS Secrets Manager:
   ```bash
   aws secretsmanager put-secret-value \
     --secret-id profile-image-generator/prod/openai-api-key \
     --secret-string "your_openai_api_key"
   ```

## Configuration

### Application Properties

The application uses the following property files:

- `application.properties`: Common settings for all environments
- `application-dev.properties`: Development environment settings
- `application-test.properties`: Testing environment settings
- `application-prod.properties`: Production environment settings

### Key Configuration Parameters

| Parameter | Description | Default Value |
|-----------|-------------|---------------|
| `imagegen.provider` | Image generation provider (OPENAI or SPRING_AI) | OPENAI |
| `imagegen.api-key` | OpenAI API key | From environment variable |
| `imagegen.base-url` | OpenAI API base URL | https://api.openai.com/v1/images/generations |
| `imagegen.model` | OpenAI model to use | dall-e-3 (prod), dall-e-2 (dev/test) |
| `imagegen.image-size` | Image size | 1024x1024 (prod), 512x512 (dev), 256x256 (test) |
| `imagegen.max-retries` | Maximum number of retries for API calls | 5 (prod), 3 (dev/test) |
| `imagegen.retry-delay` | Delay between retries in milliseconds | 2000 (prod), 1000 (dev/test) |

## Monitoring and Logging

### Logging

The application uses SLF4J with Logback for logging. Log levels are configured per environment:

- Development: DEBUG
- Testing: INFO
- Production: WARN

### Health Checks

The application provides health endpoints via Spring Boot Actuator:

- Health endpoint: `http://localhost:8080/api/actuator/health`
- Liveness probe: `http://localhost:8080/api/actuator/health/liveness`
- Readiness probe: `http://localhost:8080/api/actuator/health/readiness`

### Metrics

The application exposes metrics via Spring Boot Actuator:

- Metrics endpoint: `http://localhost:8080/api/actuator/metrics`
- Prometheus endpoint: `http://localhost:8080/api/actuator/prometheus`

## Troubleshooting

### Common Issues

#### API Authentication Errors

If you encounter a "401 Unauthorized" error when running the application:

1. Check if the `.env` file exists in the project root
2. Verify that the `OPENAI_API_KEY` variable is correctly set in the `.env` file
3. Ensure the API key is valid and has not expired
4. Check if your OpenAI account has sufficient credits

#### Connection Issues

If the application cannot connect to the OpenAI API:

1. Check your internet connection
2. Verify the `OPENAI_BASE_URL` is correct
3. Check if the OpenAI API is experiencing downtime
4. Ensure your firewall or proxy settings allow outbound connections to the API

#### Image Generation Failures

If images fail to generate:

1. Check the application logs for specific error messages
2. Verify that the profile data is valid and complete
3. Ensure the OpenAI model specified is available to your API key
4. Check if you've exceeded your API rate limits

#### AWS Deployment Issues

If the AWS deployment fails:

1. Check the Terraform output for specific error messages
2. Verify that your AWS credentials have the necessary permissions
3. Ensure the VPC, subnets, and other network resources exist and are correctly configured
4. Check if the ECR repository exists and contains the application image
5. Verify that the SSL certificate ARN is valid and in the correct region