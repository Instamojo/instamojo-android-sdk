package com.instamojo.android.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Netbanking options details for a transaction.
 */
public class NetBankingOptions implements Parcelable {

    @SerializedName("choices")
    private List<Bank> banks;

    @SerializedName("submission_data")
    private SubmissionData submissionData;

    @SerializedName("submission_url")
    private String submissionURL;

    protected NetBankingOptions(Parcel in) {
        banks = in.createTypedArrayList(Bank.CREATOR);
        submissionData = in.readParcelable(SubmissionData.class.getClassLoader());
        submissionURL = in.readString();
    }

    public static final Creator<NetBankingOptions> CREATOR = new Creator<NetBankingOptions>() {
        @Override
        public NetBankingOptions createFromParcel(Parcel in) {
            return new NetBankingOptions(in);
        }

        @Override
        public NetBankingOptions[] newArray(int size) {
            return new NetBankingOptions[size];
        }
    };

    public List<Bank> getBanks() {
        return banks;
    }

    public void setBanks(List<Bank> banks) {
        this.banks = banks;
    }

    public SubmissionData getSubmissionData() {
        return submissionData;
    }

    public void setSubmissionData(SubmissionData submissionData) {
        this.submissionData = submissionData;
    }

    public String getSubmissionURL() {
        return submissionURL;
    }

    public void setSubmissionURL(String submissionURL) {
        this.submissionURL = submissionURL;
    }

    /**
     * PostData to be posted with Netbanking URL.
     *
     * @param bankCode Bank code of the Bank user selected.
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(banks);
        parcel.writeParcelable(submissionData, i);
        parcel.writeString(submissionURL);
    }

    @Override
    public String toString() {
        return "NetBankingOptions{" +
                "banks=" + banks +
                ", submissionData=" + submissionData +
                ", submissionURL='" + submissionURL + '\'' +
                '}';
    }
}
