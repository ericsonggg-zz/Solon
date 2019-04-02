package com.bme.solon.database;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
    private Calendar dateTime;
    private int severity;
    private int resolution;
    private Calendar resolutionTime;
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



    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy MM dd hh:mm:ss a");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("h:mm:ss a");

    public Instance(int severity, long deviceId) {
        this(-1, DATE_TIME_FORMAT.format(Calendar.getInstance().getTime()), severity, UNRESOLVED, DATE_TIME_FORMAT.format(Calendar.getInstance().getTime()), deviceId);
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
        try {
            this.id = id;
            this.dateTime = Calendar.getInstance();
            this.dateTime.setTime(DATE_TIME_FORMAT.parse(dateTime));
            this.severity = severity;
            this.resolution = resolution;
            this.resolutionTime = Calendar.getInstance();
            this.resolutionTime.setTime(DATE_TIME_FORMAT.parse(resolutionTime));
            this.deviceId = deviceId;
        } catch (ParseException ignored) {};
    }

    public long getId() {
        return id;
    }
    public Date getDateTime() {
        return dateTime.getTime();
    }
    public String getDateTimeAsString() {
        return DATE_TIME_FORMAT.format(dateTime.getTime());
    }
    public int getSeverity() {
        return severity;
    }
    public int getResolution() {
        return resolution;
    }
    public Date getResolutionTime() {
        return resolutionTime.getTime();
    }
    public String getResolutionTimeAsString() {
        return DATE_TIME_FORMAT.format(resolutionTime.getTime());
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
        resolutionTime = Calendar.getInstance();
    }
    public void setResolutionTime(Date resolutionTime) {
        this.resolutionTime.setTime(resolutionTime);
    }

    public String toString() {
        return "id=" + id +
                ", dateTime=" + DATE_TIME_FORMAT.format(dateTime.getTime()) +
                ", severity=" + severity +
                ", resolution=" + resolution +
                ", resolutionTime=" + DATE_TIME_FORMAT.format(resolutionTime.getTime()) +
                ", deviceId=" + deviceId;
    }
}
