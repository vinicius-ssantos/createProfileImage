package com.example.matchapp.util;

import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Utility class for centralized logging functionality.
 * Provides methods for working with MDC (Mapped Diagnostic Context) and standardized logging.
 */
public class LoggingUtils {

    private static final String REQUEST_ID = "requestId";
    private static final String PROFILE_ID = "profileId";

    /**
     * Sets the request ID in the MDC context.
     * If no request ID is provided, a new UUID is generated.
     *
     * @param requestId The request ID to set, or null to generate a new one
     * @return The request ID that was set
     */
    public static String setRequestId(String requestId) {
        String id = requestId != null ? requestId : UUID.randomUUID().toString();
        MDC.put(REQUEST_ID, id);
        return id;
    }

    /**
     * Gets the current request ID from MDC context.
     *
     * @return The current request ID, or null if not set
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    /**
     * Sets the profile ID in the MDC context.
     *
     * @param profileId The profile ID to set
     */
    public static void setProfileId(String profileId) {
        if (profileId != null) {
            MDC.put(PROFILE_ID, profileId);
        }
    }

    /**
     * Gets the current profile ID from MDC context.
     *
     * @return The current profile ID, or null if not set
     */
    public static String getProfileId() {
        return MDC.get(PROFILE_ID);
    }

    /**
     * Clears all MDC context values.
     */
    public static void clearMDC() {
        MDC.clear();
    }

    /**
     * Executes a function with the given MDC context values and clears them afterward.
     *
     * @param contextMap The MDC context values to set
     * @param function The function to execute
     * @param <T> The return type of the function
     * @return The result of the function
     */
    public static <T> T withMDC(Map<String, String> contextMap, Supplier<T> function) {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        try {
            if (contextMap != null) {
                contextMap.forEach(MDC::put);
            }
            return function.get();
        } finally {
            MDC.clear();
            if (previousContext != null) {
                previousContext.forEach(MDC::put);
            }
        }
    }

    /**
     * Executes a runnable with the given MDC context values and clears them afterward.
     *
     * @param contextMap The MDC context values to set
     * @param runnable The runnable to execute
     */
    public static void withMDC(Map<String, String> contextMap, Runnable runnable) {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        try {
            if (contextMap != null) {
                contextMap.forEach(MDC::put);
            }
            runnable.run();
        } finally {
            MDC.clear();
            if (previousContext != null) {
                previousContext.forEach(MDC::put);
            }
        }
    }

    /**
     * Executes a function with the profile ID set in MDC and clears it afterward.
     *
     * @param profileId The profile ID to set
     * @param function The function to execute
     * @param <T> The return type of the function
     * @return The result of the function
     */
    public static <T> T withProfileId(String profileId, Supplier<T> function) {
        String previousProfileId = getProfileId();
        try {
            setProfileId(profileId);
            return function.get();
        } finally {
            if (previousProfileId != null) {
                setProfileId(previousProfileId);
            } else {
                MDC.remove(PROFILE_ID);
            }
        }
    }

    /**
     * Executes a runnable with the profile ID set in MDC and clears it afterward.
     *
     * @param profileId The profile ID to set
     * @param runnable The runnable to execute
     */
    public static void withProfileId(String profileId, Runnable runnable) {
        String previousProfileId = getProfileId();
        try {
            setProfileId(profileId);
            runnable.run();
        } finally {
            if (previousProfileId != null) {
                setProfileId(previousProfileId);
            } else {
                MDC.remove(PROFILE_ID);
            }
        }
    }
}