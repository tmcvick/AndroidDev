package com.timmcvicker.baccalculator;

import java.io.Serializable;

/**
 * Created by timmcvicker on 1/22/17.
 */

public class Drink implements Serializable{
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
        return volumeInOz * proofAsPercentage * 5.14;
    }
}
