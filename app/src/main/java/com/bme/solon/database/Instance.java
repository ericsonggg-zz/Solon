package com.bme.solon.database;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Instance {
    public static final String TABLE_NAME = "Instance";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIME = "dateTime";
    public static final String COLUMN_SEVERITY = "severity";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_DEVICE_ID = "deviceId";

    public static final int STATUS_RESOLVED = 1;
    public static final int STATUS_UNRESOLVED = 0;

    private int id;
    private int severity;
    private int status;
    private LocalDateTime dateTime;
    private long deviceId;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_SEVERITY + " INTEGER,"
                    + COLUMN_STATUS + " INTEGER,"
                    + COLUMN_TIME + " TEXT,"
                    + COLUMN_DEVICE_ID + " INTEGER"
                    + ")";

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM dd, YYYY");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a");


    public Instance(int severity, long deviceId) {
        this(-1, severity, STATUS_UNRESOLVED, LocalDateTime.now().toString(), deviceId);
    }

    /**
     * Constructor
     * @param id        Database ID
     * @param severity  UTI Severity
     * @param status    Resolution status
     * @param dateTime  DateTime of incident
     * @param deviceId  Database ID of device
     */
    public Instance(int id, int severity, int status, String dateTime, long deviceId) {
        this.id = id;
        this.severity = severity;
        this.status = status;
        this.dateTime = LocalDateTime.parse(dateTime);
        this.deviceId = deviceId;
    }

    public int getId() {
        return id;
    }
    public int getSeverity() {
        return severity;
    }
    public int getStatus() {
        return status;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public String getDateTimeAsString() {
        return dateTime.toString();
    }
    public long getDeviceId() {
        return  deviceId;
    }

    public String toString() {
        return "id=" + id +
                ", severity=" + severity +
                ", status=" + status +
                ", dateTime=" + dateTime +
                ", deviceId=" + deviceId;
    }
}
