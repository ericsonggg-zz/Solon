package com.bme.solon;

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
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Instance_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(Instance.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Instance.TABLE_NAME);

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
        SQLiteDatabase db = this.getReadableDatabase();

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
        // close the db connection
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


}