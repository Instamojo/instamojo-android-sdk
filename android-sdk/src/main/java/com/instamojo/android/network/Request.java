package com.instamojo.android.network;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.instamojo.android.BuildConfig;
import com.instamojo.android.Instamojo;
import com.instamojo.android.callbacks.JuspayRequestCallback;
import com.instamojo.android.callbacks.OrderRequestCallback;
import com.instamojo.android.callbacks.UPICallback;
import com.instamojo.android.helpers.CardValidator;
import com.instamojo.android.helpers.Constants;
import com.instamojo.android.helpers.Logger;
import com.instamojo.android.models.Card;
import com.instamojo.android.models.CardOptions;
import com.instamojo.android.models.EMIBank;
import com.instamojo.android.models.EMIOptions;
import com.instamojo.android.models.Errors;
import com.instamojo.android.models.NetBankingOptions;
import com.instamojo.android.models.Order;
import com.instamojo.android.models.UPIOptions;
import com.instamojo.android.models.UPISubmissionResponse;
import com.instamojo.android.models.Wallet;
import com.instamojo.android.models.WalletOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Network Request Class.
 */

public class Request {

    private Order order;
    private OrderRequestCallback orderRequestCallback;
    private JuspayRequestCallback juspayRequestCallback;
    private UPICallback upiCallback;
    private String virtualPaymentAddress;
    private UPISubmissionResponse upiSubmissionResponse;
    private Card card;
    private MODE mode;
    private String accessToken;
    private String orderID;

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new DefaultHeadersInterceptor())
            .build();

    /**
     * Network Request to create an order ID from Instamojo server.
     *
     * @param order                Order model with all the mandatory fields set.
     * @param orderRequestCallback Callback interface for the Asynchronous Network Call.
     */
    public Request(@NonNull Order order, @NonNull OrderRequestCallback orderRequestCallback) {
        this.mode = MODE.OrderCreate;
        this.order = order;
        this.orderRequestCallback = orderRequestCallback;
    }


    /**
     * Network Request to get order details from Juspay for JuspaySafeBrowser.
     *
     * @param order                 Order model with all the mandatory fields set.
     * @param card                  Card with all the proper validations done.
     * @param juspayRequestCallback Callback for Asynchronous network call.
     */
    public Request(@NonNull Order order, @NonNull Card card, @NonNull JuspayRequestCallback juspayRequestCallback) {
        this.mode = MODE.Juspay;
        this.card = card;
        this.order = order;
        this.juspayRequestCallback = juspayRequestCallback;
    }

    /**
     * Network request for UPISubmission Submission
     *
     * @param order                 {@link Order}
     * @param virtualPaymentAddress String
     * @param upiCallback           {@link UPICallback}
     */
    public Request(@NonNull Order order, @NonNull String virtualPaymentAddress, @NonNull UPICallback upiCallback) {
        this.mode = MODE.UPISubmission;
        this.order = order;
        this.virtualPaymentAddress = virtualPaymentAddress;
        this.upiCallback = upiCallback;
    }

    /**
     * Network Request to check the status of the transaction
     *
     * @param order                 {@link Order}
     * @param upiSubmissionResponse {@link UPISubmissionResponse}
     * @param upiCallback           {@link UPICallback}
     */
    public Request(@NonNull Order order, @NonNull UPISubmissionResponse upiSubmissionResponse, @NonNull UPICallback upiCallback) {
        this.mode = MODE.UPIStatusCheck;
        this.order = order;
        this.upiSubmissionResponse = upiSubmissionResponse;
        this.upiCallback = upiCallback;
    }

    /**
     * Network request to fetch the order
     *
     * @param accessToken          String
     * @param orderID              String
     * @param orderRequestCallback {@link OrderRequestCallback}
     */
    public Request(@NonNull String accessToken, @NonNull String orderID, @NonNull OrderRequestCallback orderRequestCallback) {
        this.mode = MODE.FetchOrder;
        this.accessToken = accessToken;
        this.orderID = orderID;
        this.orderRequestCallback = orderRequestCallback;
    }

    /**
     * Executes the call to the server and calls the callback with  {@link Exception} if failed.
     */
    public void execute() {
        switch (this.mode) {
            case OrderCreate:
                executeCreateOrder();
                break;
            case Juspay:
                executeJuspayRequest();
                break;
            case UPISubmission:
                executeUPIRequest();
                break;
            case UPIStatusCheck:
                executeUPIStatusCheck();
                break;
            case FetchOrder:
                executeFetchOrder();
                break;
            default:
                throw new RuntimeException("Unknown Mode");
        }
    }

    private void executeJuspayRequest() {
        //For maestro, add the default values if empty
        if (CardValidator.maestroCard(card.getCardNumber())) {
            if (card.getDate() == null || card.getDate().isEmpty()) {
                card.setDate("12/49");
            }

            if (card.getCvv() == null || card.getCvv().isEmpty()) {
                card.setDate("111");
            }
        }

        FormBody.Builder body = new FormBody.Builder()
                .add("order_id", order.getCardOptions().getOrderID())
                .add("merchant_id", order.getCardOptions().getMerchantID())
                .add("payment_method_type", "CARD")
                .add("card_number", card.getCardNumber())
                .add("card_exp_month", card.getMonth())
                .add("card_exp_year", card.getYear())
                .add("card_security_code", card.getCvv())
                .add("save_to_locker", card.canSaveCard() ? "true" : "false")
                .add("redirect_after_payment", "true")
                .add("format", "json");

        if (card.getCardHolderName() != null) {
            body.add("name_on_card", card.getCardHolderName());
        }

        if (order.getEmiOptions() != null
                && order.getEmiOptions().getSelectedBankCode() != null) {
            Logger.logDebug(this.getClass().getSimpleName(), "emi selected....");
            body.add("is_emi", "true");
            body.add("emi_bank", order.getEmiOptions().getSelectedBankCode());
            body.add("emi_tenure", String.valueOf(order.getEmiOptions().getSelectedTenure()));
        }

        final okhttp3.Request request = new okhttp3.Request.Builder()
                .url(order.getCardOptions().getUrl())
                .post(body.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.logError(this.getClass().getSimpleName(), "Error while making Juspay request - " + e.getMessage());
                juspayRequestCallback.onFinish(null, new Errors.ConnectionError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                String responseBody;
                try {
                    responseBody = response.body().string();
                    response.body().close();
                    Bundle bundle = parseJusPayResponse(responseBody);
                    juspayRequestCallback.onFinish(bundle, null);
                } catch (IOException e) {
                    Logger.logError(this.getClass().getSimpleName(), "Error while making Juspay request - " + e.getMessage());
                    juspayRequestCallback.onFinish(null, new Errors.ServerError(e.getMessage()));
                } catch (JSONException e) {
                    Logger.logError(this.getClass().getSimpleName(), "Error while making Juspay request - " + e.getMessage());
                    juspayRequestCallback.onFinish(null, e);
                }
            }
        });

    }

    private Bundle parseJusPayResponse(String responseBody) throws JSONException {
        JSONObject responseObject = new JSONObject(responseBody);
        String url = responseObject.getJSONObject("payment").getJSONObject("authentication").getString("url");
        final Bundle args = new Bundle();
        args.putString(Constants.URL, url);
        args.putString(Constants.MERCHANT_ID, order.getCardOptions().getMerchantID());
        args.putString(Constants.ORDER_ID, order.getCardOptions().getOrderID());
        return args;
    }

    private void executeFetchOrder() {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Urls.getOrderFetchURL(orderID))
                .header("Authorization", "Bearer " + accessToken)
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.logError(this.getClass().getSimpleName(), "Error while making Instamojo request - " + e.getMessage());
                orderRequestCallback.onFinish(null, new Errors.ConnectionError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response r) throws IOException {
                String responseBody = "";
                try {
                    responseBody = r.body().string();
                    r.body().close();
                    parseOrder(responseBody);
                    orderRequestCallback.onFinish(order, null);
                } catch (IOException e) {
                    Logger.logError(this.getClass().getSimpleName(), "Error while making Instamojo request - " + e.getMessage());
                    orderRequestCallback.onFinish(order, new Errors.ServerError(e.getMessage()));
                } catch (JSONException e) {
                    Logger.logError(this.getClass().getSimpleName(), "Error while making Instamojo request - " + e.getMessage());
                    orderRequestCallback.onFinish(order, Errors.getAppropriateError(responseBody));
                }
            }
        });
    }

    private void executeCreateOrder() {
        FormBody.Builder builder = new FormBody.Builder()
                .add("name", order.getBuyerName())
                .add("email", order.getBuyerEmail())
                .add("amount", order.getAmount())
                .add("description", order.getDescription())
                .add("phone", order.getBuyerPhone())
                .add("currency", order.getCurrency())
                .add("transaction_id", order.getTransactionID())
                .add("redirect_url", order.getRedirectionUrl())
                .add("advanced_payment_options", "true");
        if (order.getWebhook() != null) {
            builder.add("webhook_url", order.getWebhook());
        }
        RequestBody body = builder.build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(Urls.getOrderCreateUrl())
                .header("Authorization", "Bearer " + order.getAuthToken())
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.logError(this.getClass().getSimpleName(), "Error while making Instamojo request - " + e.getMessage());
                orderRequestCallback.onFinish(order, new Errors.ConnectionError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response r) {
                String responseBody = "";
                try {
                    responseBody = r.body().string();
                    r.body().close();
                    JSONObject responseObject = new JSONObject(responseBody);
                    JSONObject orderObject = responseObject.getJSONObject("order");
                    order.setId(orderObject.getString("id"));
                    order.setTransactionID(orderObject.getString("transaction_id"));
                    updateTransactionDetails(responseObject);
                    orderRequestCallback.onFinish(order, null);
                } catch (IOException e) {
                    Logger.logError(this.getClass().getSimpleName(), "Error while making Instamojo request - " + e.getMessage());
                    orderRequestCallback.onFinish(order, new Errors.ServerError(e.getMessage()));
                } catch (JSONException e) {
                    Logger.logError(this.getClass().getSimpleName(), "Error while making Instamojo request - " + e.getMessage());
                    orderRequestCallback.onFinish(order, Errors.getAppropriateError(responseBody));
                }
            }
        });
    }

    private void parseOrder(String responseBody) throws JSONException {
        JSONObject responseObject = new JSONObject(responseBody);
        JSONObject orderObject = responseObject.getJSONObject("order");
        String id = orderObject.getString("id");
        String transactionID = orderObject.getString("transaction_id");
        String buyerName = orderObject.getString("name");
        String buyerEmail = orderObject.getString("email");
        String phone = orderObject.getString("phone");
        String amount = orderObject.getString("amount");
        String description = orderObject.getString("description");
        String currency = orderObject.getString("currency");
        String redirectionURL = orderObject.getString("redirect_url");
        String webhookURL = orderObject.getString("webhook_url");
        order = new Order(accessToken, transactionID, buyerName, buyerEmail, phone, amount, description);
        order.setId(id);
        order.setCurrency(currency);
        order.setRedirectionUrl(redirectionURL);
        order.setWebhook(webhookURL);
        updateTransactionDetails(responseObject);
    }

    private void updateTransactionDetails(JSONObject responseObject) throws JSONException {

        JSONObject paymentOptionsObject = responseObject.getJSONObject("payment_options");
        if (paymentOptionsObject.has("card_options") && !paymentOptionsObject.isNull("card_options")) {
            JSONObject cardOptions = paymentOptionsObject.getJSONObject("card_options");
            JSONObject submissionData = cardOptions.getJSONObject("submission_data");
            String merchantID = submissionData.getString("merchant_id");
            String orderID = submissionData.getString("order_id");
            String paymentURL = cardOptions.getString("submission_url");
            order.setCardOptions(new CardOptions(orderID, merchantID, paymentURL));
        }

        if (paymentOptionsObject.has("netbanking_options") && !paymentOptionsObject.isNull("netbanking_options")) {
            JSONObject netbankingOptions = paymentOptionsObject.getJSONObject("netbanking_options");
            String nbURL = netbankingOptions.getString("submission_url");
            JSONArray banksArray = netbankingOptions.getJSONArray("choices");
            LinkedHashMap<String, String> banks = new LinkedHashMap<>();
            JSONObject bank;
            for (int i = 0; i < banksArray.length(); i++) {
                bank = banksArray.getJSONObject(i);
                banks.put(bank.getString("name"), bank.getString("id"));
            }
            if (banks.size() > 0) {
                order.setNetBankingOptions(new NetBankingOptions(nbURL, banks));
            }
        }

        if (paymentOptionsObject.has("emi_options") && !paymentOptionsObject.isNull("emi_options")) {
            JSONObject emiOptionsRaw = paymentOptionsObject.getJSONObject("emi_options");
            JSONArray emiListRaw = emiOptionsRaw.getJSONArray("emi_list");
            EMIBank emiBank;
            JSONObject emiOptionRaw;
            JSONArray ratesRaw;
            JSONObject rateRaw;
            ArrayList<EMIBank> emis = new ArrayList<>();
            for (int i = 0; i < emiListRaw.length(); i++) {
                emiOptionRaw = emiListRaw.getJSONObject(i);
                String bankName = emiOptionRaw.getString("bank_name");
                String bankCode = emiOptionRaw.getString("bank_code");
                Map<Integer, BigDecimal> rates = new HashMap<>();
                ratesRaw = emiOptionRaw.getJSONArray("rates");
                for (int j = 0; j < ratesRaw.length(); j++) {
                    rateRaw = ratesRaw.getJSONObject(j);
                    int tenure = rateRaw.getInt("tenure");
                    String interest = rateRaw.getString("interest");
                    rates.put(tenure, new BigDecimal(interest));
                }
                LinkedList<Map.Entry<Integer, BigDecimal>> ratesList = new LinkedList<>(rates.entrySet());
                Collections.sort(ratesList, new Comparator<Map.Entry<Integer, BigDecimal>>() {
                    @Override
                    public int compare(Map.Entry<Integer, BigDecimal> lhs, Map.Entry<Integer, BigDecimal> rhs) {
                        return lhs.getKey() - rhs.getKey();
                    }
                });
                LinkedHashMap<Integer, BigDecimal> sortedRates = new LinkedHashMap<>();
                for (Map.Entry<Integer, BigDecimal> entry : ratesList) {
                    sortedRates.put(entry.getKey(), entry.getValue());
                }

                if (sortedRates.size() > 0) {
                    emiBank = new EMIBank(bankName, bankCode, sortedRates);
                    emis.add(emiBank);
                }
            }
            String url = emiOptionsRaw.getString("submission_url");
            JSONObject submissionData = emiOptionsRaw.getJSONObject("submission_data");
            String merchantID = submissionData.getString("merchant_id");
            String orderID = submissionData.getString("order_id");
            if (emis.size() > 0) {
                order.setEmiOptions(new EMIOptions(merchantID, orderID, url, emis));
            }
        }

        if (paymentOptionsObject.has("wallet_options") && !paymentOptionsObject.isNull("wallet_options")) {
            JSONObject walletOptionsObject = paymentOptionsObject.getJSONObject("wallet_options");
            JSONArray walletChoices = walletOptionsObject.getJSONArray("choices");
            JSONObject walletObject;
            ArrayList<Wallet> wallets = new ArrayList<>();
            for (int i = 0; i < walletChoices.length(); i++) {
                walletObject = walletChoices.getJSONObject(i);
                String name = walletObject.getString("name");
                String walletID = walletObject.getString("id");
                String walletImage = walletObject.getString("image");
                wallets.add(new Wallet(name, walletImage, walletID));
            }

            String url = walletOptionsObject.getString("submission_url");

            if (wallets.size() > 0) {
                order.setWalletOptions(new WalletOptions(url, wallets));
            }
        }

        if (paymentOptionsObject.has("upi_options") && !paymentOptionsObject.isNull("upi_options")) {
            JSONObject upiOptionsRaw = paymentOptionsObject.getJSONObject("upi_options");
            UPIOptions upiOptions = new UPIOptions(upiOptionsRaw.getString("submission_url"));
            order.setUpiOptions(upiOptions);
        }
    }

    private void executeUPIRequest() {
        RequestBody body = new FormBody.Builder()
                .add("virtual_address", this.virtualPaymentAddress)
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(order.getUpiOptions().getUrl())
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.logError(this.getClass().getSimpleName(),
                        "Error while making UPI Submission request - " + e.getMessage());
                upiCallback.onSubmission(null, e);
            }

            @Override
            public void onResponse(Call call, Response r) {
                String responseBody = "";
                try {
                    responseBody = r.body().string();
                    r.body().close();
                    upiCallback.onSubmission(parseUPIResponse(responseBody), null);
                } catch (IOException | JSONException e) {
                    Logger.logError(this.getClass().getSimpleName(),
                            "Error while making UPI Submission request - " + e.getMessage());
                    upiCallback.onSubmission(null, e);
                }
            }
        });
    }

    private UPISubmissionResponse parseUPIResponse(String responseString) throws JSONException {
        JSONObject responseObject = new JSONObject(responseString);
        String paymentID = responseObject.getString("payment_id");
        int statusCode = responseObject.getInt("status_code");
        String payerVirtualAddress = responseObject.getString("payer_virtual_address");
        String payeeVirtualAddress = responseObject.getString("payee_virtual_address");
        String statusCheckURL = responseObject.getString("status_check_url");
        String upiBank = responseObject.getString("upi_bank");
        String statusMessage = responseObject.getString("status_message");

        return new UPISubmissionResponse(paymentID, statusCode, payerVirtualAddress,
                payeeVirtualAddress, statusCheckURL, upiBank, statusMessage);
    }

    private void executeUPIStatusCheck() {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(upiSubmissionResponse.getStatusCheckURL())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.logError(this.getClass().getSimpleName(),
                        "Error while making UPI Status Check request - " + e.getMessage());
                upiCallback.onStatusCheckComplete(null, false, e);
            }

            @Override
            public void onResponse(Call call, Response r) {
                String responseBody = "";
                try {
                    responseBody = r.body().string();
                    r.body().close();
                    boolean transactionComplete = isTransactionComplete(responseBody);
                    if (!transactionComplete) {
                        upiCallback.onStatusCheckComplete(null, false, null);
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.ORDER_ID, order.getId());
                    bundle.putString(Constants.TRANSACTION_ID, order.getTransactionID());
                    bundle.putString(Constants.PAYMENT_ID, upiSubmissionResponse.getPaymentID());
                    upiCallback.onStatusCheckComplete(bundle, true, null);
                } catch (IOException | JSONException e) {
                    Logger.logError(this.getClass().getSimpleName(),
                            "Error while making UPI Status Check request - " + e.getMessage());
                    upiCallback.onStatusCheckComplete(null, false, e);
                }
            }
        });
    }

    private boolean isTransactionComplete(String responseBody) throws JSONException {
        JSONObject responseObject = new JSONObject(responseBody);
        return responseObject.getInt("status_code") != Constants.PENDING_PAYMENT;
    }

    private enum MODE {
        OrderCreate,
        FetchOrder,
        Juspay,
        UPISubmission,
        UPIStatusCheck
    }

    private static class DefaultHeadersInterceptor implements Interceptor {
        private String userAgent;
        private String referer;

        @Override
        public Response intercept(Chain chain) throws IOException {
            return chain.proceed(chain.request()
                    .newBuilder()
                    .header("User-Agent", getUserAgent())
                    .header("Referer", getReferer())
                    .build());
        }

        private String getUserAgent() {
            if (this.userAgent == null || this.userAgent.length() == 0) {
                userAgent = "instamojo-android/" + BuildConfig.VERSION_NAME
                        + " android/" + Build.VERSION.RELEASE
                        + " " + Build.BRAND + "/" + Build.MODEL;
            }

            return this.userAgent;
        }

        private String getReferer() {
            if (this.referer == null || this.referer.length() == 0) {
                if (!Instamojo.isInitialised()) {
                    return "";
                }

                Context appContext = Instamojo.getInstance().getAppContext();

                String packageName = appContext.getPackageName();
                this.referer = "android-app://" + packageName;

                try {
                    this.referer += "/" + appContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    Logger.logError("Request", "Unable to get version of the current application.");
                }
            }
            return this.referer;
        }
    }
}
