package com.timmcvicker.todolist;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
    private static final int DATABASE_VERSION = 21;

    // Table Names
    private static final String TABLE_ITEMS = "items";

    // Post Table Columns
    private static final String KEY_ITEM_ID = "id";
    private static final String KEY_ITEM_TITLE = "title";
    private static final String KEY_ITEM_PRIORITY = "priority";
    private static final String KEY_ITEM_DATE = "due_date";
    private static final String KEY_ITEM_DELETED = "deleted_ind";
    private static final String KEY_ITEM_UPDATED_AT = "updated_at";

    private static ItemsDatabaseHelper sInstance;

    private  Map<Integer, ListItem> itemsChangedInLocal;
    private  Map<Integer, ListItem> itemsChangedInRemote;
    private  Map<Integer, ListItem> itemsInLocal;
    private  Map<Integer, ListItem> itemsInRemote;

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
                KEY_ITEM_DATE + " DATETIME NOT NULL," +
                KEY_ITEM_DELETED + " BOOLEAN NOT NULL DEFAULT FALSE," +
                KEY_ITEM_UPDATED_AT + " DATETIME NOT NULL" +
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
    public long addOrUpdateItem(ListItem item, boolean deleted_flag) {
        //todo it might be a good idea to make id a member of listItem so that we can have two matching descriptions
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long itemId = -1;

        db.beginTransaction();

        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM_DATE, getDateTime(item.getDueDate()));
            values.put(KEY_ITEM_PRIORITY, item.getPriority());
            values.put(KEY_ITEM_TITLE, item.getTitle());
            values.put(KEY_ITEM_UPDATED_AT, "datetime()");
            values.put(KEY_ITEM_DELETED, deleted_flag);

            // First try to update the item in case the item already exists in the database
            // This assumes descriptions are unique
            int rows = 0;
            if (item.getPrimaryKey() > -1)
                rows = db.update(TABLE_ITEMS, values, KEY_ITEM_ID + "= ?", new String[]{String.valueOf(item.getPrimaryKey())});

            // Check if update succeeded
            if (rows == 1) {
                itemId = item.getPrimaryKey();
                db.setTransactionSuccessful();
            } else if(deleted_flag) {
                Log.e(TAG, "Error while trying to delete item: item does not exist");
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
                String.format("SELECT * FROM %s WHERE %s = 0",
                        TABLE_ITEMS, KEY_ITEM_DELETED);

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
                    newItem.setPrimaryKey(cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)));

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

    private Map<String, ListItem> getItem(int id) {
        Map<String, ListItem> items = new HashMap();

        String ITEMS_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = %d",
                        TABLE_ITEMS, KEY_ITEM_ID, id);

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
                    newItem.setPrimaryKey(cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)));

                    items.put(cursor.getString(cursor.getColumnIndex(KEY_ITEM_UPDATED_AT)), newItem);
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
     * This method will return all items currently being stored in the database that are deleted
     * @return a List containing all listItems currently stored in the database that are deleted
     */
    public List<ListItem> getAllDeletedItems() {
        List<ListItem> items = new ArrayList<>();

        String ITEMS_SELECT_QUERY =
                String.format("SELECT * FROM %s WHERE %s = 1",
                        TABLE_ITEMS, KEY_ITEM_DELETED);

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
                    newItem.setPrimaryKey(cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID)));

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
     * This method will delete a specific row in the database
     * @param item the item in the database to delete, keyed off the description
     */
    public void deleteItem(ListItem item) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            addOrUpdateItem(item, true);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete item " + item.getTitle());
        } finally {
            db.endTransaction();
        }
    }

    private String getDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }

    public void syncData(final Context context) {
        final String GET_URL = "https://people.cs.clemson.edu/~tmcvick/cpsc4820/todolist/getitems.php";
        final String DELETE_URL = "https://people.cs.clemson.edu/~tmcvick/cpsc4820/todolist/delete_item.php";
        final String CREATE_URL = "https://people.cs.clemson.edu/~tmcvick/cpsc4820/todolist/create_item.php";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, GET_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadJson(response, context);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                return null;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }

    private void loadJson(String response, Context context) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                JSONObject row = result.getJSONObject(i);
                Integer id = Integer.parseInt(row.get(KEY_ITEM_ID).toString());
                String title = row.get(KEY_ITEM_TITLE).toString();
                Integer priority = Integer.parseInt(row.get(KEY_ITEM_PRIORITY).toString());
                String duedate = row.get(KEY_ITEM_DATE).toString();
                final String deleted = row.get(KEY_ITEM_DELETED).toString();
                String updated = row.get(KEY_ITEM_UPDATED_AT).toString();

                ListItem remoteListItem = new ListItem(title, priority, new Date(duedate));
                remoteListItem.setPrimaryKey(id);

                final Map<String, ListItem> localListItemList = getItem(id);

                //is not in local
                if (localListItemList.size() < 1) {
                    addOrUpdateItem(remoteListItem, Boolean.parseBoolean(deleted));
                }
                else {
                    for(final String date: localListItemList.keySet()) {
                        if (new Date(date).before(new Date(updated))) {
                            //is newer in remote
                            addOrUpdateItem(remoteListItem, Boolean.parseBoolean(deleted));
                        } else {
                            //is newer in remote
                            final String UPDATE_URL = "https://people.cs.clemson.edu/~tmcvick/cpsc4820/todolist/update_item.php";

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_URL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                        }
                                    }){
                                @Override
                                protected Map<String,String> getParams() {
                                    ListItem item = localListItemList.get(date);
                                    Map<String, String> values = new HashMap<>();
                                    values.put(KEY_ITEM_DATE, getDateTime(item.getDueDate()));
                                    values.put(KEY_ITEM_PRIORITY, String.valueOf(item.getPriority()));
                                    values.put(KEY_ITEM_TITLE, item.getTitle());
                                    values.put(KEY_ITEM_UPDATED_AT, "datetime()");
                                    values.put(KEY_ITEM_DELETED, deleted);

                                    return values;
                                }
                            };
                            RequestQueue requestQueue = Volley.newRequestQueue(context);
                            requestQueue.add(stringRequest);
                        }
                    }
                }

            }
        } catch (JSONException e) {
            Log.d("SYNC", "unable to sync database data");
        }
    }
}
