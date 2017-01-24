package com.timmcvicker.baccalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public void submitGuess(View view) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public double findGramsOfAlcoholConsumed(List<Drink> drinks) {
        double totalGrams = 0.0;
        for (Drink drink : drinks) {
            totalGrams += drink.getGramsOfAlcohol();
        }

        return totalGrams;
    }

    public double findRawBAC(List<Drink> drinksConsumed, Person person) {
        double personFactor = person.calculateFactor();

        return (findGramsOfAlcoholConsumed(drinksConsumed) / personFactor) * 100;
    }

    public double findWeightedBAC(List<Drink> drinksConsumed, Person person, double timeInHours) {
        double rawBAC = findRawBAC(drinksConsumed, person);
        double timeFactor = timeInHours * 0.015;

        return rawBAC - timeInHours;
    }
}
