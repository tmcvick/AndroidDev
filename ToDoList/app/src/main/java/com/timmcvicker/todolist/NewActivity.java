package com.timmcvicker.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Toast;

import java.util.Collections;

/**
 * Activity that will represent the new item/edit item page
 */
public class NewActivity extends AppCompatActivity {
    /**
     * The incoming/outgoing listItem
     */
    private ListItem listItem;

    /**
     * instance of the databaseHelper
     */
    private ItemsDatabaseHelper databaseHelper;


    private EditText descriptionField;
    private EditText dateField;
    private RatingBar priorityRatingBar;

    /**
     * creates the activity and loads all listeners
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        //set up all the variables
        listItem = getIntent().getParcelableExtra("listItem");
        Button button = (Button) findViewById(R.id.button);
        descriptionField = (EditText) findViewById(R.id.descriptionField);
        dateField = (EditText) findViewById(R.id.dateField);
        priorityRatingBar = (RatingBar) findViewById(R.id.priorityRatingBar);
        databaseHelper = ItemsDatabaseHelper.getInstance(NewActivity.this);

        //listItem needs to be created
        if (listItem == null) {
            //new item
            button.setText("Create");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    validateEntries(priorityRatingBar, dateField, descriptionField, true);
                }
            });
        //list item needs to be updated
        } else {
            //edit state
            button.setText("Save");
            databaseHelper.deleteItem(listItem);
            dateField.setText(listItem.getDueDate());
            descriptionField.setText(listItem.getTitle());
            priorityRatingBar.setRating(listItem.getPriority());

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    validateEntries(priorityRatingBar, dateField, descriptionField, false);
                }
            });
        }
    }

    /**
     * will perform the necessary update operations on the listItem and then exit the activity
     */
    private void doUpdate() {
        listItem.setPriority((int) priorityRatingBar.getRating());
        listItem.setTitle(descriptionField.getText().toString());
        listItem.setDueDate(dateField.getText().toString());

        databaseHelper.addOrUpdateItem(listItem);
        Toast.makeText(NewActivity.this, "Updating Item!", Toast.LENGTH_SHORT).show();
        exitToMainList();
    }

    /**
     * Will perform the necessary create operations on the listItem and then exit the activity
     */
    private void doCreate() {
        listItem = new ListItem(descriptionField.getText().toString(), (int) priorityRatingBar.getRating(), dateField.getText().toString());

        databaseHelper.addOrUpdateItem(listItem);
        Toast.makeText(NewActivity.this, "Creating Item!", Toast.LENGTH_SHORT).show();
        exitToMainList();
    }

    /**
     * Will ensure that there is input for each item in the form and then create or update the listItem
     * @param priorityRatingBar the priority rating to be persisted
     * @param dateField the date to be persisted
     * @param descriptionField the title to be persisted
     * @param create_ind indicator for persistence of listItem; true creates the item, false updates it
     */
    private void validateEntries(RatingBar priorityRatingBar, EditText dateField, EditText descriptionField, boolean create_ind) {
        //all fields are not filled out
        if (priorityRatingBar.getRating() == 0 || dateField.getText().toString().isEmpty() || descriptionField.getText().toString().isEmpty()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(NewActivity.this);
            dialog.setCancelable(false);
            dialog.setTitle("Edit Item");
            dialog.setMessage("Data is incomplete and will not be persisted! Would you like to continue editing?");
            dialog.setPositiveButton("Continue Editing", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            })
                    .setNegativeButton("Exit To Main List", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (listItem != null) {
                                databaseHelper.addOrUpdateItem(listItem);
                            }
                            exitToMainList();
                        }
                    });

            final AlertDialog alert = dialog.create();
            alert.show();
        //all fields are filled out
        } else {
            if (create_ind) doCreate();
            else doUpdate();
        }
    }


    /**
     * This method starts the main activity
     */
    private void exitToMainList() {
        Intent intentToMainList = new Intent(this, ListActivity.class);
        startActivity(intentToMainList);
    }


    /**
     * This method is invoked when the back button is pressed, and ensures that the item is not removed from the list halfway through an update
     */
    @Override
    public void onBackPressed() {
        if (listItem != null) {
            databaseHelper.addOrUpdateItem(listItem);
        }
        super.onBackPressed();
    }
}
