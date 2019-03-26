package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.widget.TextView;

import com.bme.solon.R;

import java.io.IOException;

/**
 * AsyncTask that tries to connect to the specified device in the socket.
 * Updates dialog on completion.
 */
public class ConnectAsync extends AsyncTask<TextView, Void, Boolean> {
    private BluetoothSocket socket;
    private TextView view;      //No chance of leakage since we wait for this to finish before killing activity

    /**
     * Class constructor
     * @param socket        Socket to try to connect to
     */
    public ConnectAsync(BluetoothSocket socket) {
        this.socket = socket;
    }

    /**
     * Only try to connect the first socket in the list
     * @return      True if connect was successful
     */
    protected Boolean doInBackground(TextView... views) {
        if (views.length > 0) {
            this.view = views[0];
        }

        try {
            socket.connect();
            return true;
        }
        catch (IOException exception) {
            try {
                socket.close(); //close socket on failure
            } catch (IOException ignored) {}
        }
        return false;
    }

    /**
     * Update dialog message on completion, if exists.
     * @param connectStatus     Connection status from doInBackground()
     */
    protected void onPostExecute(Boolean connectStatus) {
        if (view != null) {
            if (connectStatus) {
                view.setText(R.string.splash_progress_connect_successful);
            }
            else {
                view.setText(R.string.splash_progress_connect_unsuccessful);
            }
        }
    }
}
