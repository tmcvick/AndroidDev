package com.timmcvicker.todolist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents the main Activity for the todolist app
 * @author timmcvicker
 */
public class ListActivity extends AppCompatActivity {

    /**
     * comparator that is used to sort list by either date or priority
     */
    private Comparator<ListItem> comparator;

    /**
     * List that holds all items as part of the data model
     */
    private List<ListItem> arrayOfItems;

    /**
     * adapter that allows the listView to hold listItems
     */
    private ItemsAdapter adapter;

    private Context context = this;

    /**
     * Sets up the app homepage and loads all listeners
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //set up the infoActivity button
        ImageButton infoButton = (ImageButton) findViewById(R.id.imageButton8);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(ListActivity.this, InfoActivity.class));
            }
        });

        //set up the switch
        Switch mySwitch = (Switch) findViewById(R.id.sortBySwitch);

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                            boolean isChecked) {
                if(isChecked){
                    comparator = ListItem.ListItemPriorityComparator;
                }else {
                    comparator = ListItem.ListItemDateComparator;
                }

                Collections.sort(arrayOfItems, comparator);
                adapter = new ItemsAdapter(context, arrayOfItems);
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setAdapter(adapter);
            }
        });

        //check the current state of the switch before we display the screen
        if(mySwitch.isChecked()){
            comparator = ListItem.ListItemPriorityComparator;
        }
        else {
            comparator = ListItem.ListItemDateComparator;
        }

        ItemsDatabaseHelper databaseHelper = ItemsDatabaseHelper.getInstance(this);

        // populate the data source
        arrayOfItems = databaseHelper.getAllItems();
        Collections.sort(arrayOfItems, comparator);

        // Create the adapter to convert the array to views
        adapter = new ItemsAdapter(this, arrayOfItems);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View childView, int position, long id) {
                final ListItem listItem = arrayOfItems.get(position);

                //alert dialog for clicking on an item
                AlertDialog.Builder dialog = new AlertDialog.Builder(ListActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("Edit Item");
                dialog.setMessage("What would you like to do with the item?" );
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        ItemsDatabaseHelper databaseHelper = ItemsDatabaseHelper.getInstance(ListActivity.this);

                        databaseHelper.deleteItem(listItem);
                        arrayOfItems = databaseHelper.getAllItems();

                        Collections.sort(arrayOfItems, comparator);
                        // Create the adapter to convert the array to views
                        adapter = new ItemsAdapter(ListActivity.this, arrayOfItems);
                        // Attach the adapter to a ListView
                        ListView listView = (ListView) findViewById(R.id.listView);
                        listView.setAdapter(adapter);
                    }
                })
                        .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switchToEditPage(listItem);
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                final AlertDialog alert = dialog.create();
                alert.show();
            }
        });
    }

    /**
     * Method that is called when the user clicks "Add Item", which will switch activities to create a new item
     * @param view the buttont that is clicked
     */
    public void addItem(View view) {
        switchToEditPage(null);
    }

    /**
     * Method that is called to switch to the next activity, the edit/new activity.
     * @param listItem listItem to be passed to next activity. Null if creating a new item
     */
    private void switchToEditPage(ListItem listItem) {
        Intent intentToEditPage = new Intent(this, NewActivity.class);
        intentToEditPage.putExtra("listItem", (Parcelable) listItem);
        startActivity(intentToEditPage);
    }
}
