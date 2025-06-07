package com.example.matchapp.model;

/**
 * Enum representing the allowed gender values.
 */
public enum Gender {
    MALE,
    FEMALE,
    NON_BINARY,
    OTHER;
    
    /**
     * Checks if the given string is a valid gender value.
     * 
     * @param gender the gender string to check
     * @return true if the string is a valid gender value, false otherwise
     */
    public static boolean isValid(String gender) {
        if (gender == null) {
            return false;
        }
        
        try {
            valueOf(gender.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Converts the given string to a Gender enum value.
     * 
     * @param gender the gender string to convert
     * @return the Gender enum value, or null if the string is not a valid gender value
     */
    public static Gender fromString(String gender) {
        if (gender == null) {
            return null;
        }
        
        try {
            return valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}