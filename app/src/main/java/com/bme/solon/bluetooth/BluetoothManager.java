package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {
    public static final int REQUEST_ENABLE_BT = 10000; //Const for enable bluetooth intent
    public static final UUID DEVICE_UUID = UUID.randomUUID(); //Const for device's Bluetooth UUID. Must match the hardware.

    private static final String TAG = "BluetoothManager";

    private static BluetoothManager singleton;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket deviceSocket;

    /**
     * Private constructor
     * @throws BluetoothUnsupportedException    If the phone does not support Bluetooth
     */
    private BluetoothManager() throws BluetoothUnsupportedException {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e(TAG, "constructor: Device does not support Bluetooth");
            throw new BluetoothUnsupportedException("Device does not support Bluetooth");
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
        Log.d(TAG, "queryPaired: " + deviceName + " was not found in paired list");
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

    /**
     * Return an ASyncTask that attempts to connect to the {@link BluetoothDevice}
     * on execute().
     * @param device        The device to connect to
     * @return              An ASyncTask for starting a connection
     * @throws IOException  If a {@link BluetoothSocket} could not be created from the device.
     */
    public ConnectAsync connectToDevice(BluetoothDevice device) throws IOException {
        stopDiscovery();
        deviceSocket = device.createRfcommSocketToServiceRecord(DEVICE_UUID);
        return new ConnectAsync(deviceSocket);
    }

    /**
     * Get the input stream from the connected device to read bytes from.
     * @return              The InputStream of the connected device.
     * @throws IOException  If the InputStream could not be created.
     */
    public InputStream getInputStream() throws IOException {
        if (deviceSocket != null) {
            return deviceSocket.getInputStream();
        }
        else {
            Log.d(TAG, "getInputStream: socket is null");
            return null;
        }
    }

    /**
     * Cancel the Bluetooth connection if it exists.
     * @return      True if the connection was successfully disconnected or if there was no existing connection.
     */
    public boolean cancelConnection() {
        if (deviceSocket != null) {
            try {
                deviceSocket.close();
                deviceSocket = null;
            }
            catch (IOException e) {
                Log.e(TAG, "cancelConnection failed: " + e.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Get the singleton instance of this bluetooth manager. If none exists, create one.
     * @return      BluetoothManager singleton
     * @throws BluetoothUnsupportedException   If device doesn't support Bluetooth
     */
    public static synchronized BluetoothManager getInstance() throws BluetoothUnsupportedException {
        if (singleton == null) {
            singleton = new BluetoothManager();
        }
        return singleton;
    }
}
