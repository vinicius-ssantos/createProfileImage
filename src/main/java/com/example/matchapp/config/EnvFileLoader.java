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
 */
@Component
public class EnvFileLoader {
    private static final Logger logger = LoggerFactory.getLogger(EnvFileLoader.class);
    private static final String ENV_FILE_PATH = ".env";
    private static final Pattern ENV_ENTRY_PATTERN = Pattern.compile("^\\s*([\\w.-]+)\\s*=\\s*(.*)\\s*$");

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
        File envFile = new File(ENV_FILE_PATH);
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

    /**
     * Reads the .env file and returns a map of environment variables.
     * 
     * @return A map of environment variables
     */
    private Map<String, String> readEnvFile() {
        Map<String, String> envVars = new HashMap<>();
        File envFile = new File(ENV_FILE_PATH);
        
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