package com.instamojo.android.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Netbanking options details for a transaction.
 */
public class NetBankingOptions implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NetBankingOptions> CREATOR = new Parcelable.Creator<NetBankingOptions>() {
        @Override
        public NetBankingOptions createFromParcel(Parcel in) {
            return new NetBankingOptions(in);
        }

        @Override
        public NetBankingOptions[] newArray(int size) {
            return new NetBankingOptions[size];
        }
    };
    private String url;
    private LinkedHashMap<String, String> banks = new LinkedHashMap<>();

    /**
     * Constructor for Net Banking options.
     *
     * @param url   Url for Net Banking Options. Must not be null.
     * @param banks HashMap with Bank Code and Bank Name. Must not be null or empty.
     */
    public NetBankingOptions(@NonNull String url, @NonNull LinkedHashMap<String, String> banks) {
        this.url = url;
        this.banks = banks;
    }

    @SuppressWarnings("unchecked")
    protected NetBankingOptions(Parcel in) {
        url = in.readString();
        int size = in.readInt();
        if (size == 0) {
            return;
        }

        banks = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            banks.put(key, value);
        }
    }

    /**
     * @return Netbanking URl.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return HashMap of BankCode and BanksList.
     */
    public HashMap<String, String> getBanks() {
        return banks;
    }

    /**
     * PostData to be posted with Netbanking URL.
     *
     * @param bankCode  Bank code of the Bank user selected.
     * @return string with form query format.
     */
    public String getPostData(@NonNull String bankCode) {
        return "bank_code=" + bankCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        if (banks.size() < 1) {
            dest.writeInt(0);
            return;
        }
        dest.writeInt(banks.size());
        for (HashMap.Entry<String, String> entry : banks.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }
}
