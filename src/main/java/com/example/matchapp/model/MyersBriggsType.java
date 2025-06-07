package com.example.matchapp.model;

/**
 * Enum representing the allowed Myers-Briggs personality types.
 */
public enum MyersBriggsType {
    ISTJ, ISFJ, INFJ, INTJ,
    ISTP, ISFP, INFP, INTP,
    ESTP, ESFP, ENFP, ENTP,
    ESTJ, ESFJ, ENFJ, ENTJ;
    
    /**
     * Checks if the given string is a valid Myers-Briggs personality type.
     * 
     * @param type the Myers-Briggs type string to check
     * @return true if the string is a valid Myers-Briggs type, false otherwise
     */
    public static boolean isValid(String type) {
        if (type == null) {
            return false;
        }
        
        try {
            valueOf(type.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Converts the given string to a MyersBriggsType enum value.
     * 
     * @param type the Myers-Briggs type string to convert
     * @return the MyersBriggsType enum value, or null if the string is not a valid Myers-Briggs type
     */
    public static MyersBriggsType fromString(String type) {
        if (type == null) {
            return null;
        }
        
        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}