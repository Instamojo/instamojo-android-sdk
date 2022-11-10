package com.instamojo.android.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WalletOptions implements Parcelable {

    @SerializedName("submission_url")
    private String submissionURL;

    @SerializedName("choices")
    private List<Wallet> wallets;

    protected WalletOptions(Parcel in) {
        submissionURL = in.readString();
        wallets = in.createTypedArrayList(Wallet.CREATOR);
    }

    public static final Creator<WalletOptions> CREATOR = new Creator<WalletOptions>() {
        @Override
        public WalletOptions createFromParcel(Parcel in) {
            return new WalletOptions(in);
        }

        @Override
        public WalletOptions[] newArray(int size) {
            return new WalletOptions[size];
        }
    };

    public String getSubmissionURL() {
        return submissionURL;
    }

    public void setSubmissionURL(String submissionURL) {
        this.submissionURL = submissionURL;
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public void setWallets(List<Wallet> wallets) {
        this.wallets = wallets;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(submissionURL);
        parcel.writeTypedList(wallets);
    }

    /**
     * PostData to be posted with Wallet URL.
     *
     * @param walletID Wallet ID of the wallet user selected.
     * @return string with form query format.
     */
    public String getPostData(@NonNull String walletID) {
        return "wallet_id=" + walletID;
    }

    @Override
    public String toString() {
        return "WalletOptions{" +
                "submissionURL='" + submissionURL + '\'' +
                ", wallets=" + wallets +
                '}';
    }
}