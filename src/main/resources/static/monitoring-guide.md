# Health Checks and Monitoring Guide

This document provides information about the health checks and monitoring capabilities of the create_ia_profiles application.

## Health Checks

The application includes several health checks that provide information about the status of various components:

### Application Health

The `ApplicationHealthIndicator` provides overall application health status, including:
- JVM memory usage (heap and non-heap)
- Application uptime
- Thread counts
- Active profile

The application health status will be WARNING if heap memory usage exceeds 80%.

### Profile Repository Health

The `ProfileRepositoryHealthIndicator` checks the status of the profile repository:
- Verifies that the repository is accessible
- Checks if profiles are loaded
- Reports WARNING if the repository is empty

### OpenAI API Health

The `OpenAIHealthIndicator` checks the availability of the OpenAI API:
- Makes a lightweight API call to verify connectivity
- Reports different statuses based on the API response
- Handles authentication errors and rate limiting

### Disk Space Health

The `DiskSpaceHealthIndicator` monitors available disk space for image storage:
- Reports WARNING if free space is below 500 MB
- Reports DOWN if free space is below 100 MB
- Provides detailed information about total, free, and usable space

## Accessing Health Information

Health information is available through the Actuator endpoints:

- Basic health status: `GET /api/actuator/health`
- Detailed health status (requires authentication): `GET /api/actuator/health/detail`
- Component-specific health: `GET /api/actuator/health/{component}`

Example components: `application`, `profileRepository`, `openai`, `diskSpace`

## Monitoring Metrics

The application collects various metrics that can be used for monitoring:

### JVM Metrics

- Memory usage (heap and non-heap)
- Garbage collection statistics
- Thread counts
- Class loading statistics
- Processor and system metrics

### Image Generation Metrics

- Total requests
- Successful requests
- Failed requests
- API errors
- Response times (with percentiles)

### Profile Operation Metrics

- Repository operations (findAll, findById, save, delete)
- Operation response times
- Not found events

## Accessing Metrics

Metrics are available through the Actuator endpoints:

- List all available metrics: `GET /api/actuator/metrics`
- Get specific metric: `GET /api/actuator/metrics/{metric.name}`
- Prometheus format (for monitoring systems): `GET /api/actuator/prometheus`

Example metrics:
- `jvm.memory.used`
- `imagegen.requests.total`
- `profile.operations.findAll`
- `http.server.requests`

## Additional Monitoring Endpoints

The application exposes several other monitoring endpoints:

- Environment information: `GET /api/actuator/env`
- Logger configuration: `GET /api/actuator/loggers`
- Thread dump: `GET /api/actuator/threaddump`
- Heap dump: `GET /api/actuator/heapdump`
- Configuration properties: `GET /api/actuator/configprops`
- Request mappings: `GET /api/actuator/mappings`
- Scheduled tasks: `GET /api/actuator/scheduledtasks`

## Security

Most Actuator endpoints require authentication, except for:
- `/api/actuator/health/**` (basic health information)
- `/api/actuator/info` (application information)

To access protected endpoints, you need to authenticate with appropriate credentials.

## Integration with Monitoring Systems

The application can be integrated with various monitoring systems:

### Prometheus and Grafana

1. Configure Prometheus to scrape metrics from `/api/actuator/prometheus`
2. Set up Grafana dashboards to visualize the metrics

### Spring Boot Admin

The application can be monitored using Spring Boot Admin:

1. Set up a Spring Boot Admin server
2. Configure the application to register with the admin server

## Kubernetes Integration

The application includes Kubernetes-style health probes:

- Liveness probe: `GET /api/actuator/health/liveness`
- Readiness probe: `GET /api/actuator/health/readiness`

These can be used in Kubernetes deployments to ensure proper container lifecycle management.