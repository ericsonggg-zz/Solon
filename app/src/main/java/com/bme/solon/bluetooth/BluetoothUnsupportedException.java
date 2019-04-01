package com.bme.solon.bluetooth;

/**
 * Unchecked exception class if Bluetooth is unsupported by the phone.
 * Unchecked because only the first caller ({@link com.bme.solon.SplashActivity}) should always
 * crash the app.
 */
public class BluetoothUnsupportedException extends RuntimeException {

    /**
     * Default constructor
     * @param message       Reason for exception
     */
    public BluetoothUnsupportedException(String message) {
        super(message);
    }
}
