package com.instamojo.android.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class Bank implements Parcelable, Comparable<Bank> {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    public Bank(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected Bank(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public static final Creator<Bank> CREATOR = new Creator<Bank>() {
        @Override
        public Bank createFromParcel(Parcel in) {
            return new Bank(in);
        }

        @Override
        public Bank[] newArray(int size) {
            return new Bank[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
    }

    @Override
    public String toString() {
        return "Bank{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int compareTo(@NonNull Bank other) {
        return this.name.compareTo(other.name);
    }
}
