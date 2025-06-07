package com.example.matchapp.service;

/**
 * Interface for rate limiting service.
 * Implementations should provide mechanisms to limit the rate of operations
 * to prevent API quota exhaustion.
 * 
 * NOTE: This interface is currently not used in the application but is kept for future use
 * when more sophisticated rate limiting is needed. The current implementation uses
 * {@link RateLimiterService} instead.
 */
public interface RateLimiter {

    /**
     * Acquires a permit to perform an operation, blocking if necessary.
     * 
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    void acquire() throws InterruptedException;

    /**
     * Tries to acquire a permit to perform an operation without blocking.
     * 
     * @return true if a permit was acquired, false otherwise
     */
    boolean tryAcquire();

    /**
     * Gets the current rate limit in operations per minute.
     * 
     * @return the rate limit in operations per minute
     */
    int getRateLimit();

    /**
     * Gets the current number of available permits.
     * 
     * @return the number of available permits
     */
    int getAvailablePermits();
}
