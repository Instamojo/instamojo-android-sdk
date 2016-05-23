package com.instamojo.android.helpers;

import com.instamojo.android.models.Order;

/**
 * Authored by vedhavyas on 23/05/16.
 */
public class Constants {

    /**
     * Extra Bundle key passed to JuspaySafe browser.
     */
    public static final String PAYMENT_BUNDLE = "payment_bundle";

    /**
     * Extra Bundle key for Order ID which is passed back from SDK.
     */
    public static final String ORDER_ID = "orderId";

    /**
     * Extra Bundle key for Order Status which is passed back from SDK.
     */
    public static final String TRANSACTION_STATUS = "transaction_status";

    /**
     * Extra key for the {@link Order} object that is sent through Bundle.
     */
    public static final String ORDER = "order";

    /**
     * Activity request code
     */
    public static final int REQUEST_CODE = 9;

    /**
     * key for the Juspay card URL
     */
    public static final String URL = "url";

    /**
     * Key for the merchant ID for the current transaction
     */
    public static final String MERCHANT_ID = "merchantId";

    /**
     * Key for Netbanking Data passed to Juspay
     */
    public static final String POST_DATA = "postData";
}