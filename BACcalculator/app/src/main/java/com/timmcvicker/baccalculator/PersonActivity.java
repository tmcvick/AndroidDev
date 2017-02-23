package com.timmcvicker.baccalculator;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by timmcvicker on 1/20/17.
 */

public class PersonActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private String gender = "";

    public void goToInfo(View view) {
        Intent intentToInfo = new Intent(this, InfoActivity.class);
        startActivity(intentToInfo);
    }

    public void next(View view) {
        int weightOfPerson =  Integer.parseInt(((EditText) findViewById(R.id.weightText)).getText().toString());
        Person newUser = new Person(gender.equalsIgnoreCase("male"), weightOfPerson);
        Intent mainIntent = getIntent();
        Intent intentToDrinksPage = new Intent(this, DrinkActivity.class);
        intentToDrinksPage.putExtra("person", (Parcelable) newUser);
        intentToDrinksPage.putExtra("guess", mainIntent.getDoubleExtra("guess", 0.0));
        startActivity(intentToDrinksPage);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Male");
        categories.add("Female");


        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        gender = parent.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
