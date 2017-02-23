package com.timmcvicker.baccalculator;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by timmcvicker on 1/22/17.
 */

public class Person implements Serializable, Parcelable {
    private Boolean isMale;
    private double weightInPounds;

    public Person(Boolean isMale, double weightInPounds) {
        this.isMale = isMale;
        this.weightInPounds = weightInPounds;
    }

    public double calculateFactor() {
        if (isMale) {
            return weightInPounds * .73;
        } else {
            return weightInPounds * .66;
        }
    }

    protected Person(Parcel in) {
        byte isMaleVal = in.readByte();
        isMale = isMaleVal == 0x02 ? null : isMaleVal != 0x00;
        weightInPounds = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (isMale == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (isMale ? 0x01 : 0x00));
        }
        dest.writeDouble(weightInPounds);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
}