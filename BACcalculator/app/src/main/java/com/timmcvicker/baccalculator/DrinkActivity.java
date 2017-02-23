package com.timmcvicker.baccalculator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by timmcvicker on 1/31/17.
 */

public class DrinkActivity extends AppCompatActivity {
    private List<Drink> drinkList;

    public void goToInfo(View view) {
        Intent intentToInfo = new Intent(this, InfoActivity.class);
        startActivity(intentToInfo);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drinks);
        drinkList = new ArrayList<>();
    }

    public void addDrink(View view) {
        EditText volumeForm = (EditText) findViewById(R.id.volumeEdit);
        double volume = Double.parseDouble(volumeForm.getText().toString());

        EditText proofForm = (EditText) findViewById(R.id.proofEdit);
        double proof = Double.parseDouble(proofForm.getText().toString());
        proof = proof / 2;
        drinkList.add(new Drink(volume, proof));

        TextView numDrinksView = (TextView) findViewById(R.id.numDrinksLabel);
        int numDrinks = Integer.parseInt(numDrinksView.getText().toString());
        numDrinks++;
        numDrinksView.setText(String.valueOf(numDrinks));
    }

    public void calculateAnswer(View view) {
        EditText timeForm = (EditText) findViewById(R.id.lengthEdit);
        double timeInHours = Double.parseDouble(timeForm.getText().toString());
        Person person = getIntent().getParcelableExtra("person");
        double calculatedBAC = MainActivity.findWeightedBAC(drinkList, person, timeInHours);
        double guessBAC = getIntent().getDoubleExtra("guess", 0.0);

        Intent intentToFinalPage = new Intent(this, FinalActivity.class);
        intentToFinalPage.putExtra("answer", calculatedBAC);
        intentToFinalPage.putExtra("guess", guessBAC);
        startActivity(intentToFinalPage);
    }
}
