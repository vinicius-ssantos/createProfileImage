package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.service.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of RateLimiter using a token bucket algorithm.
 * This implementation limits the rate of operations to prevent API quota exhaustion.
 * 
 * NOTE: This class is currently not used in the application but is kept for future use
 * when more sophisticated rate limiting is needed. The current implementation uses
 * {@link InMemoryRateLimiterService} instead.
 */
@Service
public class TokenBucketRateLimiter implements RateLimiter {
    private static final Logger logger = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

    private final int rateLimit; // Requests per minute
    private final int burstCapacity; // Maximum number of concurrent requests
    private final AtomicInteger availableTokens;
    private final Semaphore semaphore;
    private final Lock refillLock = new ReentrantLock();
    private long lastRefillTimestamp;

    /**
     * Creates a new TokenBucketRateLimiter with the specified rate limit and burst capacity.
     * 
     * @param properties the image generation properties containing rate limiting configuration
     */
    public TokenBucketRateLimiter(@Qualifier("imageGenProperties") ImageGenProperties properties) {
        this.rateLimit = properties.getRequestsPerMinute();
        this.burstCapacity = properties.getBurstCapacity();
        this.availableTokens = new AtomicInteger(burstCapacity);
        this.semaphore = new Semaphore(burstCapacity, true);
        this.lastRefillTimestamp = System.currentTimeMillis();

        logger.info("Initialized rate limiter with {} requests per minute and burst capacity of {}", 
                rateLimit, burstCapacity);

        // Start a background thread to refill tokens
        Thread refillThread = new Thread(this::refillTokens);
        refillThread.setDaemon(true);
        refillThread.setName("rate-limiter-refill");
        refillThread.start();
    }

    /**
     * Refills tokens at a fixed rate based on the configured rate limit.
     */
    private void refillTokens() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Sleep for a short interval
                TimeUnit.SECONDS.sleep(1);

                refillLock.lock();
                try {
                    long now = System.currentTimeMillis();
                    long timePassed = now - lastRefillTimestamp;

                    // Calculate how many tokens to add based on time passed
                    double tokensToAdd = (timePassed / 1000.0) * (rateLimit / 60.0);

                    if (tokensToAdd >= 1) {
                        int newTokens = (int) tokensToAdd;
                        int currentTokens = availableTokens.get();
                        int tokensAfterRefill = Math.min(currentTokens + newTokens, burstCapacity);

                        // Release additional permits to the semaphore
                        int permitsToRelease = tokensAfterRefill - currentTokens;
                        if (permitsToRelease > 0) {
                            semaphore.release(permitsToRelease);
                            availableTokens.set(tokensAfterRefill);
                            logger.debug("Refilled {} tokens, now have {}/{}", 
                                    permitsToRelease, tokensAfterRefill, burstCapacity);
                        }

                        lastRefillTimestamp = now;
                    }
                } finally {
                    refillLock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Token refill thread interrupted", e);
                break;
            }
        }
    }

    @Override
    public void acquire() throws InterruptedException {
        logger.debug("Attempting to acquire permit, available: {}/{}", 
                availableTokens.get(), burstCapacity);

        semaphore.acquire();
        availableTokens.decrementAndGet();

        logger.debug("Permit acquired, remaining: {}/{}", 
                availableTokens.get(), burstCapacity);
    }

    @Override
    public boolean tryAcquire() {
        boolean acquired = semaphore.tryAcquire();
        if (acquired) {
            availableTokens.decrementAndGet();
            logger.debug("Permit acquired (non-blocking), remaining: {}/{}", 
                    availableTokens.get(), burstCapacity);
        } else {
            logger.debug("Failed to acquire permit (non-blocking), available: {}/{}", 
                    availableTokens.get(), burstCapacity);
        }
        return acquired;
    }

    @Override
    public int getRateLimit() {
        return rateLimit;
    }

    @Override
    public int getAvailablePermits() {
        return availableTokens.get();
    }
}
