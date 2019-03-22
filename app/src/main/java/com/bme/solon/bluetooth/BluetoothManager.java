package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {
    public static final int REQUEST_ENABLE_BT = 10000; //Const for enable bluetooth intent
    public static final UUID DEVICE_UUID = UUID.randomUUID(); //Const for device's Bluetooth UUID. Must match the hardware.

    private static BluetoothManager singleton;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothManager() throws BluetoothException {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            throw new BluetoothException("Device does not support Bluetooth");
        }
    }

    /**
     * Get the adapter object
     * @return  A BluetoothAdapter
     */
    public BluetoothAdapter getAdapter() {
        return bluetoothAdapter;
    }

    /**
     * Check if the specified device has previously been paired.
     * @param deviceName        Name of Bluetooth device
     * @param deviceAddress     MAC address of Bluetooth device
     * @return      The paired device if exists. Otherwise, null.
     */
    public BluetoothDevice queryPaired(String deviceName, String deviceAddress) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName) && device.getAddress().equals(deviceAddress)) {
                    return device;
                }
            }
        }
        return null;
    }

    /**
     * Start Bluetooth discovery for new devices.
     * Activities should register a BroadcastReceiver to obtain info on found devices.
     * @return      True if discovery was successfully started.
     */
    public boolean startDiscovery() {
        return bluetoothAdapter.startDiscovery();
    }

    /**
     * Stop Bluetooth discovery for new devices. Always stop discovery as it consumes a lot of resources.
     * Activities should unregister any BroadcastReceiver when calling this method.
     * @return      True if discovery was successfully stopped.
     */
    public boolean stopDiscovery() {
        return bluetoothAdapter.cancelDiscovery();
    }

    public AsyncConnect connectToDevice(BluetoothDevice device) throws IOException {
        stopDiscovery();
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(DEVICE_UUID);
        return new AsyncConnect(socket);
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
