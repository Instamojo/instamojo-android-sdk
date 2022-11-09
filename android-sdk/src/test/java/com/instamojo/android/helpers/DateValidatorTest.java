package com.instamojo.android.helpers;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class DateValidatorTest {

    private static final String VALID_MONTH = "8";
    private static final String INVALID_MONTH = "13";
    private static final String VALID_YEAR = "2035";
    private static final String INVALID_YEAR = "0000";
    private static final String VALID_DIGITS = "123";
    private static final String INVALID_DIGITS = "abc";

    @Test
    public void validator_ValidMonth_ValidYear_ReturnsTrue() {
        assertTrue(DateValidator.isValid(VALID_MONTH, VALID_YEAR));
    }

    @Test
    public void validator_ValidMonth_InvalidYear_ReturnsFalse() {
        assertFalse(DateValidator.isValid(VALID_MONTH, INVALID_YEAR));
    }

    @Test
    public void validator_InvalidMonth_ValidYear_ReturnsFalse() {
        assertFalse(DateValidator.isValid(INVALID_MONTH, VALID_YEAR));
    }

    @Test
    public void validator_InvalidMonth_InvalidYear_ReturnsFalse() {
        assertFalse(DateValidator.isValid(INVALID_MONTH, INVALID_YEAR));
    }

    @Test
    public void validator_IsDigits_ReturnsTrue() {
        assertTrue(DateValidator.isDigitsOnly(VALID_DIGITS));
    }

    @Test
    public void validator_IsNotDigits_ReturnsFalse() {
        assertFalse(DateValidator.isDigitsOnly(INVALID_DIGITS));
    }
}
