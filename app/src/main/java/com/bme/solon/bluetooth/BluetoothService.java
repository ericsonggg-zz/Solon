package com.bme.solon.bluetooth;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bme.solon.R;
import com.bme.solon.SplashActivity;
import com.bme.solon.database.Device;

import java.util.Map;

public class BluetoothService extends Service {
    public static final String THREAD_NAME = "BluetoothService";
    public static final String NOTIFICATION_CHANNEL = "SolonService";
    public static final int NOTIFICATION_ID = 39573;
    public static final String TAG = "BluetoothService";

    private Looper looper;
    private Handler handler;
    private IBinder binder = new Binder();

    private BluetoothManager btManager;

    /**
     * Thread handler to post Runnables or Messages.
     */
    private final class Handler extends android.os.Handler {
        Handler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            //stopSelf(message.arg1); //??? dunno
        }
    }

    /**
     * Binder for UI classes to bind & talk with {@link BluetoothService}
     */
    public class Binder extends android.os.Binder {
        /**
         * Return the bound {@link BluetoothService}
         * @return      Bound BluetoothService
         */
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        btManager = BluetoothManager.getInstance();

        //Start worker thread
        HandlerThread thread = new HandlerThread(THREAD_NAME, Process.THREAD_PRIORITY_FOREGROUND);
        thread.start();
        looper = thread.getLooper();
        handler = new Handler(looper);

        //start foreground
        Intent intent = new Intent(this, SplashActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentTitle(getText(R.string.app_name))
                .setContentText("TEMPORARY")
                .setContentIntent(pendingIntent)
                .build();       //TODO: add ticker & proper notification

        startForeground(NOTIFICATION_ID, notification);

        //TODO: START BT
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        return START_STICKY;
    }

    /**
     * Allow UI to bind to service via {@link BluetoothService.Binder}
     * @return      a BluetoothService.Binder
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopForeground(true);
        //TODO: destroy
    }

    /**
     * Check if Bluetooth is on
     * @return      True if enabled
     */
    public boolean isBluetoothOn() {
        return btManager.isBluetoothOn();
    }

    /**
     * Start Bluetooth discovery with scanCallback
     * @param scanCallback  Callback listener
     */
    public void startDiscovery(ScanCallback scanCallback) {
        Log.d(TAG, "startDiscovery; posting task with callback " + scanCallback.toString());
        handler.post(() -> {
            Log.d(TAG, "startDiscovery: running task with callback " + scanCallback.toString());
            btManager.startDiscovery(scanCallback);
        });
    }

    /**
     * Stop Bluetooth discovery identified by scanCallback
     * @param scanCallback  Discovery to stop
     */
    public void stopDiscovery(ScanCallback scanCallback) {
        Log.d(TAG, "stopDiscovery: posting task with callback " + scanCallback.toString());
        handler.post(() -> {
            Log.d(TAG, "stopDiscovery: running task with callback " + scanCallback.toString());
            btManager.stopDiscovery(scanCallback);
        });
    }

    /**
     * Start a connection to the Bluetooth device described by the deviceData.
     * The deviceData must describe a previously paired BluetoothDevice
     *
     * @param deviceData    Map containing the name and address of the BluetoothDevice to connect to.
     */
    public void connectToDevice(Map<String, String> deviceData) {
        Log.d(TAG, "connectToDevice: posting task with Map " + deviceData.toString());
        handler.post(() -> {
            Log.d(TAG, "connectToDevice: running task with Map " + deviceData.toString());
            BluetoothDevice device = btManager.queryPaired(deviceData.get(Device.COLUMN_NAME), deviceData.get(Device.COLUMN_ADDRESS));
            btManager.connectToDevice(device, this);
        });
    }

    /**
     * Start a connection to the BluetoothDevice
     * @param device    Device to connect with
     */
    public void connectToDevice(BluetoothDevice device) {
        Log.d(TAG, "connectToDevice: posting task with BluetoothDevice " + device.toString());
        handler.post(() -> {
            Log.d(TAG, "connectToDevice: running task with BluetoothDevice " + device.toString());
            btManager.connectToDevice(device, this);
        });
    }
}
