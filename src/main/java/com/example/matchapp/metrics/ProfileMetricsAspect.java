package com.example.matchapp.metrics;

import com.example.matchapp.model.Profile;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Aspect for recording metrics related to profile operations.
 * This aspect intercepts calls to the ProfileRepository and records metrics.
 */
@Aspect
@Component
public class ProfileMetricsAspect {
    private static final Logger logger = LoggerFactory.getLogger(ProfileMetricsAspect.class);
    
    private final ProfileMetrics metrics;
    
    public ProfileMetricsAspect(ProfileMetrics metrics) {
        this.metrics = metrics;
    }
    
    /**
     * Records metrics for findAll operations.
     * 
     * @param joinPoint The join point representing the intercepted method call
     * @return The result of the intercepted method call
     * @throws Throwable If an error occurs during the method execution
     */
    @Around("execution(* com.example.matchapp.repository.ProfileRepository.findAll())")
    public Object recordFindAllMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample timerSample = Timer.start();
        
        metrics.recordFindAll();
        
        try {
            Object result = joinPoint.proceed();
            
            long responseTimeMs = timerSample.stop(metrics.getOperationTimer());
            logger.debug("findAll operation completed in {} ms", responseTimeMs);
            
            return result;
        } catch (Throwable e) {
            timerSample.stop(metrics.getOperationTimer());
            logger.debug("Error during findAll operation: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Records metrics for findById operations.
     * 
     * @param joinPoint The join point representing the intercepted method call
     * @return The result of the intercepted method call
     * @throws Throwable If an error occurs during the method execution
     */
    @Around("execution(* com.example.matchapp.repository.ProfileRepository.findById(String))")
    public Object recordFindByIdMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample timerSample = Timer.start();
        
        metrics.recordFindById();
        
        try {
            Object result = joinPoint.proceed();
            
            // Check if profile was found
            if (result instanceof Optional && ((Optional<?>) result).isEmpty()) {
                metrics.recordNotFound();
                logger.debug("Profile not found for ID: {}", joinPoint.getArgs()[0]);
            }
            
            long responseTimeMs = timerSample.stop(metrics.getOperationTimer());
            logger.debug("findById operation completed in {} ms", responseTimeMs);
            
            return result;
        } catch (Throwable e) {
            timerSample.stop(metrics.getOperationTimer());
            logger.debug("Error during findById operation: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Records metrics for save operations.
     * 
     * @param joinPoint The join point representing the intercepted method call
     * @return The result of the intercepted method call
     * @throws Throwable If an error occurs during the method execution
     */
    @Around("execution(* com.example.matchapp.repository.ProfileRepository.save(com.example.matchapp.model.Profile))")
    public Object recordSaveMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample timerSample = Timer.start();
        
        // Determine if this is a create or update operation
        Profile profile = (Profile) joinPoint.getArgs()[0];
        boolean isUpdate = false;
        
        try {
            // Check if profile already exists
            if (profile != null && profile.id() != null) {
                Object repo = joinPoint.getTarget();
                java.lang.reflect.Method findByIdMethod = repo.getClass().getMethod("findById", String.class);
                Optional<?> existingProfile = (Optional<?>) findByIdMethod.invoke(repo, profile.id());
                isUpdate = existingProfile.isPresent();
            }
            
            if (isUpdate) {
                metrics.recordUpdate();
                logger.debug("Recording update for profile ID: {}", profile.id());
            } else {
                metrics.recordSave();
                logger.debug("Recording new profile creation");
            }
            
            Object result = joinPoint.proceed();
            
            long responseTimeMs = timerSample.stop(metrics.getOperationTimer());
            logger.debug("save operation completed in {} ms", responseTimeMs);
            
            return result;
        } catch (Throwable e) {
            timerSample.stop(metrics.getOperationTimer());
            logger.debug("Error during save operation: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Records metrics for delete operations.
     * 
     * @param joinPoint The join point representing the intercepted method call
     * @return The result of the intercepted method call
     * @throws Throwable If an error occurs during the method execution
     */
    @Around("execution(* com.example.matchapp.repository.ProfileRepository.deleteById(String))")
    public Object recordDeleteMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample timerSample = Timer.start();
        
        metrics.recordDelete();
        
        try {
            Object result = joinPoint.proceed();
            
            // Check if profile was deleted
            if (result instanceof Boolean && !((Boolean) result)) {
                metrics.recordNotFound();
                logger.debug("Profile not found for deletion, ID: {}", joinPoint.getArgs()[0]);
            }
            
            long responseTimeMs = timerSample.stop(metrics.getOperationTimer());
            logger.debug("delete operation completed in {} ms", responseTimeMs);
            
            return result;
        } catch (Throwable e) {
            timerSample.stop(metrics.getOperationTimer());
            logger.debug("Error during delete operation: {}", e.getMessage());
            throw e;
        }
    }
}