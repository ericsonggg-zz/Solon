package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;

/**
 * Constants class for broadcast intents sent by {@link BluetoothService}.
 */
public class BluetoothBroadcast {
    public static final String ACTION_SERVICE_BOUND = "com.bme.solon.ACTION_SERVICE_BOUND";
    public static final String ACTION_SERVICE_DISCONNECTED = "com.bme.solon.ACTION_SERVICE_DISCONNECTED";
    public static final String ACTION_CONNECTING = "com.bme.solon.ACTION_CONNECTING";
    public static final String ACTION_CONNECTED = "com.bme.solon.ACTION_CONNECTED";
    public static final String ACTION_CONNECTED_UPDATE = "com.bme.solon.ACTION_CONNECTED_UPDATE";
    public static final String ACTION_DISCONNECTED = "com.bme.solon.ACTION_DISCONNECTED";
    public static final String ACTION_DEVICES_CHANGED = "com.bme.solon.ACTION_DEVICES_CHANGED";
    public static final String ACTION_NEW_INSTANCE = "com.bme.solon.ACTION_NEW_INSTANCE";
    public static final String ACTION_INSTANCE_UPDATE = "com.bme.solon.ACTION_INSTANCE_UPDATE";
    public static final String ACTION_ADDRESS = "com.bme.solon.ADDRESS";
    public static final String ACTION_SNOOZE = "com.bme.solon.SNOOZE";

    public static final String KEY_DEVICE_NAME = "KEY_DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "KEY_DEVICE_ADDRESS";
    public static final String KEY_INSTANCE_ID = "KEY_INSTANCE_ID";

    /**
     * Get intent filter for this app.
     * All actions are filtered
     * @return  An intent filter
     */
    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothBroadcast.ACTION_ADDRESS);
        filter.addAction(BluetoothBroadcast.ACTION_SNOOZE);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothBroadcast.ACTION_CONNECTING);
        filter.addAction(BluetoothBroadcast.ACTION_CONNECTED);
        filter.addAction(BluetoothBroadcast.ACTION_CONNECTED_UPDATE);
        filter.addAction(BluetoothBroadcast.ACTION_DISCONNECTED);
        filter.addAction(BluetoothBroadcast.ACTION_DEVICES_CHANGED);
        filter.addAction(BluetoothBroadcast.ACTION_NEW_INSTANCE);
        filter.addAction(BluetoothBroadcast.ACTION_INSTANCE_UPDATE);
        filter.addAction(BluetoothBroadcast.ACTION_SERVICE_BOUND);
        filter.addAction(BluetoothBroadcast.ACTION_SERVICE_DISCONNECTED);
        return filter;
    }
}
