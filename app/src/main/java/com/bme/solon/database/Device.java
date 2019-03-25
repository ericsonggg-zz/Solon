package com.bme.solon.database;

public class Device {
    public static final String TABLE_NAME = "Device";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_ACTIVE = "active";

    private String name;
    private String address;
    private boolean active;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_ADDRESS + " TEXT,"
                    + COLUMN_ACTIVE + " INTEGER DEFAULT 0"
                    + ")";

    public Device(String name, String address, int active) {
        this.name = name;
        this.address = address;
        this.active = active == 1;
    }

    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public boolean getActive() {
        return active;
    }
}
