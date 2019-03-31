package com.bme.solon.bluetooth;

/**
 * Constants class for broadcast intents sent by {@link BluetoothService}.
 * All ACTIONS must be added to the filter in {@link com.bme.solon.MainActivity}
 */
public class BluetoothBroadcast {
    public static final String ACTION_CONNECTING = "com.bme.solon.ACTION_CONNECTING";
    public static final String ACTION_CONNECTED = "com.bme.solon.ACTION_CONNECTED";
    public static final String ACTION_CONNECTED_UPDATE = "com.bme.solon.ACTION_CONNECTED_UPDATE";
    public static final String ACTION_DISCONNECTED = "com.bme.solon.ACTION_DISCONNECTED";

    public static final String KEY_DEVICE_NAME = "KEY_DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "KEY_DEVICE_ADDRESS";
}
