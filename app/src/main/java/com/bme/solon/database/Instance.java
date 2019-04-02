package com.bme.solon.database;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Instance {
    public static final String TABLE_NAME = "Instance";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIME = "dateTime";
    public static final String COLUMN_SEVERITY = "severity";
    public static final String COLUMN_RESOLUTION = "resolution";
    public static final String COLUMN_RESOLUTION_TIME = "resolutionTime";
    public static final String COLUMN_DEVICE_ID = "deviceId";

    public static final int RESOLVED = 1;
    public static final int UNRESOLVED = 0;

    private long id;
    private LocalDateTime dateTime;
    private int severity;
    private int resolution;
    private LocalDateTime resolutionTime;
    private long deviceId;

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TIME + " TEXT,"
                    + COLUMN_SEVERITY + " INTEGER,"
                    + COLUMN_RESOLUTION + " INTEGER,"
                    + COLUMN_RESOLUTION_TIME + " TEXT,"
                    + COLUMN_DEVICE_ID + " INTEGER"
                    + ")";

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM dd, YYYY");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a");


    public Instance(int severity, long deviceId) {
        this(-1, LocalDateTime.now().toString(), severity, UNRESOLVED, LocalDateTime.MIN.toString(), deviceId);
    }

    /**
     * Constructor
     * @param id        Database ID
     * @param severity  UTI Severity
     * @param resolution    Resolution resolution
     * @param dateTime  DateTime of incident
     * @param deviceId  Database ID of device
     */
    public Instance(long id, String dateTime, int severity, int resolution, String resolutionTime, long deviceId) {
        this.id = id;
        this.dateTime = LocalDateTime.parse(dateTime);
        this.severity = severity;
        this.resolution = resolution;
        this.resolutionTime = LocalDateTime.parse(resolutionTime);
        this.deviceId = deviceId;
    }

    public long getId() {
        return id;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public String getDateTimeAsString() {
        return dateTime.toString();
    }
    public int getSeverity() {
        return severity;
    }
    public int getResolution() {
        return resolution;
    }
    public LocalDateTime getResolutionTime() {
        return resolutionTime;
    }
    public String getResolutionTimeAsString() {
        return resolutionTime.toString();
    }
    public long getDeviceId() {
        return  deviceId;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setSeverity(int severity) {
        this.severity = severity;
    }
    public void setResolution(int resolution) {
        this.resolution = resolution;
    }
    public void setResolutionTime() {
        resolutionTime = LocalDateTime.now();
    }

    public String toString() {
        return "id=" + id +
                ", dateTime=" + dateTime +
                ", severity=" + severity +
                ", resolution=" + resolution +
                ", resolutionTime=" + resolutionTime +
                ", deviceId=" + deviceId;
    }
}
