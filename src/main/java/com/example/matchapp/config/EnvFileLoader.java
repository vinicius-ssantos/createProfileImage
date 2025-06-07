package com.example.matchapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads environment variables from a .env file and sets them as system properties.
 * This is a replacement for the dotenv-java library.
 * 
 * This class also handles setting the active Spring profile based on the ENVIRONMENT variable.
 */
@Component
public class EnvFileLoader {
    private static final Logger logger = LoggerFactory.getLogger(EnvFileLoader.class);
    private static final String ENV_FILE_NAME = ".env";
    private static final Pattern ENV_ENTRY_PATTERN = Pattern.compile("^\\s*([\\w.-]+)\\s*=\\s*(.*)\\s*$");
    private static final String DEFAULT_ENVIRONMENT = "dev";
    private static final String[] VALID_ENVIRONMENTS = {"dev", "test", "prod"};

    @jakarta.annotation.PostConstruct
    public void init() {
        Map<String, String> envVars = loadEnvFile();
        setActiveProfile(envVars);
    }

    /**
     * Sets the active Spring profile based on the ENVIRONMENT variable.
     * If the ENVIRONMENT variable is not set or is invalid, defaults to "dev".
     * 
     * @param envVars The map of environment variables
     */
    private void setActiveProfile(Map<String, String> envVars) {
        String environment = envVars.getOrDefault("ENVIRONMENT", 
                                                 System.getenv("ENVIRONMENT"));

        if (environment == null || environment.trim().isEmpty()) {
            environment = DEFAULT_ENVIRONMENT;
            logger.info("No ENVIRONMENT variable found. Using default environment: {}", environment);
        } else {
            boolean isValid = false;
            for (String validEnv : VALID_ENVIRONMENTS) {
                if (validEnv.equalsIgnoreCase(environment)) {
                    environment = validEnv; // Ensure correct case
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                logger.warn("Invalid environment '{}' specified. Valid values are: {}. Using default: {}", 
                           environment, String.join(", ", VALID_ENVIRONMENTS), DEFAULT_ENVIRONMENT);
                environment = DEFAULT_ENVIRONMENT;
            }
        }

        // Set the spring.profiles.active property
        System.setProperty("spring.profiles.active", environment);
        logger.info("Active Spring profile set to: {}", environment);
    }

    /**
     * Loads environment variables from the .env file.
     * If the file doesn't exist, it creates one with default values.
     * 
     * @return A map of environment variables loaded from the .env file
     */
    public Map<String, String> loadEnvFile() {
        checkAndCreateEnvFile();
        return readEnvFile();
    }

    /**
     * Checks if the .env file exists and creates it if it doesn't.
     */
    private void checkAndCreateEnvFile() {
        String userDir = System.getProperty("user.dir");
        File envFile = new File(userDir, ENV_FILE_NAME);
        File envExampleFile = new File(userDir, ".env.exemplo");

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
                    "# Environment Configuration\n" +
                    "# -----------------------\n" +
                    "# Set the environment to one of: dev, test, prod\n" +
                    "# This determines which application-{profile}.properties file is used\n" +
                    "ENVIRONMENT=dev\n\n" +
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

    /**
     * Reads the .env file and returns a map of environment variables.
     * 
     * @return A map of environment variables
     */
    private Map<String, String> readEnvFile() {
        Map<String, String> envVars = new HashMap<>();
        String userDir = System.getProperty("user.dir");
        File envFile = new File(userDir, ENV_FILE_NAME);

        if (envFile.exists()) {
            try {
                String content = Files.readString(envFile.toPath());
                String[] lines = content.split("\n");

                for (String line : lines) {
                    // Skip comments and empty lines
                    if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                        continue;
                    }

                    Matcher matcher = ENV_ENTRY_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String key = matcher.group(1);
                        String value = matcher.group(2);

                        // Remove quotes if present
                        if (value.startsWith("\"") && value.endsWith("\"") || 
                            value.startsWith("'") && value.endsWith("'")) {
                            value = value.substring(1, value.length() - 1);
                        }

                        envVars.put(key, value);

                        // Set as system property so Spring can use it
                        System.setProperty(key, value);

                        // Map OpenAI environment variables to Spring properties
                        if (key.equals("OPENAI_API_KEY")) {
                            System.setProperty("imagegen.api-key", value);
                        } else if (key.equals("OPENAI_BASE_URL")) {
                            System.setProperty("imagegen.base-url", value);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to read .env file", e);
            }
        }

        return envVars;
    }

    /**
     * Logs instructions for setting up the OpenAI API key.
     */
    private void logApiKeyInstructions() {
        logger.info(".env file created. Please follow these steps to set up your OpenAI API key:");
        logger.info("1. Go to https://platform.openai.com/api-keys to create an API key if you don't have one");
        logger.info("2. Copy your API key from the OpenAI dashboard");
        logger.info("3. Open the .env file in the project root directory");
        logger.info("4. Replace 'your_openai_key_here' with your actual API key");
        logger.info("5. Save the file and restart the application");
    }
}
