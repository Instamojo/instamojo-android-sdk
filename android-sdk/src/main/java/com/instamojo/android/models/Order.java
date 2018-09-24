package com.instamojo.android.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.instamojo.android.network.Urls;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Order Class to hold the details of a Order.
 */

public class Order implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel in) {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
    private String id;
    private String transactionID;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String amount;
    private String description;
    private String currency;
    private String redirectionUrl;
    private String webhook;
    private String authToken;
    private CardOptions cardOptions;
    private NetBankingOptions netBankingOptions;
    private EMIOptions emiOptions;
    private WalletOptions walletOptions;
    private UPIOptions upiOptions;

    /**
     * Order model with all the Mandatory Parameters passed.
     *
     * @param buyerName     Name of the buyer.
     * @param buyerEmail    Email of the buyer.
     * @param buyerPhone    Phone number of the buyer.
     * @param amount        Order amount.
     * @param description   Order description.
     * @param authToken     App access token generated using client id and secret.
     * @param transactionID A unique Transaction ID generated on the developers side
     */
    public Order(@NonNull String authToken, @NonNull String transactionID, @NonNull String buyerName, @NonNull String buyerEmail, @NonNull String buyerPhone,
                 @NonNull String amount, @NonNull String description) {
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.buyerPhone = buyerPhone;
        this.amount = amount;
        this.description = description;
        this.currency = "INR";
        this.authToken = authToken;
        this.transactionID = transactionID;
        this.redirectionUrl = Urls.getDefaultRedirectUrl();
    }

    protected Order(Parcel in) {
        id = in.readString();
        transactionID = in.readString();
        buyerName = in.readString();
        buyerEmail = in.readString();
        buyerPhone = in.readString();
        amount = in.readString();
        description = in.readString();
        currency = in.readString();
        redirectionUrl = in.readString();
        webhook = in.readString();
        authToken = in.readString();
        cardOptions = in.readParcelable(CardOptions.class.getClassLoader());
        netBankingOptions = in.readParcelable(NetBankingOptions.class.getClassLoader());
        emiOptions = in.readParcelable(EMIOptions.class.getClassLoader());
        walletOptions = in.readParcelable(WalletOptions.class.getClassLoader());
        upiOptions = in.readParcelable(UPIOptions.class.getClassLoader());
    }

    private static boolean isUrlValid(String url) {
        try {
            URL parsedUrl = new URL(url);
            if (parsedUrl.getQuery() != null) {
                return false;
            }
        } catch (MalformedURLException e) {
            return false;
        }

        return true;
    }

    /**
     * @return buyer name if available else null.
     */
    public String getBuyerName() {
        return buyerName;
    }

    /**
     * @param buyerName Buyer Name for this transaction. Must not be null.
     */
    public void setBuyerName(@NonNull String buyerName) {
        this.buyerName = buyerName;
    }

    /**
     * @return buyer email if available else null.
     */
    public String getBuyerEmail() {
        return buyerEmail;
    }

