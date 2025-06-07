package com.example.matchapp.service.impl;

import com.example.matchapp.config.ImageGenProperties;
import com.example.matchapp.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateLimiterServiceTest {

    private InMemoryRateLimiterService rateLimiter;

    @BeforeEach
    void setUp() {
        ImageGenProperties props = new ImageGenProperties();
        props.setRequestsPerMinute(2);
        rateLimiter = new InMemoryRateLimiterService(props);
    }

    @Test
    void acquire_withinLimit_doesNotThrow() {
        assertDoesNotThrow(() -> {
            rateLimiter.acquire();
            rateLimiter.acquire();
        });
    }

    @Test
    void acquire_exceedsLimit_throwsException() {
        rateLimiter.acquire();
        rateLimiter.acquire();
        assertThrows(RateLimitExceededException.class, () -> rateLimiter.acquire());
    }
}
