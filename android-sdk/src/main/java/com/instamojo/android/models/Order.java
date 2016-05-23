package com.instamojo.android.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Order Class to hold the details of a Order.
 *
 *
 * @author vedhavyas
 * @version 1.0
 * @since 14/03/16
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
    private String mode;
    private String authToken;
    private String resourceURI;
    private CardOptions cardOptions;
    private NetBankingOptions netBankingOptions;

    /**
     * Order model with all the Mandatory Parameters passed.
     *
     * @param buyerName Name of the buyer.
     * @param buyerEmail Email of the buyer.
     * @param buyerPhone Phone number of the buyer.
     * @param amount Order amount.
     * @param description Order description.
     * @param authToken App access token generated using client id and secret.
     * @param transactionID A unique Transaction ID generated on the developers side
     *
     */
    public Order(@NonNull String authToken, @NonNull String transactionID, @NonNull String buyerName, @NonNull String buyerEmail, @NonNull String buyerPhone,
                 @NonNull String amount, @NonNull String description) {
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.buyerPhone = buyerPhone;
        this.amount = amount;
        this.description = description;
        this.currency = "INR";
        this.mode = "Android_SDK";
        this.authToken = authToken;
        this.transactionID = transactionID;
        this.redirectionUrl = "https://www.instamojo.com/integrations/android/redirect/";
    }

    @SuppressWarnings("unchecked")
    protected Order(Parcel in) {
        id = in.readString();
        transactionID = in.readString();
        buyerName = in.readString();
        buyerEmail = in.readString();
        buyerPhone = in.readString();
        amount = in.readString();
        description = in.readString();
        currency = in.readString();
        mode = in.readString();
        redirectionUrl = in.readString();
        authToken = in.readString();
        resourceURI = in.readString();
        cardOptions = in.readParcelable(CardOptions.class.getClassLoader());
        netBankingOptions = in.readParcelable(NetBankingOptions.class.getClassLoader());
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
        dest.writeString(mode);
        dest.writeString(redirectionUrl);
        dest.writeString(authToken);
        dest.writeString(resourceURI);
        dest.writeParcelable(cardOptions, flags);
        dest.writeParcelable(netBankingOptions, flags);
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
     * @return Mode of the Order.
     */
    public String getMode() {
        return mode;
    }

    /**
     * @return web hook for this transaction.
     */
    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    /**
     * @param redirectionUrl Web hook for this order. Will be redirected to this URL
     *                after payment. Should not be called unless you know what you are doing.
     */
    public void setRedirectionUrl(@NonNull String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
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
     * @return resourceURI of the Seller.
     */
    public String getResourceURI() {
        return resourceURI;
    }

    /**
     * @param resourceURI resource URI of the seller. Must not be null.
     */
    public void setResourceURI(@NonNull String resourceURI) {
        this.resourceURI = resourceURI;
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

    @Override
    public int describeContents() {
        return 0;
    }

}