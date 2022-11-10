package com.instamojo.androidsdksample;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.instamojo.android.activities.PaymentActivity;
import com.instamojo.android.adapters.BankListAdapter;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.helpers.ObjectMapper;
import com.instamojo.android.models.Bank;
import com.instamojo.android.models.Card;
import com.instamojo.android.models.CardOptions;
import com.instamojo.android.models.CardPaymentResponse;
import com.instamojo.android.models.GatewayOrder;
import com.instamojo.android.models.PaymentOptions;
import com.instamojo.android.network.ImojoService;
import com.instamojo.android.network.ServiceGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomUIActivity extends AppCompatActivity {

    private static final String TAG = CustomUIActivity.class.getSimpleName();
    private AlertDialog dialog;
    private GatewayOrder mOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_form);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false); // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog);
        dialog = builder.create();

        final String orderID = getIntent().getStringExtra(Constants.ORDER_ID);
        fetchOrder(orderID);
    }

    private void fetchOrder(String orderID) {
        ImojoService imojoService = ServiceGenerator.getImojoService();
        Call<GatewayOrder> gatewayOrderCall = imojoService.getPaymentOptions(orderID);
        gatewayOrderCall.enqueue(new Callback<GatewayOrder>() {
            @Override
            public void onResponse(Call<GatewayOrder> call, Response<GatewayOrder> response) {
                if (response.isSuccessful()) {
                    mOrder = response.body();
                    makeUI();

                } else {
                    if (response.errorBody() != null) {
                        try {
                            Logger.d(TAG, "Error response from server while fetching order details.");
                            Logger.e(TAG, "Error: " + response.errorBody().string());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<GatewayOrder> call, Throwable t) {
                Logger.d(TAG, "Failure fetching gateway order");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //send back the result to Main activity
        if (requestCode == Constants.REQUEST_CODE) {
            setResult(resultCode);
            setIntent(data);
            finish();
        }
    }

    private void makeUI() {

        //finish the activity if the order is null or both the debit and netbanking is disabled
        if (mOrder == null || (mOrder.getPaymentOptions().getCardOptions() == null
                && mOrder.getPaymentOptions().getNetBankingOptions() == null)) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        final PaymentOptions paymentOptions = mOrder.getPaymentOptions();

        final AppCompatEditText cardNumber = findViewById(R.id.card_number);
        final AppCompatEditText cardExpiryDate = findViewById(R.id.card_expiry_date);
        cardNumber.setNextFocusDownId(R.id.card_expiry_date);
        final AppCompatEditText cardHoldersName = findViewById(R.id.card_holder_name);
        cardExpiryDate.setNextFocusDownId(R.id.card_holder_name);
        final AppCompatEditText cvv = findViewById(R.id.card_cvv);
        cardHoldersName.setNextFocusDownId(R.id.card_cvv);
        AppCompatButton proceed = findViewById(R.id.proceed_with_card);
        View separator = findViewById(R.id.net_banking_separator);
        AppCompatSpinner netBankingSpinner = findViewById(R.id.net_banking_spinner);

        if (paymentOptions.getCardOptions() == null) {
            //seems like card payment is not enabled
            findViewById(R.id.card_layout_1).setVisibility(View.GONE);
            findViewById(R.id.card_layout_2).setVisibility(View.GONE);
            proceed.setVisibility(View.GONE);
            separator.setVisibility(View.GONE);

        } else {
            proceed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Card card = new Card();
                    card.setCardNumber(cardNumber.getText().toString());
                    card.setDate(cardExpiryDate.getText().toString());
                    card.setCardHolderName(cardHoldersName.getText().toString());
                    card.setCvv(cvv.getText().toString());

                    //Validate the card here
                    if (!cardValid(card)) {
                        return;
                    }

                    //Get order details form Juspay
                    proceedWithCard(mOrder, card);
                }
            });
        }

        if (paymentOptions.getNetBankingOptions() == null) {
            //seems like netbanking is not enabled
            separator.setVisibility(View.GONE);
            netBankingSpinner.setVisibility(View.GONE);

        } else {
            final List<Bank> banks = paymentOptions.getNetBankingOptions().getBanks();
            Collections.sort(banks);
            banks.add(0, new Bank("-1", "Select a bank"));

            BankListAdapter adapter = new BankListAdapter(this, banks);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            netBankingSpinner.setAdapter(adapter);

            netBankingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        return;
                    }

                    //User selected a Bank. Hence proceed to Juspay
                    String bankCode = banks.get(position).getId();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.URL, paymentOptions.getNetBankingOptions().getSubmissionURL());
                    bundle.putString(Constants.POST_DATA, paymentOptions.getNetBankingOptions().getPostData(bankCode));
                    CustomUIActivity.this.startPaymentActivity(bundle);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    private void proceedWithCard(GatewayOrder order, Card card) {
        dialog.show();

        ImojoService service = ServiceGenerator.getImojoService();
        final CardOptions cardOptions = order.getPaymentOptions().getCardOptions();
        Map<String, String> cardPaymentRequest = ObjectMapper.populateCardRequest(order, card, null, 0);

        Call<CardPaymentResponse> orderCall = service.collectCardPayment(cardOptions.getSubmissionURL(), cardPaymentRequest);
        orderCall.enqueue(new Callback<CardPaymentResponse>() {
            @Override
            public void onResponse(Call<CardPaymentResponse> call, final Response<CardPaymentResponse> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            final Bundle bundle = new Bundle();
                            bundle.putString(Constants.URL, response.body().getUrl());
                            bundle.putString(Constants.MERCHANT_ID, cardOptions.getSubmissionData().getMerchantID());
                            bundle.putString(Constants.ORDER_ID, cardOptions.getSubmissionData().getOrderID());
                            CustomUIActivity.this.startPaymentActivity(bundle);

                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    Logger.e(TAG, "Error response from card checkout call - " +
                                            response.errorBody().string());

                                } catch (IOException e) {
                                    Logger.e(TAG, "Error reading error response: " + e.getMessage());
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<CardPaymentResponse> call, final Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.e(TAG, "Card checkout failed due to - " + t.getMessage());
                    }
                });
            }
        });
    }

    private boolean cardValid(Card card) {
        if (!card.isCardValid()) {

            if (!card.isCardNameValid()) {
                showErrorToast("Card Holder's Name is invalid");
            }

            if (!card.isCardNumberValid()) {
                showErrorToast("Card Number is invalid");
            }

            if (!card.isDateValid()) {
                showErrorToast("Expiry date is invalid");
            }

            if (!card.isCVVValid()) {
                showErrorToast("CVV is invalid");
            }

            return false;
        }

        return true;
    }

    private void startPaymentActivity(Bundle bundle) {
        // Start the payment activity
        //Do not change this unless you know what you are doing
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra(Constants.PAYMENT_BUNDLE, bundle);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    private void showErrorToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
