package com.example.matchapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Validator for Myers-Briggs personality types.
 * Validates that the personality type follows the correct format:
 * - 4 letters
 * - First letter: E or I
 * - Second letter: S or N
 * - Third letter: T or F
 * - Fourth letter: J or P
 */
public class MyersBriggsValidator implements ConstraintValidator<MyersBriggs, String> {

    private static final Logger logger = LoggerFactory.getLogger(MyersBriggsValidator.class);

    // Pattern for valid Myers-Briggs types: 
    // First letter: E or I
    // Second letter: S or N
    // Third letter: T or F
    // Fourth letter: J or P
    private static final Pattern MYERS_BRIGGS_PATTERN = Pattern.compile("^[EI][SN][TF][JP]$");

    @Override
    public void initialize(MyersBriggs constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        // Trim the value to handle whitespace
        String trimmedValue = value.trim();

        // Check if the value is empty after trimming
        if (trimmedValue.isEmpty()) {
            logger.debug("Invalid Myers-Briggs personality type: empty string");
            return false;
        }

        boolean isValid = MYERS_BRIGGS_PATTERN.matcher(trimmedValue).matches();

        if (!isValid) {
            logger.debug("Invalid Myers-Briggs personality type: {}", value);
        }

        return isValid;
    }
}
