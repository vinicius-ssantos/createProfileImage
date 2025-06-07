package com.example.matchapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.context.annotation.Bean;

import java.util.Map;
import java.util.HashMap;

import com.example.matchapp.exception.ApiConnectionException;
import com.example.matchapp.exception.ApiRateLimitException;

/**
 * Configuration for retry mechanism.
 * Enables Spring Retry and configures a RetryTemplate with exponential backoff.
 */
@Configuration
@EnableRetry
public class RetryConfig {

    /**
     * Creates a RetryTemplate with exponential backoff policy.
     * 
     * @return configured RetryTemplate
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Configure exponential backoff
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000); // 1 second
        backOffPolicy.setMultiplier(2.0); // double the interval each time
        backOffPolicy.setMaxInterval(30000); // max 30 seconds
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Configure which exceptions should trigger retry
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(ApiConnectionException.class, true);
        retryableExceptions.put(ApiRateLimitException.class, true);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions, true);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
}