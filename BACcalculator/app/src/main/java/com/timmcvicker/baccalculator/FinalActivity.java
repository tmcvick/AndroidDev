package com.timmcvicker.baccalculator;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by timmcvicker on 1/31/17.
 */

public class FinalActivity extends AppCompatActivity {
    public void goToInfo(View view) {
        Intent intentToInfo = new Intent(this, InfoActivity.class);
        startActivity(intentToInfo);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final);

        Intent intent = getIntent();
        double bacActual = intent.getDoubleExtra("answer", 0.0);
        double bacGuess = intent.getDoubleExtra("guess", 0.0);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.drunk);

        TextView bacView = (TextView) findViewById(R.id.actualBACLabel);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.activity_final);
        bacView.setText(String.format("%.3f", bacActual));
        if (bacActual < 0.08) {
            relativeLayout.setBackgroundColor(Color.GREEN);
        } else if (bacActual < 0.25) {
            relativeLayout.setBackgroundColor(Color.YELLOW);
        } else {
            relativeLayout.setBackgroundColor(Color.RED);
        }

        TextView differenceView = (TextView) findViewById(R.id.differenceLabel);
        double difference = Math.abs(bacActual - bacGuess);
        Log.i("diff", String.valueOf(difference));
        if (difference <=  0.001) {
            TextView differenceLabel = (TextView) findViewById(R.id.offLabel);
            differenceLabel.setText("You were right!!");
            differenceView.setText("");
        } else {
            differenceView.setText(String.format("%.3f", difference));
        }
    }
}
