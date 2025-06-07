# Centralized Logging Setup

This document describes the centralized logging setup for the create_ia_profiles application.

## Overview

The application uses a centralized logging system based on the ELK stack (Elasticsearch, Logstash, Kibana) to collect, process, and visualize logs from all environments. This setup provides:

- Real-time log aggregation across all application instances
- Structured logging with consistent format
- Request tracking across components using MDC (Mapped Diagnostic Context)
- Searchable and filterable logs through Kibana
- Performance metrics and error monitoring

## Configuration

### Logback Configuration

The application uses Logback as its logging framework, configured in `src/main/resources/logback-spring.xml`. The configuration includes:

1. **Console Appender**: Outputs logs to the console
2. **File Appender**: Writes logs to `logs/application.log` with rotation
3. **Error File Appender**: Writes error logs to `logs/error.log` with rotation
4. **Logstash Appender**: Sends logs to a centralized Logstash server

The Logstash appender is wrapped in an AsyncAppender to ensure that logging doesn't impact application performance.

### Environment Variables

The following environment variables can be used to configure the centralized logging:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| LOG_PATH | Directory for log files | ./logs |
| LOGSTASH_HOST | Hostname of the Logstash server | localhost |
| LOGSTASH_PORT | Port of the Logstash server | 5044 |
| APP_NAME | Application name for log identification | create_ia_profiles |
| APP_ENV | Environment name (dev, test, prod) | dev |

### Profile-Specific Configuration

Different logging levels are configured for each Spring profile:

- **dev**: DEBUG level for application code, INFO for Spring framework
- **test**: INFO level for application code, WARN for Spring framework
- **prod**: WARN level for application code, ERROR for Spring framework

## MDC (Mapped Diagnostic Context)

The application uses MDC to track context information across log messages:

- **requestId**: Unique identifier for each HTTP request
- **profileId**: Identifier for the profile being processed

These values are automatically included in all log messages and sent to the centralized logging system.

## Utility Class

The `LoggingUtils` class provides methods for working with MDC:

- `setRequestId(String)`: Sets the request ID in MDC
- `getRequestId()`: Gets the current request ID from MDC
- `setProfileId(String)`: Sets the profile ID in MDC
- `getProfileId()`: Gets the current profile ID from MDC
- `clearMDC()`: Clears all MDC values
- `withMDC(Map, Supplier)`: Executes a function with temporary MDC values
- `withMDC(Map, Runnable)`: Executes a runnable with temporary MDC values
- `withProfileId(String, Supplier)`: Executes a function with a profile ID in MDC
- `withProfileId(String, Runnable)`: Executes a runnable with a profile ID in MDC

## Logstash Configuration

For the ELK stack setup, a Logstash configuration is needed to receive and process the logs. Here's a sample Logstash configuration:

```
input {
  tcp {
    port => 5044
    codec => json_lines
  }
}

filter {
  if [application] == "create_ia_profiles" {
    mutate {
      add_field => { "[@metadata][app]" => "create_ia_profiles" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "%{[@metadata][app]}-%{environment}-%{+YYYY.MM.dd}"
  }
}
```

## Best Practices

1. **Always use MDC for context**: When processing profiles or handling requests, ensure the appropriate context is set in MDC.
2. **Use structured logging**: Include relevant information in a structured format rather than embedding it in log messages.
3. **Log at appropriate levels**: Use DEBUG for detailed information, INFO for normal operation, WARN for potential issues, and ERROR for actual problems.
4. **Include request IDs in responses**: The application automatically adds the X-Request-ID header to responses, which can be used to correlate logs with specific requests.
5. **Monitor the logging system**: The application includes a logger for `net.logstash.logback` to monitor the centralized logging system itself.

## Troubleshooting

If logs are not appearing in the centralized system:

1. Check that the Logstash server is running and accessible
2. Verify the LOGSTASH_HOST and LOGSTASH_PORT environment variables
3. Check the application logs for any errors related to the Logstash connection
4. Ensure that the Logstash configuration is correctly set up to receive and process the logs
5. Check Elasticsearch to ensure indices are being created correctly