package com.bme.solon.strip;

import android.util.Log;

public class StripReader {
    public static final String TAG = "StripReader";

    public static final int THRESHOLD = 1000;
    public static final int DRY_RED = 35500;
    public static final int DRY_GREEN = 29000;
    public static final int DRY_BLUE = 15500;
    public static final int WET_RED = 10000;
    public static final int WET_GREEN = 10500;
    public static final int WET_BLUE = 6500;
    public static final int LOW_SEVERITY_RED = 8500;
    public static final int LOW_SEVERITY_GREEN = 10750;
    public static final int LOW_SEVERITY_BLUE = 8750;
    public static final int MED_SEVERITY_RED = 9000;
    public static final int MED_SEVERITY_GREEN = 9000;
    public static final int MED_SEVERITY_BLUE = 9000;
    public static final int HIGH_SEVERITY_RED = 15500;
    public static final int HIGH_SEVERITY_GREEN = 20000;
    public static final int HIGH_SEVERITY_BLUE = 16500;

    public static final int STATUS_RESET_THRESHOLD = 30;

    public static StripStatus readValue(String value) {
        String[] rgb = value.split("\\r?\\n");
        if (rgb.length != 3) {
            return StripStatus.DRY;
        }
        Log.v(TAG, "readValue: red=" + rgb[0] + " green=" + rgb[1] + " blue=" + rgb[2]);

        int red = Integer.valueOf(rgb[0].replaceAll("[^\\d.]", ""));
        int green = Integer.valueOf(rgb[1].replaceAll("[^\\d.]", ""));
        int blue = Integer.valueOf(rgb[2].replaceAll("[^\\d.]", ""));

        if (withinThreshold(red, DRY_RED) && withinThreshold(green, DRY_GREEN) && withinThreshold(blue, DRY_BLUE)) {
            Log.v(TAG, "Strip is dry");
            return StripStatus.DRY;
        }
        if (withinThreshold(red, WET_RED) && withinThreshold(green, WET_GREEN) && withinThreshold(blue, WET_BLUE)) {
            Log.v(TAG, "Strip is wet, no UTI");
            return StripStatus.NO_UTI;
        }
        if (withinThreshold(red, LOW_SEVERITY_RED) && withinThreshold(green, LOW_SEVERITY_GREEN) && withinThreshold(blue, LOW_SEVERITY_BLUE)) {
            Log.v(TAG, "Strip has light UTI");
            return StripStatus.MEDIUM_UTI;
        }
        if (withinThreshold(red, MED_SEVERITY_RED) && withinThreshold(green, MED_SEVERITY_GREEN) && withinThreshold(blue, MED_SEVERITY_BLUE)) {
            Log.v(TAG, "Strip has medium UTI");
            return StripStatus.MEDIUM_UTI;
        }
        if (withinThreshold(red, HIGH_SEVERITY_RED) && withinThreshold(green, HIGH_SEVERITY_GREEN) && withinThreshold(blue, HIGH_SEVERITY_BLUE)) {
            Log.v(TAG, "Strip has severe UTI");
            return StripStatus.SEVERE_UTI;
        }
        return StripStatus.DRY;
    }

    public static boolean withinThreshold(int value, int median) {
        return value > (median - THRESHOLD) && value < (median + THRESHOLD);
    }
}
