package com.timmcvicker.todolist;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * This class is a database helper for the todolist app. It will help to perform CRUD operations on
 * the SQLite database
 *
 * @author timmcvicker
 * @see ListItem
 *
 */
public class ItemsDatabaseHelper extends SQLiteOpenHelper {
    // Database Info
    private static final String DATABASE_NAME = "itemsDatabase";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    private static final String TABLE_ITEMS = "items";

    // Post Table Columns
    private static final String KEY_ITEM_ID = "id";
    private static final String KEY_ITEM_TITLE = "title";
    private static final String KEY_ITEM_PRIORITY = "priority";
    private static final String KEY_ITEM_DATE = "due_date";

    private static ItemsDatabaseHelper sInstance;

    /**
     *
     * @param context context that the database helper will be used in
     * @return the singleton instance of the database helper
     */
    public static synchronized ItemsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new ItemsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private ItemsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Method that is called when database is configured. Overriden to allow for custom configs
     * @param db instance of the SQLite database
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    /**
     * Called when the database is created for the FIRST time, creates desired table.
     * If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
     * @param db instance of SQLite database for this app
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_ITEMS +
                "(" +
                KEY_ITEM_ID + " INTEGER PRIMARY KEY NOT NULL," + // Define a primary key
                KEY_ITEM_TITLE + " TEXT NOT NULL," +
                KEY_ITEM_PRIORITY + " INTEGER NOT NULL," +
                KEY_ITEM_DATE + " TEXT NOT NULL" +
                ")";

        db.execSQL(CREATE_POSTS_TABLE);
    }

    /**
     * Called when the database needs to be upgraded.
     * This method will only be called if a database already exists on disk with the same DATABASE_NAME,
     * but the DATABASE_VERSION is different than the version of the database that exists on disk.
     * @param db instance of SQLite database
     * @param oldVersion old version of database
     * @param newVersion new version of database. hard coded as constant at top of class
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
        }
    }

    /**
     * This method will first attempt to update an item if it is already in the database, keyed off the description.
     * If the description does not exist, it will add a row for the item
     * @param item the listItem to update or add
     * @return the id of the updated/inserted listItem
     */
    public long addOrUpdateItem(ListItem item) {
        //todo it might be a good idea to make id a member of listItem so that we can have two matching descriptions
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long itemId = -1;

        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM_DATE, item.getDueDate());
            values.put(KEY_ITEM_PRIORITY, item.getPriority());
            values.put(KEY_ITEM_TITLE, item.getTitle());

            // First try to update the user in case the item already exists in the database
            // This assumes descriptions are unique
            int rows = db.update(TABLE_ITEMS, values, KEY_ITEM_TITLE + "= ?", new String[]{item.getTitle()});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the item we just updated
                String itemsSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_ITEM_ID, TABLE_ITEMS, KEY_ITEM_TITLE);
                Cursor cursor = db.rawQuery(itemsSelectQuery, new String[]{item.getTitle()});
                try {
                    if (cursor.moveToFirst()) {
                        itemId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // item with this description did not already exist, so insert new item
                itemId = db.insertOrThrow(TABLE_ITEMS, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update item");
        } finally {
            db.endTransaction();
        }
        return itemId;
    }

    /**
     * This method will return all items currently being stored in the database
     * @return a List containing all listItems currently stored in the database
     */
    public List<ListItem> getAllItems() {
        List<ListItem> items = new ArrayList<>();

        String ITEMS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_ITEMS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ITEMS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    ListItem newItem = new ListItem();
                    newItem.setTitle(cursor.getString(cursor.getColumnIndex(KEY_ITEM_TITLE)));
                    newItem.setPriority(cursor.getInt(cursor.getColumnIndex(KEY_ITEM_PRIORITY)));
                    newItem.setDueDate(cursor.getString(cursor.getColumnIndex(KEY_ITEM_DATE)));

                    items.add(newItem);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get items from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return items;
    }


    /**
     * This method deletes all rows currently in the items table
     */
    public void deleteAllItems() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_ITEMS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all items");
        } finally {
            db.endTransaction();
        }
    }

    /**
     * This method will delete a specific row in the database
     * @param item the item in the database to delete, keyed off the description
     */
    public void deleteItem(ListItem item) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_ITEMS, KEY_ITEM_TITLE + "= ?", new String[]{item.getTitle()});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete item " + item.getTitle());
        } finally {
            db.endTransaction();
        }
    }
}
