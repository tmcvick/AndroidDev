package com.timmcvicker.tsaapp;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public void changeLine(View view) throws InterruptedException {
        TextView lineName = (TextView) findViewById(R.id.lineName);
        int color = ((ColorDrawable) lineName.getBackground()).getColor();
        Toast.makeText(this, "Finding next line", Toast.LENGTH_SHORT).show();
        Random random = new Random();
        if (random.nextBoolean()) {
            changeView(lineName);
        } else {
            lineName.setBackgroundColor(color);
        }
    }

    private void changeView(TextView lineName) {
        if (lineName.getText().toString().equalsIgnoreCase("Left")) {
            lineName.setBackgroundColor(Color.MAGENTA);
            lineName.setText("Right");
        } else {
            lineName.setBackgroundColor(Color.BLUE);
            lineName.setText("Left");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
