package com.instamojo.android.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;

public class WalletOptions implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WalletOptions> CREATOR = new Parcelable.Creator<WalletOptions>() {
        @Override
        public WalletOptions createFromParcel(Parcel in) {
            return new WalletOptions(in);
        }

        @Override
        public WalletOptions[] newArray(int size) {
            return new WalletOptions[size];
        }
    };
    private String url;
    private ArrayList<Wallet> wallets = new ArrayList<>();

    public WalletOptions(String url, ArrayList<Wallet> wallets) {
        this.url = url;
        this.wallets = wallets;
    }

    protected WalletOptions(Parcel in) {
        url = in.readString();
        int walletsSize = in.readInt();
        if (walletsSize == 0) {
            return;
        }
        wallets = new ArrayList<>();
        for (int i = 0; i < walletsSize; i++) {
            wallets.add((Wallet) in.readParcelable(Wallet.class.getClassLoader()));
        }
    }

    public String getUrl() {
        return url;
    }

    public ArrayList<Wallet> getWallets() {
        return wallets;
    }

    /**
     * PostData to be posted with Wallet URL.
     *
     * @param walletID  Wallet ID of the wallet user selected.
     * @return string with form query format.
     */
    public String getPostData(@NonNull String walletID) {
        return "wallet_id=" + walletID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        if (wallets.size() < 1) {
            dest.writeInt(0);
            return;
        }

        dest.writeInt(wallets.size());
        for (Wallet wallet : wallets) {
            dest.writeParcelable(wallet, flags);
        }
    }
}