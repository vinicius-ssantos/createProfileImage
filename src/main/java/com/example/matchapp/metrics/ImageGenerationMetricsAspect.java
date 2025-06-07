package com.example.matchapp.metrics;

import com.example.matchapp.exception.ApiAuthenticationException;
import com.example.matchapp.exception.ApiConnectionException;
import com.example.matchapp.exception.ApiRateLimitException;
import com.example.matchapp.exception.ImageGenerationException;
import com.example.matchapp.model.Profile;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for recording metrics related to image generation.
 * This aspect intercepts calls to the ImageGenerationService and records metrics.
 */
@Aspect
@Component
public class ImageGenerationMetricsAspect {
    private static final Logger logger = LoggerFactory.getLogger(ImageGenerationMetricsAspect.class);
    
    private final ImageGenerationMetrics metrics;
    
    public ImageGenerationMetricsAspect(ImageGenerationMetrics metrics) {
        this.metrics = metrics;
    }
    
    /**
     * Records metrics for image generation requests.
     * This method intercepts calls to the generateImage method of any implementation of ImageGenerationService.
     * 
     * @param joinPoint The join point representing the intercepted method call
     * @return The result of the intercepted method call
     * @throws Throwable If an error occurs during the method execution
     */
    @Around("execution(* com.example.matchapp.service.ImageGenerationService.generateImage(..))")
    public Object recordImageGenerationMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the profile from the method arguments
        Profile profile = (Profile) joinPoint.getArgs()[0];
        
        // Start timer for response time measurement
        Timer.Sample timerSample = Timer.start();
        
        // Record the request
        metrics.recordRequest();
        
        try {
            // Execute the original method
            Object result = joinPoint.proceed();
            
            // Record successful request
            metrics.recordSuccess();
            
            // Record response time
            long responseTimeMs = timerSample.stop(metrics.getResponseTimeTimer());
            logger.debug("Image generation for profile {} completed in {} ms", profile.id(), responseTimeMs);
            
            return result;
        } catch (Throwable e) {
            // Record appropriate metric based on exception type
            if (e instanceof ApiAuthenticationException || 
                e instanceof ApiConnectionException || 
                e instanceof ApiRateLimitException) {
                metrics.recordApiError();
                logger.debug("API error during image generation for profile {}: {}", profile.id(), e.getMessage());
            } else {
                metrics.recordFailure();
                logger.debug("Failure during image generation for profile {}: {}", profile.id(), e.getMessage());
            }
            
            // Record response time even for failures
            timerSample.stop(metrics.getResponseTimeTimer());
            
            // Rethrow the exception
            throw e;
        }
    }
}