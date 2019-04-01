package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;

/**
 * Constants class for broadcast intents sent by {@link BluetoothService}.
 */
public class BluetoothBroadcast {
    public static final String ACTION_CONNECTING = "com.bme.solon.ACTION_CONNECTING";
    public static final String ACTION_CONNECTED = "com.bme.solon.ACTION_CONNECTED";
    public static final String ACTION_CONNECTED_UPDATE = "com.bme.solon.ACTION_CONNECTED_UPDATE";
    public static final String ACTION_DISCONNECTED = "com.bme.solon.ACTION_DISCONNECTED";
    public static final String ACTION_DEVICES_CHANGED = "com.bme.solon.ACTION_DEVICES_CHANGED";
    public static final String ACTION_ADDRESS = "com.bme.solon.ADDRESS";
    public static final String ACTION_SNOOZE = "com.bme.solon.SNOOZE";

    public static final String KEY_DEVICE_NAME = "KEY_DEVICE_NAME";
    public static final String KEY_DEVICE_ADDRESS = "KEY_DEVICE_ADDRESS";

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
        return filter;
    }
}
