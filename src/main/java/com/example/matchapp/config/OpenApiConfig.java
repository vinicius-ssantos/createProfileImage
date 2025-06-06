package com.example.matchapp.config;

import com.example.matchapp.exception.ErrorResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * Configuration for OpenAPI documentation.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * Creates a standardized API response definition.
     *
     * @param code the HTTP status code
     * @param title the response title
     * @param description the response description
     * @return the API response definition
     */
    private ApiResponse createApiResponse(String code, String title, String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType("application/json", 
                                new MediaType().schema(new Schema<ErrorResponse>()
                                        .$ref("#/components/schemas/ErrorResponse"))));
    }

    /**
     * Configures the OpenAPI documentation.
     *
     * @return the OpenAPI configuration
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Profile Management API")
                        .description("API for managing profiles and generating profile images using OpenAI")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080" + contextPath)
                                .description("Local server")
                ))
                .components(new Components()
                        .addResponses("BadRequest", createApiResponse("400", "Bad Request", "The request was invalid"))
                        .addResponses("NotFound", createApiResponse("404", "Not Found", "The requested resource was not found"))
                        .addResponses("InternalServerError", createApiResponse("500", "Internal Server Error", "An unexpected error occurred"))
                        .addSchemas("ErrorResponse", new Schema<ErrorResponse>()
                                .type("object")
                                .description("Standard error response")
                                .addProperty("status", new Schema<>().type("integer").description("HTTP status code"))
                                .addProperty("message", new Schema<>().type("string").description("Error message"))
                                .addProperty("errors", new Schema<>().type("object").description("Field-specific errors"))
                                .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("Error timestamp"))));
    }
}
