package com.example.matchapp.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for metrics collection using Micrometer.
 * This class sets up JVM metrics and enables the @Timed annotation for method-level metrics.
 */
@Configuration
public class MetricsConfig {

    /**
     * Creates JVM metrics binders.
     * This approach avoids circular dependencies by not directly referencing the MeterRegistry.
     * 
     * @return ClassLoaderMetrics instance
     */
    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    /**
     * Creates JVM memory metrics binder.
     * 
     * @return JvmMemoryMetrics instance
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * Creates JVM garbage collection metrics binder.
     * 
     * @return JvmGcMetrics instance
     */
    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * Creates JVM thread metrics binder.
     * 
     * @return JvmThreadMetrics instance
     */
    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * Creates processor metrics binder.
     * 
     * @return ProcessorMetrics instance
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Creates uptime metrics binder.
     * 
     * @return UptimeMetrics instance
     */
    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    /**
     * Enables the @Timed annotation for method-level metrics.
     * This allows us to measure execution time of methods by adding the @Timed annotation.
     * 
     * @param registry The meter registry
     * @return The timed aspect
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    // The MeterRegistry bean is automatically provided by Spring Boot's auto-configuration
    // We don't need to explicitly define it here to avoid circular references
}
