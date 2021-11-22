package com.instamojo.android.helpers;

import com.instamojo.android.models.Order;

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

    /**
     * Key for transactionID in the return bundle
     */
    public static final String TRANSACTION_ID = "transactionID";

    /*
     * Key for paymentID in the return bundle
     */
    public static final String PAYMENT_ID = "paymentID";

    public static final String PAYMENT_STATUS = "paymentStatus";

    /**
     * Status code for UPI Pending Authentication
     */
    public static final int PENDING_PAYMENT = 2;

    public static final String KEY_CODE = "code";

    public static final String KEY_MESSGE = "message";
    public static final int PAYMENT_SUCCEDED = 5;
    public static final int PAYMENT_DECLINED = 6;
}
