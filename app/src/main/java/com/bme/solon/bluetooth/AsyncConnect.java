package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import java.io.IOException;

/**
 * AsyncTask that tries to connect to the specified device in the socket.
 * Updates dialog on completion.
 */
public class AsyncConnect extends AsyncTask<AlertDialog, Void, Boolean> {
    private BluetoothSocket socket;
    private AlertDialog dialog;

    /**
     * Class constructor
     * @param socket        Socket to try to connect to
     */
    public AsyncConnect(BluetoothSocket socket) {
        this.socket = socket;
    }

    /**
     * Only try to connect the first socket in the list
     * @return      True if connect was successful
     */
    protected Boolean doInBackground(AlertDialog... dialogs) {
        if (dialogs.length > 0) {
            this.dialog = dialogs[0];
        }

        try {
            socket.connect();
            return new Boolean(true);
        }
        catch (IOException exception) {
            try {
                socket.close(); //close socket on failure
            } catch (IOException ignored) {
            }
        }
        return new Boolean(false);
    }

    /**
     * Update dialog message on completion.
     * @param connectStatus     Connection status from doInBackground()
     */
    protected void onPostExecute(Boolean connectStatus) {
        if (dialog != null) {
            if (connectStatus) {
                dialog.setMessage("The connection was successful!");
            }
            else {
                dialog.setMessage("The connection failed.");
            }
        }
    }
}
