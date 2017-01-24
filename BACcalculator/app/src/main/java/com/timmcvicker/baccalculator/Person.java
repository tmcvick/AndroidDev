package com.timmcvicker.baccalculator;

/**
 * Created by timmcvicker on 1/22/17.
 */

public class Person {
    private Boolean isMale;
    private double weightInPounds;

    public Person(Boolean isMale, double weightInPounds) {
        this.isMale = isMale;
        this.weightInPounds = weightInPounds;
    }

    public double calculateFactor() {
        if (isMale) {
            return 0.68 * weightInPounds * 454;
        } else {
            return 0.55 * weightInPounds * 454;
        }
    }
}
