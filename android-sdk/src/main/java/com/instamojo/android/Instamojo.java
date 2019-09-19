package com.instamojo.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.instamojo.android.activities.PaymentDetailsActivity;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.network.ServiceGenerator;

/**
 * Instamojo SDK
 */
public class Instamojo extends BroadcastReceiver {

    public static final String TAG = Instamojo.class.getSimpleName();
    public static final String ACTION_INTENT_FILTER = "com.instamojo.android.sdk";
    private static Instamojo mInstance;
    private Context mContext;

    private PaymentCallback mCallback;

    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_CANCELLED = 2;
    public static final int RESULT_FAILED = 3;


    private Instamojo() {
        // Default private constructor
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String unknownErrMsg = "Unknown error";

        Bundle returnData = intent.getExtras();
        if (returnData != null && returnData.getInt(Constants.KEY_CODE, -1) != -1) {
            int resultCode = returnData.getInt(Constants.KEY_CODE);
            switch (resultCode) {
                case RESULT_SUCCESS:
                    String orderID = returnData.getString(Constants.ORDER_ID);
                    String transactionID = returnData.getString(Constants.TRANSACTION_ID);
                    String paymentID = returnData.getString(Constants.PAYMENT_ID);
                    String paymentStatus = returnData.getString(Constants.PAYMENT_STATUS);
                    mCallback.onPaymentComplete(orderID, transactionID, paymentID, paymentStatus);
                    return;

                case RESULT_CANCELLED:
                    mCallback.onPaymentCancelled();
                    return;

                case RESULT_FAILED:
                    String message = returnData.getString(Constants.KEY_MESSGE);
                    if (message == null) {
                        message = unknownErrMsg;
                    }

                    mCallback.onInitiatePaymentFailure(message);
                    return;
            }
        }

        mCallback.onInitiatePaymentFailure(unknownErrMsg);
    }

    public enum Environment {
        TEST, PRODUCTION
    }

    public interface PaymentCallback {
        void onPaymentComplete(String orderID, String transactionID, String paymentID, String paymentStatus);

        void onPaymentCancelled();

        void onInitiatePaymentFailure(String errorMessage);
    }

    public static Instamojo getInstance() {
        if (mInstance == null) {
            synchronized (Instamojo.class) {
                if (mInstance == null) {
                    mInstance = new Instamojo();
                }
            }
        }

        return mInstance;
    }

    /**
     * Initialize the SDK with application context and environment
     */
    public void initialize(Context context, Environment environment) {
        Log.e(TAG, "Initializing SDK...");

        mContext = context;
        ServiceGenerator.initialize(environment);
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * Initiate an Instamojo payment with an orderID
     *
     * @param orderID  Identifier of an Gateway Order instance created in the server (developer)
     * @param callback Callback interface to receive the response from Instamojo SDK
     */
    public void initiatePayment(final Activity activity, final String orderID, final PaymentCallback callback) {
        mCallback = callback;
        Intent intent = new Intent(mContext, PaymentDetailsActivity.class);
        intent.putExtra(Constants.ORDER_ID, orderID);
        activity.startActivity(intent);
    }
}
