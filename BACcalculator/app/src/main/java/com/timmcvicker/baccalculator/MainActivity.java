package com.timmcvicker.baccalculator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public void goToInfo(View view) {
        Intent intentToInfo = new Intent(this, InfoActivity.class);
        startActivity(intentToInfo);
    }

    public void submitGuess(View view) {
        Intent mainToPersonIntent = new Intent(this, PersonActivity.class);
        EditText guessEditText = (EditText) findViewById(R.id.bacGuess);
        double guess = Double.parseDouble(guessEditText.getText().toString());
        mainToPersonIntent.putExtra("guess", guess);
        startActivity(mainToPersonIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private static double findGramsOfAlcoholConsumed(List<Drink> drinks) {
        double totalGrams = 0.0;
        for (Drink drink : drinks) {
            totalGrams += drink.getGramsOfAlcohol();
        }

        return totalGrams;
    }

    private static double findRawBAC(List<Drink> drinksConsumed, Person person) {
        double personFactor = person.calculateFactor();

        return (findGramsOfAlcoholConsumed(drinksConsumed) / (personFactor*100));
    }

    public static double findWeightedBAC(List<Drink> drinksConsumed, Person person, double timeInHours) {
        double rawBAC = findRawBAC(drinksConsumed, person);
        double timeFactor = timeInHours * 0.015;

        return rawBAC - timeFactor;
    }
}
