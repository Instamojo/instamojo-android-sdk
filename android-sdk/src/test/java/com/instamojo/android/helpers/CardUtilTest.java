package com.instamojo.android.helpers;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)
public class CardUtilTest {

    // Sample numbers picked up from
    // https://github.com/Instamojo/mojocard/blob/develop/design/static/ts/__tests__/config.ts
    private static final String VALID_VISA = "4242424242424242";
    private static final String VALID_MASTERCARD = "5555555555554444";
    private static final String VALID_DINERS_CLUB = "30569309025904";
    private static final String VALID_AMEX = "378282246310005";
    private static final String VALID_DISCOVER = "6011000990139424";
    private static final String VALID_RUPAY = "6073849700004947";
    private static final String VALID_MAESTRO = "6759649826438453";

    private static final String INVALID_CARD = "0000000000000000";

    private Object[] parametersForCardNumberValidator_ValidNumber_ReturnsTrue() {
        return new Object[]{
                VALID_VISA,
                VALID_MASTERCARD,
                VALID_DINERS_CLUB,
                VALID_AMEX,
                VALID_DISCOVER,
                VALID_RUPAY,
                VALID_MAESTRO
        };
    }

    @Test
    @Parameters
    public void cardNumberValidator_ValidNumber_ReturnsTrue(String validCardNumber) {
        assertTrue(CardUtil.isCardNumberValid(validCardNumber));
    }

    private Object[] parametersForCardNumberValidator_InvalidNumber_ReturnsFalse() {
        return new Object[]{
                INVALID_CARD
        };
    }

    @Test
    @Parameters
    public void cardNumberValidator_InvalidNumber_ReturnsFalse(String invalidCardNumber) {
        assertFalse(CardUtil.isCardNumberValid(invalidCardNumber));
    }

    private Object[] parametersForGetCardType_CardNumber_ReturnsExpectedType() {
        return new Object[]{
                new Object[]{VALID_VISA, CardType.VISA},
                new Object[]{VALID_MASTERCARD, CardType.MASTER_CARD},
                new Object[]{VALID_DINERS_CLUB, CardType.DINERS_CLUB},
                new Object[]{VALID_AMEX, CardType.AMEX},
                new Object[]{VALID_DISCOVER, CardType.DISCOVER},
                new Object[]{VALID_RUPAY, CardType.RUPAY},
                new Object[]{VALID_MAESTRO, CardType.MAESTRO},
                new Object[]{INVALID_CARD, CardType.UNKNOWN},
        };
    }

    @Test
    @Parameters
    public void getCardType_CardNumber_ReturnsExpectedType(String cardNumber, CardType expectedCardType) {
        assertEquals(expectedCardType, CardUtil.getCardType(cardNumber));
    }

    @Test
    public void isMaestroCard_Maestro_ReturnsTrue() {
        assertTrue(CardUtil.isMaestroCard(VALID_MAESTRO));
    }

    @Test
    public void isMaestroCard_NotMaestro_ReturnsFalse() {
        assertFalse(CardUtil.isMaestroCard(VALID_VISA));
    }

    private Object[] parametersForIsDateExpired_DateString_ReturnsExpected() {
        DateFormat validDateFormat = new SimpleDateFormat("MM/yy");
        DateFormat invalidDateFormat = new SimpleDateFormat("MM/yyyy");

        Calendar futureCalendar = Calendar.getInstance();
        // 1 month in future
        futureCalendar.add(Calendar.MONTH, 1);

        Calendar pastCalendar = Calendar.getInstance();
        // 1 month in past
        pastCalendar.add(Calendar.MONTH, -1);

        return new Object[]{
                new Object[]{validDateFormat.format(futureCalendar.getTime()), false},
                new Object[]{validDateFormat.format(pastCalendar.getTime()), true},
                new Object[]{invalidDateFormat.format(pastCalendar.getTime()), true},
        };
    }

    @Test
    @Parameters
    public void isDateExpired_DateString_ReturnsExpected(String dateString, boolean expected) {
        assertEquals(expected, CardUtil.isDateInValid(dateString));
    }

}