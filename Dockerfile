FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user to run the application
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy the JAR file
COPY target/*.jar app.jar

# Environment variables
ENV ENVIRONMENT="prod"
ENV OPENAI_API_KEY=""
ENV OPENAI_BASE_URL="https://api.openai.com/v1/images/generations"

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
