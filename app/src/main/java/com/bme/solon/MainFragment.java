package com.bme.solon;

import android.support.v4.app.Fragment;

import com.bme.solon.bluetooth.BluetoothService;

/**
 * Parent Fragment class for all fragments used in {@link MainActivity}.
 * Holds and updates references to bound {@link BluetoothService}.
 */
public abstract class MainFragment extends Fragment {
    protected boolean isServiceBound;
    protected BluetoothService btService;

    /**
     * On connection
     */
    public void onServiceConnected(BluetoothService service) {
        btService = service;
        isServiceBound = true;
    }

    /**
     * On crash or disconnect
     * TODO: auto-reconnect
     */
    public void onServiceDisconnected() {
        btService = null;
        isServiceBound = false;
    }

    /**
     * On unbinding
     */
    public void onServiceUnbound() {
        btService = null;
        isServiceBound = false;
    }
}
