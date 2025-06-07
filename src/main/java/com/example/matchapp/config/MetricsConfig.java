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
     * Configures JVM and system metrics to be collected.
     * 
     * @param registry The meter registry
     * @return The configured meter registry
     */
    @Bean
    public MeterRegistry configureMeterRegistry(MeterRegistry registry) {
        // JVM metrics
        new ClassLoaderMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        
        // System metrics
        new ProcessorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);
        
        return registry;
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
    
    /**
     * Creates a global meter registry that can be used throughout the application.
     * 
     * @return The global meter registry
     */
    @Bean
    public MeterRegistry meterRegistry() {
        return Metrics.globalRegistry;
    }
}