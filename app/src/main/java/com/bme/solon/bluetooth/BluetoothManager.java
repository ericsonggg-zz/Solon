package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Set;

public class BluetoothManager {
    public static final int REQUEST_ENABLE_BT = 10000; //Const for enable bluetooth intent
    public static final ParcelUuid HARDWARE_UUID = ParcelUuid.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");


    private static final String TAG = "BluetoothManager";

    private static BluetoothManager singleton;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt gattClient;
    
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
    
    /**
     * Get the adapter object
     * @return  A BluetoothAdapter
     */
    public BluetoothAdapter getAdapter() {
        return bluetoothAdapter;
    }

    /**
     * Check if Bluetooth is on
     * @return      True if enabled
     */
    boolean isBluetoothOn() {
        return bluetoothAdapter.isEnabled();
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
     * @param scanCallback      Callback listener
     */
    void startDiscovery(ScanCallback scanCallback) {
        bluetoothAdapter.getBluetoothLeScanner().startScan(null, new ScanSettings.Builder().build(), scanCallback);
    }

    /**
     * Stop Bluetooth discovery for new devices.
     * Activities should unregister any BroadcastReceiver when calling this method. TODO: change?
     * @param scanCallback      Callback listener for the scan to cancel
     */
    void stopDiscovery(ScanCallback scanCallback) {
        bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
    }

    /**
     * Starts auto connection to Bluetooth device via GATT.
     * @param device            Device to connect to
     * @param context           Calling context
     * @param bluetoothCallback Callback listener
     */
    void connectToDevice(BluetoothDevice device, Context context, BluetoothGattCallback bluetoothCallback) {
        Log.d(TAG, "connectToDevice: " + device.getName() + " with address " + device.getAddress() + " from context " + context.toString());
        synchronized (this) {
            gattClient = device.connectGatt(context, true, bluetoothCallback);
        }
    }

    /**
     * Cancel the Bluetooth connection if it exists.
     * @return      True if the connection was successfully disconnected or if there was no existing connection.
     */
    public boolean cancelConnection() {
        return true;
    }
}
