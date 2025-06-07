package com.example.matchapp.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
import java.util.UUID;

/**
 * Gatling simulation for testing the performance of image generation endpoints.
 * This simulation tests the API endpoints under various load conditions.
 */
public class ImageGenerationSimulation extends Simulation {

    // Base URL for the application
    private static final String BASE_URL = "http://localhost:8080";

    // HTTP protocol configuration
    private final HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling/Performance-Test");

    // Common steps
    private ChainBuilder getProfileId = 
        exec(http("Get All Profiles")
            .get("/api/profiles")
            .check(status().is(200))
            .check(jsonPath("$[0].id").saveAs("profileId")));

    private ChainBuilder generateImage = 
        exec(http("Generate Image")
            .post("/api/images/generate/${profileId}")
            .check(status().is(200)));

    private ChainBuilder createProfile = 
        exec(session -> {
            String randomId = UUID.randomUUID().toString();
            return session
                .set("randomId", randomId)
                .set("firstName", "Test")
                .set("lastName", "User")
                .set("age", 30)
                .set("ethnicity", "Test Ethnicity")
                .set("gender", "FEMALE")
                .set("bio", "Test bio for performance testing");
        })
        .exec(http("Create Profile")
            .post("/api/profiles")
            .body(StringBody("""
                {
                    "id": "${randomId}",
                    "firstName": "${firstName}",
                    "lastName": "${lastName}",
                    "age": ${age},
                    "ethnicity": "${ethnicity}",
                    "gender": "${gender}",
                    "bio": "${bio}",
                    "personalityType": "INTJ"
                }
                """))
            .check(status().is(201))
            .check(jsonPath("$.id").saveAs("newProfileId")))
        .exec(http("Generate Image for New Profile")
            .post("/api/images/generate/${newProfileId}")
            .check(status().is(200)));

    // Scenarios
    private final ScenarioBuilder generateImageScenario = 
        scenario("Generate Image for Profile")
            .exec(getProfileId)
            .pause(1)
            .exec(generateImage);

    private final ScenarioBuilder createProfileScenario = 
        scenario("Create Profile with Image")
            .exec(createProfile);

    // Simulation setup
    {
        setUp(
            // Low load test - 5 users over 30 seconds
            generateImageScenario.injectOpen(
                rampUsers(5).during(Duration.ofSeconds(30))
            ).protocols(httpProtocol),

            // Create profile test - 3 users over 30 seconds
            createProfileScenario.injectOpen(
                rampUsers(3).during(Duration.ofSeconds(30))
            ).protocols(httpProtocol)
        );
    }
}
