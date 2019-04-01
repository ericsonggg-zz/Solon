package com.bme.solon.strip;

import android.util.Log;

public class StripReader {
    public static final String TAG = "StripReader";

    public static final int WET_RED = 9000;
    public static final int WET_GREEN = 9000;
    public static final int WET_BLUE = 9000;
    public static final int LOW_SEVERITY_RED = 9000;
    public static final int LOW_SEVERITY_GREEN = 9000;
    public static final int LOW_SEVERITY_BLUE = 9000;
    public static final int MED_SEVERITY_RED = 9000;
    public static final int MED_SEVERITY_GREEN = 9000;
    public static final int MED_SEVERITY_BLUE = 9000;
    public static final int HIGH_SEVERITY_RED = 28500;
    public static final int HIGH_SEVERITY_GREEN = 23500;
    public static final int HIGH_SEVERITY_BLUE = 19500;

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

        if (red < WET_RED && green < WET_GREEN && blue < WET_BLUE) {
            Log.v(TAG, "Strip is dry");
            return StripStatus.DRY;
        }
        else if (red < LOW_SEVERITY_RED && green < LOW_SEVERITY_GREEN && blue < LOW_SEVERITY_BLUE) {
            Log.v(TAG, "Strip is wet, no UTI");
            return StripStatus.NO_UTI;
        }
        else if (red < MED_SEVERITY_RED && green < MED_SEVERITY_GREEN && blue < MED_SEVERITY_BLUE) {
            Log.v(TAG, "Strip has light UTI");
            return StripStatus.LIGHT_UTI;
        }
        else if (red < HIGH_SEVERITY_RED && green < HIGH_SEVERITY_GREEN && blue < HIGH_SEVERITY_BLUE) {
            Log.v(TAG, "Strip has medium UTI");
            return StripStatus.MEDIUM_UTI;
        }
        else {
            Log.v(TAG, "Strip has severe UTI");
            return StripStatus.SEVERE_UTI;
        }
    }
}
