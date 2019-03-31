package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.util.Set;

public class BluetoothManager {
    public static final int REQUEST_ENABLE_BT = 10000; //Const for enable bluetooth intent
    public static final ParcelUuid HARDWARE_UUID = ParcelUuid.fromString("0000dfb0-0000-1000-8000-00805f9b34fb");

    private static final String TAG = "BluetoothManager";

    private static BluetoothManager singleton;
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothGatt gatt;
    private int gattStatus;

    private BluetoothGattCallback bluetoothCallback =  new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange: newState=" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
            }
            gattStatus = newState;
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered");
            for (BluetoothGattService service : gatt.getServices()) {
                Log.d(TAG, "ohno " +service.getUuid().toString());
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    Log.d(TAG,"chr " + characteristic.getUuid().toString());
                    if (characteristic.getUuid().toString().equals("0000dfb1-0000-1000-8000-00805f9b34fb")){
                        Log.d(TAG, "set");
                        gatt.setCharacteristicNotification(characteristic, true);
                        gatt.readCharacteristic(characteristic);
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead");
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, new String (characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, new String (characteristic.getValue()));
        }
    };
    
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
     * @param device        Device to connect to
     * @param context       Calling context
     */
    void connectToDevice(BluetoothDevice device, Context context) {
        Log.d(TAG, "connectToDevice: " + device.getName() + " with address " + device.getAddress() + " from context " + context.toString());
        synchronized (this) {
            gatt = device.connectGatt(context, true, bluetoothCallback);
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
