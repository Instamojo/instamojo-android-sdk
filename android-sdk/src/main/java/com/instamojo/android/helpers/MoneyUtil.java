package com.instamojo.android.helpers;

import android.os.Bundle;

import com.instamojo.android.models.GatewayOrder;

import java.math.BigDecimal;

public class MoneyUtil {

    /**
     * Converts a number to a expected precision by rounding the number.
     * @param value Amount
     * @param precision Number of precision digits post decimal point.
     * @return Rounded value of amount.
     */
    public static double getRoundedValue(double value, int precision) {
        BigDecimal bigDecimal = new BigDecimal(value);
        bigDecimal = bigDecimal.setScale(precision, BigDecimal.ROUND_HALF_DOWN);
        return bigDecimal.doubleValue();
    }

    /**
     * Calculates the monthly EMI installment of an EMI Plan.
     * @param amount Principal Amount.
     * @param rate Rate of interest.
     * @param tenure Number of months.
     * @return Monthly installment amount.
     */
    public static double getMonthlyEMI(double amount, BigDecimal rate, int tenure) {
        double perRate = rate.doubleValue() / 1200;
        double emiAmount = amount * perRate / (1 - Math.pow((1 / (1 + perRate)), tenure));
        return getRoundedValue(emiAmount, 2);
    }

    public static Bundle createBundleFromOrder(String id,String transactionID, String paymentID) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ORDER_ID, id);
        bundle.putString(Constants.TRANSACTION_ID, transactionID);
        bundle.putString(Constants.PAYMENT_ID, paymentID);
        return bundle;
    }

}
