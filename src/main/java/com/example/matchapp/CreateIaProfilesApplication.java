package com.example.matchapp;

import com.example.matchapp.config.EnvFileLoader;
import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.nio.file.Paths;
import java.util.Map;

@SpringBootApplication(scanBasePackages = {"com.example.matchapp"})
@EnableConfigurationProperties(ImageGenProperties.class)
public class CreateIaProfilesApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CreateIaProfilesApplication.class);

    private final ProfileService profileService;
    private final EnvFileLoader envFileLoader;

    public CreateIaProfilesApplication(ProfileService profileService, EnvFileLoader envFileLoader) {
        this.profileService = profileService;
        this.envFileLoader = envFileLoader;
    }

    public static void main(String[] args) {
        SpringApplication.run(CreateIaProfilesApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Load environment variables from .env file
        Map<String, String> envVars = envFileLoader.loadEnvFile();

        // Check if the API key is missing or using the default placeholder
        String apiKey = envVars.get("OPENAI_API_KEY");
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

        profileService.generateImages(Paths.get("src/main/resources/static/images"));
    }
}
