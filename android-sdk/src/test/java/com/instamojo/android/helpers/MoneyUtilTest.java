package com.instamojo.android.helpers;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class MoneyUtilTest {

    private Object[] parametersForGetRoundedValue_ValidParams_ReturnsExpectedValue() {
        return new Object[]{
                new Object[]{1.123456789, 1, 1.1},
                new Object[]{1.123456789, 3, 1.123},
                new Object[]{1.123456789, 5, 1.12346},
        };
    }

    @Test
    @Parameters
    public void getRoundedValue_ValidParams_ReturnsExpectedValue(double value, int precision, double expectedValue) {
        assertEquals(expectedValue, MoneyUtil.getRoundedValue(value, precision), 0.0);
    }

    private Object[] parametersForGetMonthlyEMI_ValidParams_ReturnsExpectedEMI() {
        return new Object[]{
                new Object[]{1000.0, new BigDecimal(10.0), 12, 87.92},
                new Object[]{5000.0, new BigDecimal(15.0), 9, 590.85},
                new Object[]{10000.0, new BigDecimal(13.0), 3, 3405.81}
        };
    }

    @Test
    @Parameters
    public void getMonthlyEMI_ValidParams_ReturnsExpectedEMI(double amount, BigDecimal rate, int tenure, double expectedEMI) {
        assertEquals(expectedEMI, MoneyUtil.getMonthlyEMI(amount, rate, tenure), 0.0);
    }


}
