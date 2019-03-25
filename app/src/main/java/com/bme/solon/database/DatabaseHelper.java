package com.bme.solon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper singleton;        //singleton
    private SQLiteDatabase readDb;
    //TODO: make writeable DB a class variable

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Instance_db";

    /**
     * Private constructor
     * @param context   Caller context
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        readDb = getReadableDatabase();
    }

    /**
     * Get the singleton instance of this DatabaseHelper. If none exists, create one.
     * @param context   Caller context
     * @return          The singleton instance
     */
    public static DatabaseHelper getInstance(Context context) {
        if (singleton == null) {
            singleton = new DatabaseHelper(context.getApplicationContext());
        }
        return singleton;
    }

    /**
     * Close the singleton database and all its open connections.
     */
    public static void killInstance() {
        if (singleton != null) {
            singleton.close();
            singleton = null;
        }
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(Instance.CREATE_TABLE);
        db.execSQL(Device.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Instance.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Device.TABLE_NAME);
        //TODO: migrate tables, not drop

        // Create tables again
        onCreate(db);
    }

    public long addInstance(int severity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Instance.COLUMN_SEVERITY, severity);
        long id = db.insert(Instance.TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public Instance retrieveInstance(long id) {
         // get readable database as we are not inserting anything
        SQLiteDatabase db = readDb;

        Cursor cursor = db.query(Instance.TABLE_NAME,
                new String[]{Instance.COLUMN_ID, Instance.COLUMN_SEVERITY, Instance.COLUMN_STATUS, Instance.COLUMN_TIME},
                Instance.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Instance instance = new Instance(
            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_ID)),
            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_SEVERITY)),
            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_STATUS)),
            cursor.getString(cursor.getColumnIndex(Instance.COLUMN_TIME))
        );
        // close the cursor
        cursor.close();
        return instance;
    }

    public List<Instance> getAllInstances() {
        List<Instance> instances = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Instance.TABLE_NAME + " ORDER BY " +
                Instance.COLUMN_TIME + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Instance instance = new Instance(
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_SEVERITY)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_STATUS)),
                        cursor.getString(cursor.getColumnIndex(Instance.COLUMN_TIME))
                );
                instances.add(instance);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        return instances;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Instance> getInstancesByTime(String duration) {
        List<Instance> instances = new ArrayList<>();
        String time;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String month = dateFormat.format(Date.from(ZonedDateTime.now().minusMonths(1).toInstant()));
        String week = dateFormat.format(Date.from(ZonedDateTime.now().minusWeeks(1).toInstant()));

        if (duration == "week") {
            time = week;
        } else {
            time = month;
        }

        Log.d("testing", time);
        Log.d("testing", month);


        // Select All Query
        String selectQuery = new StringBuilder().append("SELECT  * FROM ").append(Instance.TABLE_NAME).append(" WHERE ").append(Instance.COLUMN_TIME).append("> ").append("'").append(time).append("' ")
                .append(" ORDER BY ").append(Instance.COLUMN_TIME).append(" DESC").toString();

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Instance instance = new Instance(
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_SEVERITY)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_STATUS)),
                        cursor.getString(cursor.getColumnIndex(Instance.COLUMN_TIME))
                );
                instances.add(instance);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return instances;
    }

    public int markInstanceAsRead(Instance instance) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Instance.COLUMN_STATUS, 0);

        // updating row
        return db.update(Instance.TABLE_NAME, values, Instance.COLUMN_ID + " = ?",
                new String[]{String.valueOf(instance.getId())});
    }

    // insert instance (when new instance detected)
    // grabbing Instance (from time period, etc)
    // grabbing instance (by id, time, etc)
    // mark instance as read

    /**
     * Query database for the "active" device details.
     * @return      Map with device credentials if an active device exists.
     *              Otherwise an empty map.
     */
    public Map<String, String> getActiveDevice() {
        Map<String, String> device = new HashMap<>();

        Cursor cursor = readDb.query(Device.TABLE_NAME,
                new String[]{Device.COLUMN_ID, Device.COLUMN_NAME, Device.COLUMN_ADDRESS, Device.COLUMN_ACTIVE},
                Device.COLUMN_ACTIVE + "=1",
                null, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            device.put(Device.COLUMN_NAME, cursor.getString(cursor.getColumnIndex(Device.COLUMN_NAME)));
            device.put(Device.COLUMN_ADDRESS, cursor.getString(cursor.getColumnIndex(Device.COLUMN_ADDRESS)));
        }
        return device;
    }
}