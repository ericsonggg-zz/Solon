package com.bme.solon;

public class Instance {
    public static final String TABLE_NAME = "Instance";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_SEVERITY = "severity";
    public static final String COLUMN_STATUS = "status";

    private int id;
    private int severity;
    private int status;
    private String time;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_SEVERITY + " INTEGER,"
                    + COLUMN_STATUS + " INTEGER,"
                    + COLUMN_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    public Instance(int id, int severity, int status, String time) {
        this.id = id;
        this.severity = severity;
        this.status = status;
        this.time = time;
    }
}
