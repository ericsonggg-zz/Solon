package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothAdapter;

public class BluetoothManager {
    public static final int REQUEST_ENABLE_BT = 10000; //Const for enable bluetooth intent

    private static BluetoothManager singleton;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothManager() throws BluetoothException {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            throw new BluetoothException("Device does not support Bluetooth");
        }
    }

    public BluetoothAdapter getAdapter() {
        return bluetoothAdapter;
    }

    /**
     * Get the singleton instance of this bluetooth manager. If none exists, create one.
     * @return                      BluetoothManager singleton
     * @throws BluetoothException   If device doesn't support Bluetooth
     */
    public static synchronized BluetoothManager getInstance() throws BluetoothException {
        if (singleton == null) {
            singleton = new BluetoothManager();
        }
        return singleton;
    }

    public class BluetoothException extends Exception {
        public BluetoothException(String message) {
            super(message);
        }
    }
}
