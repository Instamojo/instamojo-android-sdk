package com.instamojo.android.network;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;

import com.instamojo.android.BuildConfig;
import com.instamojo.android.activities.PaymentActivity;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;

/**
 * JavaScript interface to transfer control from Webview to Application.
 */
public class JavaScriptInterface {

    private Activity activity;

    /**
     * Constructor for ScriptInterface.
     *
     * @param activity This activity must be a subclass of {@link com.instamojo.android.activities.BaseActivity}.
     */
    public JavaScriptInterface(Activity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void onTransactionComplete(String orderID, String transactionID, String paymentID, String paymentStatus) {
        Logger.d(this.getClass().getSimpleName(), "Received Call to Javascript Interface");
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ORDER_ID, orderID);
        bundle.putString(Constants.TRANSACTION_ID, transactionID);
        bundle.putString(Constants.PAYMENT_ID, paymentID);
        bundle.putString(Constants.PAYMENT_STATUS, paymentStatus);
        Logger.d(this.getClass().getSimpleName(), "Returning result back to caller");
        ((PaymentActivity) activity).returnResult(bundle, Activity.RESULT_OK);
    }

    @JavascriptInterface
    public int getSDKVersionCode() {
        return BuildConfig.VERSION_CODE;
    }
}
