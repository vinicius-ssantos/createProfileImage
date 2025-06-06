package com.example.matchapp;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.service.ProfileService;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = {"com.example.matchapp"})
@EnableConfigurationProperties(ImageGenProperties.class)
public class CreateIaProfilesApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CreateIaProfilesApplication.class);

    private final ProfileService profileService;

    public CreateIaProfilesApplication(ProfileService profileService) {
        this.profileService = profileService;
    }

    public static void main(String[] args) {
        checkAndCreateEnvFile();
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // Check if the API key is missing or using the default placeholder
        String apiKey = dotenv.get("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty() || "your_openai_key_here".equals(apiKey)) {
            logger.error("OPENAI_API_KEY environment variable is not set or is using the default placeholder value.");
            logger.error("The application will likely fail with a 401 Unauthorized error when trying to use the OpenAI API.");
            logger.error("Please follow these steps to set up your OpenAI API key:");
            logger.error("1. Go to https://platform.openai.com/api-keys to create an API key if you don't have one");
            logger.error("2. Copy your API key from the OpenAI dashboard");
            logger.error("3. Open the .env file in the project root directory");
            logger.error("4. Replace 'your_openai_key_here' with your actual API key");
            logger.error("5. Save the file and restart the application");

            // We'll still run the application, but it will likely fail with a more specific error message
            // from the OpenAIImageGenerationService
        }

        SpringApplication.run(CreateIaProfilesApplication.class, args);
    }

    private static void checkAndCreateEnvFile() {
        File envFile = new File(".env");
        File envExampleFile = new File(".env.exemplo");

        if (!envFile.exists() && envExampleFile.exists()) {
            try {
                logger.info("No .env file found. Creating one from .env.exemplo template.");
                Files.copy(envExampleFile.toPath(), envFile.toPath());
                logApiKeyInstructions();
            } catch (IOException e) {
                logger.error("Failed to create .env file from template", e);
            }
        } else if (!envFile.exists()) {
            try {
                logger.info("No .env file found. Creating a new one with instructions.");
                // Create a .env file with detailed instructions
                String envContent = 
                    "# OpenAI API Configuration\n" +
                    "# -----------------------\n" +
                    "# Replace 'your_openai_key_here' with your actual OpenAI API key\n" +
                    "# You can get an API key at: https://platform.openai.com/api-keys\n" +
                    "OPENAI_API_KEY=your_openai_key_here\n\n" +
                    "# OpenAI API Base URL (usually you don't need to change this)\n" +
                    "OPENAI_BASE_URL=https://api.openai.com/v1/images/generations\n";

                Files.writeString(envFile.toPath(), envContent);
                logApiKeyInstructions();
            } catch (IOException e) {
                logger.error("Failed to create .env file", e);
            }
        } else {
            // .env file exists, but let's check if it has a valid API key
            try {
                String content = Files.readString(envFile.toPath());
                if (content.contains("OPENAI_API_KEY=your_openai_key_here") || 
                    !content.contains("OPENAI_API_KEY=") || 
                    content.contains("OPENAI_API_KEY=") && content.split("OPENAI_API_KEY=")[1].split("\n")[0].trim().isEmpty()) {

                    logger.warn(".env file exists but appears to have an invalid or missing API key.");
                    logApiKeyInstructions();
                }
            } catch (IOException e) {
                logger.error("Failed to read .env file", e);
            }
        }
    }

    private static void logApiKeyInstructions() {
        logger.info(".env file created. Please follow these steps to set up your OpenAI API key:");
        logger.info("1. Go to https://platform.openai.com/api-keys to create an API key if you don't have one");
        logger.info("2. Copy your API key from the OpenAI dashboard");
        logger.info("3. Open the .env file in the project root directory");
        logger.info("4. Replace 'your_openai_key_here' with your actual API key");
        logger.info("5. Save the file and restart the application");
    }

    @Override
    public void run(String... args) throws Exception {
        profileService.generateImages(Paths.get("src/main/resources/static/images"));
    }
}
