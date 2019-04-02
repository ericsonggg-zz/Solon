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
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static DatabaseHelper singleton; //singleton
    private SQLiteDatabase readDb;
    private SQLiteDatabase writeDb;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "Solon_db";

    /**
     * Private constructor
     * @param context   Caller context
     */
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        readDb = getReadableDatabase();
        writeDb = getWritableDatabase();
    }

    /**
     * Get the singleton instance of this DatabaseHelper. If none exists, create one.
     * @param context   Caller context
     * @return          The singleton instance
     */
    public static DatabaseHelper getInstance(Context context) {
        Log.v(TAG, "getDatabaseHelperInstance");
        if (singleton == null) {
            Log.v(TAG,"getDatabaseHelperInstance: creating new instance");
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
        Log.i(TAG, "onCreate");

        db.execSQL(Instance.CREATE_TABLE);
        db.execSQL(Device.CREATE_TABLE);
    }

    /**
     * On database upgrade, delete all prior entries and recreate schema
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade");
        // Drop older table if existing
        db.execSQL("DROP TABLE IF EXISTS " + Instance.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Device.TABLE_NAME);
        //TODO: migrate tables, not drop

        // Create tables again
        onCreate(db);
    }

    /**
     * Add an instance to the database.
     * @param instance  Instance to add
     * @return          Database ID
     */
    public long addInstance(Instance instance) {
        Log.v(TAG, "addInstance: " + instance.toString());
        ContentValues values = new ContentValues();
        values.put(Instance.COLUMN_TIME, instance.getDateTimeAsString());
        values.put(Instance.COLUMN_SEVERITY, instance.getSeverity());
        values.put(Instance.COLUMN_RESOLUTION, instance.getResolution());
        values.put(Instance.COLUMN_RESOLUTION_TIME, instance.getResolutionTimeAsString());
        values.put(Instance.COLUMN_DEVICE_ID, instance.getDeviceId());

        return writeDb.insert(Instance.TABLE_NAME, null, values);
    }

    /**
     * Update the details of an instance in the table, if exists.
     * @param instance  Instance with updated details
     */
    public void updateInstance(Instance instance) {
        Log.v(TAG, "updateInstance: " + instance.toString());
        ContentValues values = new ContentValues();
        values.put(Instance.COLUMN_TIME, instance.getDateTimeAsString());
        values.put(Instance.COLUMN_SEVERITY, instance.getSeverity());
        values.put(Instance.COLUMN_RESOLUTION, instance.getResolution());
        values.put(Instance.COLUMN_RESOLUTION_TIME, instance.getResolutionTimeAsString());
        values.put(Instance.COLUMN_DEVICE_ID, instance.getDeviceId());

        writeDb.update(Instance.TABLE_NAME, values, Instance.COLUMN_ID + "=?", new String[] {Long.toString(instance.getId())});
    }

    /**
     * Get a specific instance from the database, if exists
     * @param id    Database ID of the instance
     * @return      The Instance, or null if not found.
     */
    public Instance retrieveInstance(long id) {
        Log.v(TAG, "retrieveInstance");
        Instance instance = null;

        Cursor cursor = readDb.query(Instance.TABLE_NAME,
                null,
                Instance.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "getLatestInstance: db query found device");
                cursor.moveToFirst();
                instance = new Instance(
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Instance.COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_SEVERITY)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION)),
                        cursor.getString(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION_TIME)),
                        cursor.getLong(cursor.getColumnIndex(Instance.COLUMN_DEVICE_ID)));
            }
            cursor.close();
        }
        return instance;
    }

    /**
     * Get the latest Instance in the database
     * @return  The latest database, or null if database is empty.
     */
    public Instance getLatestInstance() {
        Log.d(TAG,"getLatestInstance");
        Instance instance = null;

        Cursor cursor = readDb.query(Instance.TABLE_NAME,
                null,null,null,null, null,
                Instance.COLUMN_TIME + " ASC", "1");

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "getLatestInstance: db query found device");
                cursor.moveToFirst();
                instance = new Instance(
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Instance.COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_SEVERITY)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION)),
                        cursor.getString(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION_TIME)),
                        cursor.getLong(cursor.getColumnIndex(Instance.COLUMN_DEVICE_ID)));
            }
            cursor.close();
        }
        return instance;
    }

    /**
     * Get all instances in database
     * @return  All instances
     */
    public List<Instance> getAllInstances() {
        Log.d(TAG, "getAllInstances");
        List<Instance> instances = new ArrayList<>();

        Cursor cursor = readDb.query(Instance.TABLE_NAME,
                null,null,null, null, null, Instance.COLUMN_TIME + " ASC", null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "getAllInstances: db query found instance(s)");
                cursor.moveToFirst();

                //Loop over all instances
                do {
                    Instance instance = new Instance(
                            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndex(Instance.COLUMN_TIME)),
                            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_SEVERITY)),
                            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION)),
                            cursor.getString(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION_TIME)),
                            cursor.getLong(cursor.getColumnIndex(Instance.COLUMN_DEVICE_ID)));
                    instances.add(instance);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
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
                        cursor.getString(cursor.getColumnIndex(Instance.COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_SEVERITY)),
                        cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION)),
                        cursor.getString(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION_TIME)),
                        cursor.getLong(cursor.getColumnIndex(Instance.COLUMN_DEVICE_ID)));
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
        values.put(Instance.COLUMN_RESOLUTION, 0);

        // updating row
        return db.update(Instance.TABLE_NAME, values, Instance.COLUMN_ID + " = ?",
                new String[]{String.valueOf(instance.getId())});
    }

    /*public int addPairedDevice() {

    }*/

    // insert instance (when new instance detected)
    // grabbing Instance (from time period, etc)
    // grabbing instance (by id, time, etc)
    // mark instance as read

    /**
     * Add an active device into the {@link Device} database.
     * Checks for duplicates - if already inserted, updates existing row.
     * Old active device is made inactive.
     * @param device    Device to add
     */
    public long addActiveDevice(Device device) {
        long id = -1;
        ContentValues values = new ContentValues();
        Device oldActiveDevice = getActiveDevice();
        Device currentDevice = getDevice(device.getName(), device.getAddress());

        if (currentDevice == null) {
            Log.d(TAG, "addActiveDevice: adding new device");
            values.put(Device.COLUMN_NAME, device.getName());
            values.put(Device.COLUMN_ADDRESS, device.getAddress());
            values.put(Device.COLUMN_ACTIVE, 1);
            values.put(Device.COLUMN_APPNAME, device.getAppName());
            id = writeDb.insert(Device.TABLE_NAME, null, values);
        }
        else {
            Log.d(TAG, "addActiveDevice: updating values");
            id = currentDevice.getId();
            values.put(Device.COLUMN_ACTIVE, 1);
            values.put(Device.COLUMN_APPNAME, device.getAppName());
            writeDb.update(Device.TABLE_NAME,
                    values,
                    Device.COLUMN_ID + "=?",
                    new String[] {Long.toString(id)});
        }

        //Make other device non-active if exists
        if (oldActiveDevice != null) {
            Log.v(TAG, "addActiveDevice: making old device id=" + oldActiveDevice.getId() + " non-active");
            values = new ContentValues();
            values.put(Device.COLUMN_ACTIVE, 0);
            writeDb.update(Device.TABLE_NAME,
                    values,
                    Device.COLUMN_ID + "=?",
                    new String[]{Long.toString(oldActiveDevice.getId())});
        }
        return id;
    }

    /**
     * Update the app name of a device
     * @param device    Device with updated name
     */
    public void updateAppName(Device device) {
        Log.d(TAG, "updateAppName: for " + device.toString());
        ContentValues values = new ContentValues();
        values.put(Device.COLUMN_APPNAME, device.getAppName());
        writeDb.update(Device.TABLE_NAME,
                values,
                Device.COLUMN_ID + "=?",
                new String[] {Long.toString(device.getId())});
    }

    /**
     * Query database for a specific device
     * @param id        Device table id
     * @return          Device if found, otherwise null
     */
    public Device getDevice(long id) {
        Log.v(TAG, "getDevice: id=" + id);
        Device device = null;

        Cursor cursor = readDb.query(Device.TABLE_NAME,
                null,
                Device.COLUMN_ID + "=?",
                new String[] {Long.toString(id)},
                null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "getDevice: device found");
                cursor.moveToFirst();
                device = new Device(
                        cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_ADDRESS)),
                        cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ACTIVE)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_APPNAME))
                );
            }
            cursor.close();
        }
        return device;
    }

    /**
     * Query database for a specific device
     * @param name      Bluetooth name
     * @param address   Bluetooth address
     * @return          Device if found, otherwise null
     */
    public Device getDevice(String name, String address) {
        Log.v(TAG, "getDevice: name = " + name + " address = " + address);
        Device device = null;

        Cursor cursor = readDb.query(Device.TABLE_NAME,
                null,
                Device.COLUMN_NAME + "=? AND " + Device.COLUMN_ADDRESS + "=?",
                new String[] {name, address},
                null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "getDevice: device found");
                cursor.moveToFirst();
                device = new Device(
                        cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_ADDRESS)),
                        cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ACTIVE)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_APPNAME))
                );
            }
            cursor.close();
        }
        return device;
    }

    /**
     * Query database for the "active" device details.
     * @return      Map with device credentials if an active device exists.
     *              Otherwise an empty map.
     */
    public Device getActiveDevice() {
        Log.d(TAG,"getActiveDevice");
        Device device = null;

        Cursor cursor = readDb.query(Device.TABLE_NAME,
                null,
                Device.COLUMN_ACTIVE + "=1",
                null, null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "getActiveDevice: db query found device");
                cursor.moveToFirst();
                device = new Device(
                        cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_ADDRESS)),
                        cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ACTIVE)),
                        cursor.getString(cursor.getColumnIndex(Device.COLUMN_APPNAME)));
            }
            cursor.close();
        }
        return device;
    }

    /**
     * Query {@link Device} database for all entries.
     * @return  All paired devices.
     */
    public List<Device> getPairedDevices() {
        Log.d(TAG, "getPairedDevices");
        List<Device> devices = new ArrayList<>();

        Cursor cursor = readDb.query(Device.TABLE_NAME,
                null,null,null, null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                Log.d(TAG, "getPairedDevices: db query found device(s)");
                cursor.moveToFirst();

                //Loop over all instances
                do {
                    Device device = new Device(
                            cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndex(Device.COLUMN_NAME)),
                            cursor.getString(cursor.getColumnIndex(Device.COLUMN_ADDRESS)),
                            cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ACTIVE)),
                            cursor.getString(cursor.getColumnIndex(Device.COLUMN_APPNAME)));
                    devices.add(device);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return devices;
    }

    /**
     * NEVER CALL THIS
     */
    public void wipeData() {
        Log.w(TAG, "wipeData");
        writeDb.execSQL("DROP TABLE IF EXISTS " + Instance.TABLE_NAME);
        writeDb.execSQL("DROP TABLE IF EXISTS " + Device.TABLE_NAME);
        onCreate(writeDb);
    }

    /**
     * PURELY for diagnostic reasons
     */
    public void dumpDatabase() {
        Log.v(TAG, "dumpDatabase");

        //Instances
        Cursor cursor = readDb.query(Instance.TABLE_NAME,
                null, null, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    Log.v(TAG, "dumpDatabase: instance entry: " +
                            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_ID)) + " " +
                            cursor.getString(cursor.getColumnIndex(Instance.COLUMN_TIME)) + " " +
                            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_SEVERITY)) + " " +
                            cursor.getInt(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION)) + " " +
                            cursor.getString(cursor.getColumnIndex(Instance.COLUMN_RESOLUTION_TIME)) + " " +
                            cursor.getLong(cursor.getColumnIndex(Instance.COLUMN_DEVICE_ID)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        //Devices
        cursor = readDb.query(Device.TABLE_NAME,
                null, null, null, null, null, null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    Log.v(TAG, "dumpDatabase: device entry: " +
                            cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ID)) + " " +
                            cursor.getString(cursor.getColumnIndex(Device.COLUMN_NAME)) + " " +
                            cursor.getString(cursor.getColumnIndex(Device.COLUMN_ADDRESS)) + " " +
                            cursor.getInt(cursor.getColumnIndex(Device.COLUMN_ACTIVE)) + " " +
                            cursor.getString(cursor.getColumnIndex(Device.COLUMN_APPNAME)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }
}