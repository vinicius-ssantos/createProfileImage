package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.RateLimitExceededException;
import com.example.matchapp.service.RateLimiterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter using a token bucket algorithm.
 */
@Service
public class InMemoryRateLimiterService implements RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryRateLimiterService.class);

    private final int maxRequests;
    private static final long intervalMillis = 60_000L;
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private volatile long intervalStart = Instant.now().toEpochMilli();

    public InMemoryRateLimiterService(@org.springframework.beans.factory.annotation.Qualifier("imageGenProperties") ImageGenProperties properties) {
        this.maxRequests = properties.getRequestsPerMinute();
    }

    @Override
    public synchronized void acquire() {
        long now = Instant.now().toEpochMilli();
        if (now - intervalStart >= intervalMillis) {
            requestCount.set(0);
            intervalStart = now;
        }
        if (requestCount.incrementAndGet() > maxRequests) {
            requestCount.decrementAndGet();
            logger.warn("Client-side rate limit of {} requests/minute exceeded", maxRequests);
            throw new RateLimitExceededException("Client-side rate limit exceeded");
        }
    }
}
