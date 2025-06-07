package com.example.matchapp.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator that provides overall application health status.
 * This indicator will be included in the health endpoint response.
 */
@Component
public class ApplicationHealthIndicator implements HealthIndicator {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationHealthIndicator.class);
    
    private final Instant startTime = Instant.now();
    
    @Value("${spring.application.name:create_ia_profiles}")
    private String applicationName;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Override
    public Health health() {
        try {
            // Get JVM memory information
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
            
            // Calculate memory usage percentages
            double heapUsagePercent = (double) heapMemoryUsage.getUsed() / heapMemoryUsage.getMax() * 100;
            
            // Calculate uptime
            Duration uptime = Duration.between(startTime, Instant.now());
            String formattedUptime = formatDuration(uptime);
            
            // Format start time
            String formattedStartTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
                    .withZone(ZoneId.systemDefault())
                    .format(startTime);
            
            // Create details map
            Map<String, Object> details = new HashMap<>();
            details.put("application", applicationName);
            details.put("profile", activeProfile);
            details.put("startTime", formattedStartTime);
            details.put("uptime", formattedUptime);
            details.put("heap.used", formatBytes(heapMemoryUsage.getUsed()));
            details.put("heap.committed", formatBytes(heapMemoryUsage.getCommitted()));
            details.put("heap.max", formatBytes(heapMemoryUsage.getMax()));
            details.put("heap.usagePercent", String.format("%.2f%%", heapUsagePercent));
            details.put("nonHeap.used", formatBytes(nonHeapMemoryUsage.getUsed()));
            details.put("nonHeap.committed", formatBytes(nonHeapMemoryUsage.getCommitted()));
            
            // Add thread information
            int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
            int peakThreadCount = ManagementFactory.getThreadMXBean().getPeakThreadCount();
            details.put("threads.current", threadCount);
            details.put("threads.peak", peakThreadCount);
            
            // Check if heap usage is above warning threshold (80%)
            if (heapUsagePercent > 80) {
                logger.warn("High heap memory usage: {}", String.format("%.2f%%", heapUsagePercent));
                return Health.status("WARNING")
                        .withDetails(details)
                        .build();
            }
            
            return Health.up()
                    .withDetails(details)
                    .build();
        } catch (Exception e) {
            logger.error("Error checking application health: {}", e.getMessage(), e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("exception", e.getClass().getName())
                    .build();
        }
    }
    
    /**
     * Formats a duration into a human-readable string.
     * 
     * @param duration The duration to format
     * @return A formatted string (e.g., "3d 2h 5m 30s")
     */
    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        duration = duration.minusDays(days);
        
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        
        long seconds = duration.getSeconds();
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            sb.append(minutes).append("m ");
        }
        sb.append(seconds).append("s");
        
        return sb.toString();
    }
    
    /**
     * Formats bytes into a human-readable string (KB, MB, GB, etc.)
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}