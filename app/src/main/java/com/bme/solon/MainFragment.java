package com.bme.solon;

import android.content.Intent;
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

    /**
     * Override to make UI changes based on {@link BluetoothService} broadcasts.
     * @param intent    The broadcasted intent.
     */
    abstract protected void receiveBroadcast(Intent intent);
}
