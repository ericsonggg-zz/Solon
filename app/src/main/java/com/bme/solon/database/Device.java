package com.bme.solon.database;

public class Device {
    public static final String TABLE_NAME = "Device";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_APPNAME = "appname";

    private long id;
    private String name;
    private String address;
    private boolean active;
    private String appName;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_ADDRESS + " TEXT,"
                    + COLUMN_ACTIVE + " INTEGER DEFAULT 0,"
                    + COLUMN_APPNAME + " TEXT"
                    + ")";

    /**
     * Constructor for new Devices. Use this when not retrieving from the database.
     * Sets ID = -1, Active to true, and AppName to the Bluetooth name.
     * @param name      Bluetooth name
     * @param address   Bluetooth address
     */
    public Device(String name, String address) {
        this(-1, name, address, 1, name);
    }

    /**
     * Constructor for new Devices.
     * Sets ID = -1, and Active to true
     * @param name      Bluetooth name
     * @param address   Bluetooth address
     * @param appName   App assigned name
     */
    public Device(String name, String address, String appName) {
        this(-1, name, address, 1, appName);
    }

    /**
     * Constructor
     * @param id        Database ID
     * @param name      Bluetooth name
     * @param address   Bluetooth address
     * @param active    Active status
     * @param appName   App assigned name
     */
    public Device(long id, String name, String address, int active, String appName) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.active = active == 1;
        this.appName = appName;
    }

    public long getId() {
        return id;
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
    public String getAppName() {
        return appName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String toString() {
        return "id=" + id +
                ", name=" + name +
                ", address=" + address +
                ", active=" + active +
                ", appName=" + appName;
    }
}
