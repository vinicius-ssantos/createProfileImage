# Logstash Configuration Fix

## Issue

During test execution, the application was attempting to connect to a Logstash server at `localhost:5044`, resulting in connection errors:

```
17:50:26,560 |-WARN in net.logstash.logback.appender.LogstashTcpSocketAppender[LOGSTASH] - Log destination localhost/<unresolved>:5044: connection failed. java.net.ConnectException: Connection refused: getsockopt
```

These errors occurred because:
1. The application is configured to use Logstash for centralized logging
2. During tests, there is no Logstash server running at the specified address
3. The Logstash appender was included in all Spring profiles, including the test profile

## Solution

The solution was to modify the `logback-spring.xml` configuration to exclude the Logstash appender from the test profile:

1. Kept the Logstash appender configuration for dev and prod profiles
2. Removed the Logstash appender reference from the test profile

### Changes Made

In `src/main/resources/logback-spring.xml`:

```xml
<springProfile name="test">
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
        <appender-ref ref="ERROR_FILE" />
        <!-- Logstash appender is intentionally omitted for tests -->
    </root>
    <logger name="com.example.matchapp" level="INFO" />
    <logger name="org.springframework" level="WARN" />
    <logger name="net.logstash.logback" level="INFO" />
</springProfile>
```

## Alternative Approaches Considered

1. **Conditional Logstash Appender**: Initially attempted to use conditional logic with `<if>` tags in the Logback configuration, but this requires the Janino library which wasn't included in the project dependencies.

2. **Environment Variable Control**: Added an `ENABLE_LOGSTASH` property to control whether the Logstash appender should be used, but this also required conditional logic in the configuration.

3. **NOP Appender**: Tried using a `NOPAppender` as a no-op replacement for the Logstash appender, but the specific class wasn't available in the Logback version being used.

## Benefits

- Tests now run without Logstash connection errors
- No additional dependencies required
- Simple solution that maintains the existing logging configuration for dev and prod environments
- Clear documentation of the intentional omission in the configuration file

## Future Considerations

If Logstash integration during tests becomes necessary in the future, consider:

1. Adding the Janino library as a dependency to enable conditional logic in the Logback configuration
2. Setting up a test Logstash server or mock for integration tests
3. Implementing a custom appender that can be conditionally enabled/disabled at runtime