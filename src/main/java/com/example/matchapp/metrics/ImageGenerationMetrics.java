package com.example.matchapp.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Component for tracking metrics related to image generation.
 * This class provides methods to record image generation requests, successes, failures, and response times.
 */
@Component
public class ImageGenerationMetrics {

    private final Counter totalRequestsCounter;
    private final Counter successCounter;
    private final Counter failureCounter;
    private final Counter apiErrorCounter;
    private final Counter cacheHitCounter;
    private final Timer responseTimeTimer;

    public ImageGenerationMetrics(MeterRegistry registry) {
        // Initialize counters
        this.totalRequestsCounter = Counter.builder("imagegen.requests.total")
                .description("Total number of image generation requests")
                .register(registry);

        this.successCounter = Counter.builder("imagegen.requests.success")
                .description("Number of successful image generation requests")
                .register(registry);

        this.failureCounter = Counter.builder("imagegen.requests.failure")
                .description("Number of failed image generation requests")
                .register(registry);

        this.apiErrorCounter = Counter.builder("imagegen.api.errors")
                .description("Number of OpenAI API errors")
                .register(registry);

        this.cacheHitCounter = Counter.builder("imagegen.cache.hits")
                .description("Number of cache hits for image generation")
                .register(registry);

        // Initialize timer
        this.responseTimeTimer = Timer.builder("imagegen.response.time")
                .description("Response time for image generation requests")
                .publishPercentiles(0.5, 0.95, 0.99) // Publish 50th, 95th, and 99th percentiles
                .publishPercentileHistogram()
                .register(registry);
    }

    /**
     * Records a new image generation request.
     */
    public void recordRequest() {
        totalRequestsCounter.increment();
    }

    /**
     * Records a successful image generation request.
     */
    public void recordSuccess() {
        successCounter.increment();
    }

    /**
     * Records a failed image generation request.
     */
    public void recordFailure() {
        failureCounter.increment();
    }

    /**
     * Records an API error.
     */
    public void recordApiError() {
        apiErrorCounter.increment();
    }

    /**
     * Records a cache hit for image generation.
     */
    public void recordCacheHit() {
        cacheHitCounter.increment();
    }

    /**
     * Records the response time for an image generation request.
     * 
     * @param timeMs The response time in milliseconds
     */
    public void recordResponseTime(long timeMs) {
        responseTimeTimer.record(timeMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a timer sample that can be used to measure the execution time of a block of code.
     * 
     * @return A timer sample
     */
    public Timer.Sample startTimer() {
        return Timer.start();
    }

    /**
     * Stops the timer sample and records the elapsed time.
     * 
     * @param sample The timer sample to stop
     */
    public void stopTimer(Timer.Sample sample) {
        sample.stop(responseTimeTimer);
    }

    /**
     * Gets the response time timer.
     * 
     * @return The response time timer
     */
    public Timer getResponseTimeTimer() {
        return responseTimeTimer;
    }
}
