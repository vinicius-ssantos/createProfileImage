package com.example.matchapp.util;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LoggingUtilsTest {

    @Test
    void testSetAndGetRequestId() {
        // Test with a specific request ID
        String requestId = UUID.randomUUID().toString();
        String result = LoggingUtils.setRequestId(requestId);
        
        assertEquals(requestId, result, "The returned request ID should match the one we set");
        assertEquals(requestId, LoggingUtils.getRequestId(), "getRequestId should return the ID we set");
        assertEquals(requestId, MDC.get("requestId"), "MDC should contain the request ID we set");
        
        // Clean up
        LoggingUtils.clearMDC();
        assertNull(MDC.get("requestId"), "MDC should be cleared");
    }
    
    @Test
    void testSetRequestIdWithNull() {
        // Test with null (should generate a new UUID)
        String result = LoggingUtils.setRequestId(null);
        
        assertNotNull(result, "A new request ID should be generated when null is passed");
        assertEquals(result, LoggingUtils.getRequestId(), "getRequestId should return the generated ID");
        assertEquals(result, MDC.get("requestId"), "MDC should contain the generated request ID");
        
        // Clean up
        LoggingUtils.clearMDC();
    }
    
    @Test
    void testSetAndGetProfileId() {
        String profileId = "test-profile-id";
        LoggingUtils.setProfileId(profileId);
        
        assertEquals(profileId, LoggingUtils.getProfileId(), "getProfileId should return the ID we set");
        assertEquals(profileId, MDC.get("profileId"), "MDC should contain the profile ID we set");
        
        // Clean up
        LoggingUtils.clearMDC();
        assertNull(MDC.get("profileId"), "MDC should be cleared");
    }
    
    @Test
    void testWithMDCSupplier() {
        // Set up test data
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("testKey", "testValue");
        contextMap.put("requestId", "test-request-id");
        
        // Execute with context
        String result = LoggingUtils.withMDC(contextMap, () -> {
            assertEquals("testValue", MDC.get("testKey"), "MDC should contain the test key");
            assertEquals("test-request-id", MDC.get("requestId"), "MDC should contain the request ID");
            return "success";
        });
        
        assertEquals("success", result, "The function should execute and return its result");
        assertNull(MDC.get("testKey"), "MDC should be cleared after execution");
        assertNull(MDC.get("requestId"), "MDC should be cleared after execution");
    }
    
    @Test
    void testWithMDCRunnable() {
        // Set up test data
        Map<String, String> contextMap = new HashMap<>();
        contextMap.put("testKey", "testValue");
        
        // Set a value that should be preserved
        MDC.put("preserveKey", "preserveValue");
        
        // Execute with context
        LoggingUtils.withMDC(contextMap, () -> {
            assertEquals("testValue", MDC.get("testKey"), "MDC should contain the test key");
            assertEquals("preserveValue", MDC.get("preserveKey"), "MDC should preserve existing values");
        });
        
        assertNull(MDC.get("testKey"), "MDC should be cleared after execution");
        assertEquals("preserveValue", MDC.get("preserveKey"), "MDC should restore previous context");
        
        // Clean up
        MDC.clear();
    }
    
    @Test
    void testWithProfileId() {
        String profileId = "test-profile-id";
        
        // Execute with profile ID
        String result = LoggingUtils.withProfileId(profileId, () -> {
            assertEquals(profileId, MDC.get("profileId"), "MDC should contain the profile ID");
            return "success";
        });
        
        assertEquals("success", result, "The function should execute and return its result");
        assertNull(MDC.get("profileId"), "MDC should be cleared after execution");
    }
    
    @Test
    void testClearMDC() {
        // Set some values
        MDC.put("key1", "value1");
        MDC.put("key2", "value2");
        
        // Clear MDC
        LoggingUtils.clearMDC();
        
        assertNull(MDC.get("key1"), "MDC should be cleared");
        assertNull(MDC.get("key2"), "MDC should be cleared");
    }
}