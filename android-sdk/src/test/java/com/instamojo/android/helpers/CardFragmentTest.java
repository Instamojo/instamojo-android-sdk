package com.instamojo.android.helpers;

import com.instamojo.android.fragments.CardFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import junitparams.JUnitParamsRunner;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class CardFragmentTest {

    private static final String CARD_NUMBER = "4242";
    private static final List<String> EXPECTED_CARD_NUMBER_ARRAY = Arrays.asList("4", "2", "4", "2");

    @Test
    public void removeEmptyString_ReturnsExpectedType() {
        assertEquals(EXPECTED_CARD_NUMBER_ARRAY, CardFragment.getCardNumberArray(CARD_NUMBER));
    }

    @Test
    public void checkValidPaymentStatus() {
        assertEquals(5, Constants.PAYMENT_SUCCEDED);
    }

    @Test
    public void checkValidPaymentPending() {
        assertEquals(2, Constants.PENDING_PAYMENT);
    }

    @Test
    public void checkPaymentDeclined() {
        assertEquals(6, Constants.PAYMENT_DECLINED);
    }


}
