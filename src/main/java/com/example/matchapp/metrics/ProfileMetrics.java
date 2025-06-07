package com.example.matchapp.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Component for tracking metrics related to profile operations.
 * This class provides methods to record profile retrievals, creations, updates, and deletions.
 */
@Component
public class ProfileMetrics {

    private final Counter findAllCounter;
    private final Counter findByIdCounter;
    private final Counter saveCounter;
    private final Counter updateCounter;
    private final Counter deleteCounter;
    private final Counter notFoundCounter;
    private final Timer operationTimer;

    public ProfileMetrics(MeterRegistry registry) {
        // Initialize counters
        this.findAllCounter = Counter.builder("profile.operations.findAll")
                .description("Number of findAll operations on profiles")
                .register(registry);

        this.findByIdCounter = Counter.builder("profile.operations.findById")
                .description("Number of findById operations on profiles")
                .register(registry);

        this.saveCounter = Counter.builder("profile.operations.save")
                .description("Number of new profile creations")
                .register(registry);

        this.updateCounter = Counter.builder("profile.operations.update")
                .description("Number of profile updates")
                .register(registry);

        this.deleteCounter = Counter.builder("profile.operations.delete")
                .description("Number of profile deletions")
                .register(registry);

        this.notFoundCounter = Counter.builder("profile.operations.notFound")
                .description("Number of profile not found events")
                .register(registry);

        // Initialize timer
        this.operationTimer = Timer.builder("profile.operations.time")
                .description("Time taken for profile operations")
                .publishPercentiles(0.5, 0.95, 0.99) // Publish 50th, 95th, and 99th percentiles
                .publishPercentileHistogram()
                .register(registry);
    }

    /**
     * Records a findAll operation.
     */
    public void recordFindAll() {
        findAllCounter.increment();
    }

    /**
     * Records a findById operation.
     */
    public void recordFindById() {
        findByIdCounter.increment();
    }

    /**
     * Records a new profile creation.
     */
    public void recordSave() {
        saveCounter.increment();
    }

    /**
     * Records a profile update.
     */
    public void recordUpdate() {
        updateCounter.increment();
    }

    /**
     * Records a profile deletion.
     */
    public void recordDelete() {
        deleteCounter.increment();
    }

    /**
     * Records a profile not found event.
     */
    public void recordNotFound() {
        notFoundCounter.increment();
    }

    /**
     * Records the time taken for an operation.
     * 
     * @param timeMs The time taken in milliseconds
     */
    public void recordOperationTime(long timeMs) {
        operationTimer.record(timeMs, TimeUnit.MILLISECONDS);
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
        sample.stop(operationTimer);
    }

    /**
     * Gets the operation timer.
     * 
     * @return The operation timer
     */
    public Timer getOperationTimer() {
        return operationTimer;
    }
}