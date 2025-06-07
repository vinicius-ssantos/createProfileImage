# Performance Testing Documentation

This document provides information on how to run and interpret the performance tests for the create_ia_profiles application.

## Overview

The application includes two types of performance tests:

1. **Gatling Simulations**: For testing API endpoints under various load conditions
2. **JMeter Test Plans**: For load testing and performance measurement

These tests help ensure that the application can handle the expected load and identify potential performance bottlenecks.

## Running Performance Tests

### Running Gatling Tests

To run the Gatling performance tests:

```bash
# Run all Gatling simulations
mvn gatling:test

# Run a specific Gatling simulation
mvn gatling:test -Dgatling.simulationClass=com.example.matchapp.performance.ImageGenerationSimulation
```

### Running JMeter Tests

To run the JMeter performance tests:

```bash
# Run all JMeter test plans
mvn jmeter:jmeter

# Run a specific JMeter test plan
mvn jmeter:jmeter -Djmeter.test.file=image-generation-load-test.jmx
```

## Test Configurations

### Gatling Simulation

The `ImageGenerationSimulation` class tests the following scenarios:

1. **Generate Image for Profile**: Tests retrieving profiles and generating images
   - Load: 5 users over 30 seconds

2. **Create Profile with Image**: Tests creating profiles and generating images for them
   - Load: 3 users over 30 seconds

### JMeter Test Plan

The `image-generation-load-test.jmx` test plan includes:

1. **Generate Image API Test**:
   - Thread Group: 10 users, 30-second ramp-up, 5 iterations
   - Requests: Get all profiles, generate image for a profile

2. **Create Profile API Test**:
   - Thread Group: 5 users, 20-second ramp-up, 3 iterations
   - Requests: Create profile, generate image for the new profile

## Interpreting Results

### Gatling Results

Gatling generates HTML reports in the `target/gatling/results` directory. These reports include:

- **Global Information**: Overall statistics for the simulation
- **Response Time Distribution**: Distribution of response times
- **Response Time Percentiles**: Response time percentiles (50th, 75th, 95th, 99th)
- **Active Users**: Number of active users over time
- **Requests Per Second**: Number of requests per second over time

Key metrics to monitor:
- **Response Time**: Should be under 1 second for most operations
- **Error Rate**: Should be less than 1%
- **Throughput**: Should meet the expected load requirements

### JMeter Results

JMeter results are stored in the `target/jmeter/results` directory. The results include:

- **Summary Report**: Overall statistics for each request
- **Graph Results**: Visual representation of response times
- **View Results Tree**: Detailed information for each request

Key metrics to monitor:
- **Average Response Time**: Should be under 1 second for most operations
- **Error Rate**: Should be less than 1%
- **Throughput**: Should meet the expected load requirements

## Performance Thresholds

The application should meet the following performance thresholds:

- **API Endpoints**:
  - Average response time: < 500ms
  - 95th percentile response time: < 1000ms
  - Error rate: < 1%

- **Image Generation**:
  - Average response time: < 2000ms
  - 95th percentile response time: < 5000ms
  - Error rate: < 2%

## CI/CD Integration

Performance tests are automatically run as part of the CI/CD pipeline. The results are uploaded as artifacts and can be viewed in the GitHub Actions workflow.

If the performance tests fail to meet the defined thresholds, the pipeline will mark the job as failed, indicating a potential performance regression.

## Troubleshooting

If performance tests are failing, consider the following:

1. **Check Resource Usage**: Monitor CPU, memory, and network usage during tests
2. **Database Performance**: Check database query performance and connection pool settings
3. **External API Calls**: Verify that external API calls (e.g., OpenAI) are performing as expected
4. **Caching**: Ensure that caching mechanisms are working correctly
5. **Logging**: Reduce logging level during performance tests to minimize overhead

## Extending Performance Tests

To add new performance tests:

1. **Gatling**: Create a new simulation class in the `com.example.matchapp.performance` package
2. **JMeter**: Create a new test plan file in the `src/test/jmeter` directory

Ensure that new tests follow the same patterns and conventions as the existing tests.