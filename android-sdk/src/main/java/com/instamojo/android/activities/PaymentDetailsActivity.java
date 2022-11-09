package com.instamojo.android.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.instamojo.android.Instamojo;
import com.instamojo.android.R;
import com.instamojo.android.fragments.BaseFragment;
import com.instamojo.android.fragments.PaymentOptionsFragment;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.models.GatewayOrder;
import com.instamojo.android.network.ImojoService;
import com.instamojo.android.network.ServiceGenerator;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Payment Details Activity extends the {@link BaseActivity}. Activity lets user to choose a Payment method
 */
public class PaymentDetailsActivity extends BaseActivity {

    private static final String TAG = PaymentDetailsActivity.class.getSimpleName();
    private GatewayOrder order;
    private boolean showSearch;
    private SearchView.OnQueryTextListener onQueryTextListener;
    private String hintText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarColor();
        setContentView(R.layout.activity_payment_details_instamojo);
        inflateXML();

        String orderID = getIntent().getStringExtra(Constants.ORDER_ID);
        if (orderID == null) {
            Logger.e(TAG, "Object not found. Sending back - Payment Cancelled");
            fireBroadcastAndReturn(Instamojo.RESULT_CANCELLED, null);
        }

        fetchOrder(orderID);

        IntentFilter filter = new IntentFilter(Instamojo.ACTION_INTENT_FILTER);
        registerReceiver(Instamojo.getInstance(), filter);
    }

    private void setStatusBarColor() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.primary_color));
    }

    private void fetchOrder(String orderID) {
        ImojoService imojoService = ServiceGenerator.getImojoService();
        Call<GatewayOrder> gatewayOrderCall = imojoService.getPaymentOptions(orderID);
        gatewayOrderCall.enqueue(new Callback<GatewayOrder>() {
            @Override
            public void onResponse(Call<GatewayOrder> call, Response<GatewayOrder> response) {
                if (response.isSuccessful()) {
                    order = response.body();
                    loadFragments();

                } else {
                    if (response.errorBody() != null) {
                        try {
                            Logger.d(TAG, "Error response from server while fetching order details.");
                            Logger.e(TAG, "Error: " + response.errorBody().string());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    fireBroadcastAndReturn(Instamojo.RESULT_FAILED, null, "Error fetching order details");
                }
            }

            @Override
            public void onFailure(Call<GatewayOrder> call, Throwable t) {
                fireBroadcastAndReturn(Instamojo.RESULT_FAILED, null, "Failed to fetch order details");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (showSearch) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_payment_options, menu);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            MenuItem searchMenuItem = menu.findItem(R.id.search);
            SearchView searchView = (SearchView) searchMenuItem.getActionView();
            searchView.setQueryHint(hintText);

            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setSubmitButtonEnabled(true);
            if (onQueryTextListener != null) {
                searchView.setOnQueryTextListener(onQueryTextListener);
            }
        }

        Logger.d(TAG, "Inflated Options Menu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Start the payment activity with given bundle
     *
     * @param bundle Bundle with either card/netbanking url
     */
    public void startPaymentActivity(Bundle bundle) {
        Logger.d(TAG, "Starting PaymentActivity with given Bundle");
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra(Constants.PAYMENT_BUNDLE, bundle);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            fireBroadcastAndReturn(Instamojo.RESULT_CANCELLED, null);

        } else {
            getSupportFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE) {
            Logger.d(TAG, "Returning back result to caller");
            fireBroadcastAndReturn(mapResultCode(resultCode), data.getExtras());
        }
    }

    private void inflateXML() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updateActionBar();
        Logger.d(TAG, "Inflated XML");
    }

    /**
     * @return The current Order
     */
    public GatewayOrder getOrder() {
        return order;
    }

    private void loadFragments() {
        Logger.d(TAG, "Found order Object. Starting PaymentOptionsFragment");
        loadFragment(new PaymentOptionsFragment(), false);
    }

    /**
     * Load the given fragment to the support fragment manager
     *
     * @param fragment       Fragment to be added. Must be a subclass of {@link BaseFragment}
     * @param addToBackStack Whether to add this fragment to back stack
     */
    public void loadFragment(BaseFragment fragment, boolean addToBackStack) {
        Logger.d(TAG, "Loading Fragment - " + fragment.getClass().getSimpleName());
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.container, fragment);
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(fragment.getFragmentName());
        }
        fragmentTransaction.commit();
        Logger.d(TAG, "Loaded Fragment - " + fragment.getClass().getSimpleName());
    }

    /**
     * Show the search icon in the actionbar
     *
     * @param queryTextListener {@link android.support.v7.widget.SearchView.OnQueryTextListener} to listen for the query string
     */
    public void showSearchOption(String hintText, SearchView.OnQueryTextListener queryTextListener) {
        this.showSearch = true;
        this.onQueryTextListener = queryTextListener;
        this.hintText = hintText;
        invalidateOptionsMenu();
    }

    public void hideSearchOption() {
        this.showSearch = false;
        invalidateOptionsMenu();
    }

    private void fireBroadcastAndReturn(int resultCode, Bundle data) {
        fireBroadcastAndReturn(resultCode, data, null);
    }

    private void fireBroadcastAndReturn(int resultCode, Bundle data, String message) {
        Intent intent = new Intent();

        if (data != null) {
            intent.putExtras(data);
        }

        intent.putExtra(Constants.KEY_CODE, resultCode);

        if (message != null && !message.isEmpty()) {
            intent.putExtra(Constants.KEY_MESSGE, message);
        }

        intent.setAction(Instamojo.ACTION_INTENT_FILTER);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);

        // Finish this activity
        finish();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(Instamojo.getInstance());
        super.onDestroy();
    }

    private int mapResultCode(int activityResultCode) {
        switch (activityResultCode) {
            case RESULT_OK:
                return Instamojo.RESULT_SUCCESS;

            case RESULT_CANCELED:
                return Instamojo.RESULT_CANCELLED;
        }

        return Instamojo.RESULT_FAILED;
    }

    public void onUPIResponse(Bundle bundle, int resultOk) {
        fireBroadcastAndReturn(mapResultCode(resultOk),bundle);
    }

}