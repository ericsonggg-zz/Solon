package com.bme.solon;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
/*  public List<Instance> retrieveInstances(int severity) {
        return []
}*/
    public Instance retrieveInstance(long id) {
         // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Instance.TABLE_NAME,
                new String[]{Instance.COLUMN_ID},
                Instance.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare note object
        //int id, int severity, int status, String time
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
     /*
    public int markInstanceAsRead(Instance instance) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Instance.COLUMN_NOTE, instance.getNote());

        // updating row
        return db.update(Note.TABLE_NAME, values, Note.COLUMN_ID + " = ?",
                new String[]{String.valueOf(instance.getId())});
    }*/


    // insert instance (when new instance detected)
    // grabbing Instance (from time period, etc)
    // grabbing instance (by id, time, etc)
    // mark instance as read


}