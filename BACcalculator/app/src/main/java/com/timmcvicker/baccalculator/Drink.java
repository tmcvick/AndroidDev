package com.timmcvicker.baccalculator;

/**
 * Created by timmcvicker on 1/22/17.
 */

public class Drink {
    private double volumeInOz;
    private double proofAsPercentage;

    public Drink(double volumeInOz, double proofAsPercentage) {
        this.volumeInOz = volumeInOz;
        this.proofAsPercentage = proofAsPercentage;
    }

    public double getProofAsPercentage() {
        return proofAsPercentage;
    }

    public double getVolumeInOz() {
        return volumeInOz;
    }

    public double getGramsOfAlcohol() {
        return volumeInOz * proofAsPercentage * 0.789;
    }
}