    /**
     * @param buyerEmail Email of the buyer for this transaction. Must not be null.
     */
    public void setBuyerEmail(@NonNull String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    /**
     * @return buyer phone for this transaction.
     */
    public String getBuyerPhone() {
        return buyerPhone;
    }

    /**
     * @param buyerPhone Phone number of the buyer for this transaction. Must not be null.
     */
    public void setBuyerPhone(@NonNull String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    /**
     * @return transaction amount if available else null.
     */
    public String getAmount() {
        return amount;
    }

    /**
     * @param amount Order amount for this Order. Must not be null.
     */
    public void setAmount(@NonNull String amount) {
        this.amount = amount;
    }

    /**
     * @return Purpose of the Order if available else null.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description Purpose of this transaction. Must not be null.
     */
    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    /**
     * @return type of currency. Ex: INR(Default).
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @return web hook for this transaction.
     */
    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    /**
     * @param redirectionUrl Web hook for this order. Will be redirected to this URL
     *                       after payment. Should not be called unless you know what you are doing.
     */
    public void setRedirectionUrl(@NonNull String redirectionUrl) {
        this.redirectionUrl = redirectionUrl.trim();
    }

    /**
     * @return Webhook for this Order
     */
    public String getWebhook() {
        return webhook;
    }

    /**
     * Sets the webhook for this order
     *
     * @param webhookUrl String.
     */
    public void setWebhook(@NonNull String webhookUrl) {
        this.webhook = webhookUrl.trim();
    }

    /**
     * @return authToken generated for this URL.
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * @param authToken Auth token generated using clients secret keys.
     */
    public void setAuthToken(@NonNull String authToken) {
        this.authToken = authToken;
    }

    /**
     * @return Order ID if available else null.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id Order ID of this transaction. Must not be null.
     */
    public void setId(@NonNull String id) {
        this.id = id;
    }

    /**
     * @return {@link CardOptions} if available. Else null.
     */
    public CardOptions getCardOptions() {
        return cardOptions;
    }

    /**
     * @param cardOptions Debit card options for this transaction. Can be null.
     */
    public void setCardOptions(CardOptions cardOptions) {
        this.cardOptions = cardOptions;
    }

    /**
     * @return {@link NetBankingOptions} if available. Else null.
     */
    public NetBankingOptions getNetBankingOptions() {
        return netBankingOptions;
    }

    /**
     * @param netBankingOptions Netbanking options for this Order. Can be null.
     */
    public void setNetBankingOptions(NetBankingOptions netBankingOptions) {
        this.netBankingOptions = netBankingOptions;
    }

    /**
     * @param currency  Currency for this order
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * EMI Options if enabled for the seller
     *
     * @return {@link EMIOptions}
     */
    public EMIOptions getEmiOptions() {
        return emiOptions;
    }

    /**
     * Set EMI Options for this order if enabled for seller
     *
     * @param emiOptions {@link EMIOptions}
     */
    public void setEmiOptions(EMIOptions emiOptions) {
        this.emiOptions = emiOptions;
    }

    /**
     * @return Unique TransactionID generated for this order
     */
    public String getTransactionID() {
        return transactionID;
    }

    /**
     * @param transactionID Unique TransactionID generated for this order
     */
    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    /**
     * Get wallet options for this order if enabled for seller
     *
     * @return walletOptions {@link WalletOptions}
     */
    public WalletOptions getWalletOptions() {
        return walletOptions;
    }

    /**
     * Set wallet options for this order
     *
     * @param walletOptions {@link WalletOptions}
     */
    public void setWalletOptions(WalletOptions walletOptions) {
        this.walletOptions = walletOptions;
    }

    /**
     * Get UPISubmission options for this order
     *
     * @return UPIOptions
     */
    public UPIOptions getUpiOptions() {
        return upiOptions;
    }

    /**
     * Set UPISubmission options for this order
     *
     * @param upiOptions {@link UPIOptions}
     */
    public void setUpiOptions(UPIOptions upiOptions) {
        this.upiOptions = upiOptions;
    }

    /**
     * @return false if one of the mandatory fields is invalid. Else true.
     */
    public boolean isValid() {
        return isValidName() && isValidEmail() && isValidPhone() && isValidAmount()
                && isValidDescription() && isValidTransactionID() && isValidRedirectURL()
                && isValidWebhook();
    }

    /**
     * @return false if the buyer name is empty or has greater than 100 characters. Else true.
     */
    public boolean isValidName() {
        return !buyerName.trim().isEmpty() && buyerName.length() <= 100;

    }

    /**
     * @return false if the buyer email is empty or has greater than 75 characters. Else true.
     */
    public boolean isValidEmail() {
        return !buyerEmail.trim().isEmpty() && buyerEmail.length() <= 75;
    }

    /**
     * @return false if the phone number is empty. Else true.
     */
    public boolean isValidPhone() {
        return !buyerPhone.trim().isEmpty();
    }

    /**
     * @return false if the amount is empty or less than Rs. 9 or has more than 2 decimal places.
     */
    public boolean isValidAmount() {
        if (amount.trim().isEmpty()) {
            return false;
        }

        if (amount.contains(".")) {
            String[] parts = amount.split("\\.");
            if (parts.length != 2) {
                return false;
            }

            if (parts[1].length() > 2) {
                return false;
            }
        }

        try {
            float value = Float.parseFloat(amount);
            if (value < 9) {
                return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * @return false if the description is empty or has greater than 255 characters. Else true.
     */
    public boolean isValidDescription() {
        return !description.trim().isEmpty() && description.length() <= 255;
    }

    /**
     * @return false if the transaction ID is empty or has greater than 64 characters.
     */
    public boolean isValidTransactionID() {
        return !transactionID.trim().isEmpty() && transactionID.length() <= 64;
    }

    /**
     * @return false if the redirection URL is empty or contains any query parameters.
     */
    public boolean isValidRedirectURL() {
        return this.redirectionUrl != null && isUrlValid(this.redirectionUrl);

    }

    /**
     * @return false if webhook is set and not a valid url or has query parameters
     */
    public boolean isValidWebhook() {
        return this.webhook == null || isUrlValid(this.webhook);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(transactionID);
        dest.writeString(buyerName);
        dest.writeString(buyerEmail);
        dest.writeString(buyerPhone);
        dest.writeString(amount);
        dest.writeString(description);
        dest.writeString(currency);
        dest.writeString(redirectionUrl);
        dest.writeString(webhook);
        dest.writeString(authToken);
        dest.writeParcelable(cardOptions, flags);
        dest.writeParcelable(netBankingOptions, flags);
        dest.writeParcelable(emiOptions, flags);
        dest.writeParcelable(walletOptions, flags);
        dest.writeParcelable(upiOptions, flags);
    }
}