package com.example.matchapp.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Myers-Briggs personality type validator.
 */
class MyersBriggsValidatorTest {

    private MyersBriggsValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new MyersBriggsValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"INTJ", "ENFP", "ISTP", "ESFJ"})
    void shouldValidateCorrectMyersBriggsTypes(String type) {
        assertTrue(validator.isValid(type, context), "Valid Myers-Briggs type should pass validation: " + type);
    }

    @ParameterizedTest
    @ValueSource(strings = {"INT", "ENFPP", "XSTP", "ESF", "1NTJ", "ENTP-", "intj", "EnFp"})
    void shouldRejectInvalidMyersBriggsTypes(String type) {
        assertFalse(validator.isValid(type, context), "Invalid Myers-Briggs type should fail validation: " + type);
    }

    @Test
    void shouldAcceptNullValue() {
        assertTrue(validator.isValid(null, context), "Null value should be considered valid (handled by @NotNull)");
    }

    @Test
    void shouldRejectEmptyString() {
        assertFalse(validator.isValid("", context), "Empty string should be rejected");
    }

    @Test
    void shouldRejectWhitespaceOnly() {
        assertFalse(validator.isValid("    ", context), "Whitespace-only string should be rejected");
    }
}