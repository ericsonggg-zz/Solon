package com.bme.solon.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.bme.solon.R;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * AsyncTask that tries to connect to the specified device in the socket.
 * Updates dialog on completion.
 */
public class ConnectAsync extends AsyncTask<Context, Void, Boolean> {
    private BluetoothSocket socket;
    private WeakReference<Context> context;

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
    @Override
    protected Boolean doInBackground(Context... contexts) {
        if (contexts.length > 0) {
            this.context = new WeakReference<>(contexts[0]);
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
    @Override
    protected void onPostExecute(Boolean connectStatus) {
        if (context != null && context.get() != null) {
            if (connectStatus) {
                Toast.makeText(context.get(), R.string.splash_progress_connect_successful, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context.get(), R.string.splash_progress_connect_unsuccessful, Toast.LENGTH_LONG).show();
            }
        }
    }
}
