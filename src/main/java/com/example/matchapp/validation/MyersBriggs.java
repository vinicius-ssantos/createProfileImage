package com.example.matchapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating Myers-Briggs personality types.
 * Valid Myers-Briggs types follow the pattern of four letters:
 * - First letter: E or I (Extraversion or Introversion)
 * - Second letter: S or N (Sensing or Intuition)
 * - Third letter: T or F (Thinking or Feeling)
 * - Fourth letter: J or P (Judging or Perceiving)
 * 
 * Examples: INTJ, ENFP, ISTP, etc.
 */
@Documented
@Constraint(validatedBy = MyersBriggsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MyersBriggs {
    String message() default "Invalid Myers-Briggs personality type. Must be a valid 4-letter code (e.g., INTJ, ENFP)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}