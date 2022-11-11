package com.instamojo.android.activities;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.instamojo.android.R;
import com.instamojo.android.fragments.JuspaySafeBrowser;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;

import in.juspay.godel.browser.JuspayWebViewClient;
import in.juspay.godel.ui.JuspayBrowserFragment;
import in.juspay.godel.ui.JuspayWebView;

/**
 * Activity subclass extending {@link BaseActivity}. Activity for {@link JuspaySafeBrowser} fragment.
 */
public class PaymentActivity extends BaseActivity {

    private static final String TAG = PaymentActivity.class.getSimpleName();
    private JuspayBrowserFragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_instamojo);
        inflateXML();
        showFragment();
    }

    private void inflateXML() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Logger.d(TAG, "Inflated XML");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFragment() {
        final Bundle sourceArgs = getIntent().getBundleExtra(Constants.PAYMENT_BUNDLE);
        if (sourceArgs == null) {
            Logger.e(TAG, "Payment bundle is Null");
            returnResult(RESULT_CANCELED);
            return;
        }
        currentFragment = (JuspayBrowserFragment) getSupportFragmentManager().findFragmentById(R.id.juspay_browser_fragment);
        JuspayBrowserFragment.JuspayWebviewCallback juspayWebViewCallback = new JuspayBrowserFragment.JuspayWebviewCallback() {
            public void webviewReady() {
                JuspayWebView juspayWebView = currentFragment.getWebView();
                juspayWebView.setWebViewClient(new JuspayWebViewClient(juspayWebView, currentFragment));
                currentFragment.startPaymentWithArguments(sourceArgs);
            }
        };
        currentFragment.setupJuspayWebviewCallbackInterface(juspayWebViewCallback);
        Logger.d(TAG, "Loaded Fragment - " + currentFragment.getClass().getSimpleName());
    }

    @Override
    public void onBackPressed() {
        Logger.d(TAG, "Invoking Juspay Cancel Payment Handler");
        currentFragment.juspayBackPressedHandler(true);
    }
}
