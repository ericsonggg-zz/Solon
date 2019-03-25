package com.bme.solon.bluetooth;

/**
 * Exception class if Bluetooth is unsupported by the phone.
 */
public class BluetoothUnsupportedException extends Exception {

    /**
     * Default constructor
     * @param message       Reason for exception
     */
    public BluetoothUnsupportedException(String message) {
        super(message);
    }
}
