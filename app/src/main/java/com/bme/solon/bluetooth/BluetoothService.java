package com.bme.solon.bluetooth;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bme.solon.MainActivity;
import com.bme.solon.R;
import com.bme.solon.database.Device;

public class BluetoothService extends Service {
    public static final String THREAD_NAME = "BluetoothService";
    public static final String NOTIFICATION_SERVICE_CHANNEL = "SolonService";
    public static final String NOTIFICATION_INSTANCE_CHANNEL = "SolonInstance";
    public static final int NOTIFICATION_SERVICE_ID = 39573;
    public static final int NOTIFICATION_INSTANCE_ID = 40219;
    public static final long SNOOZE_DELAY_MILLIS = 15000; //900000;

    public static final String HARDWARE_SERIAL_UUID = "0000dfb1-0000-1000-8000-00805f9b34fb";
    public static final String HARDWARE_COMMAND_UUID = "0000dfb2-0000-1000-8000-00805f9b34fb";
    public static final String HARDWARE_MODEL_NUMBER_UUID = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String TAG = "BluetoothService";

    private Looper looper;
    private Handler handler;
    private IBinder binder = new Binder();

    private BluetoothManager btManager;

    private Device device;
    private BluetoothGatt gattClient;
    private int gattStatus; //BluetoothProfile.STATE_###
    private BluetoothGattCharacteristic serialChar;
    private BluetoothGattCharacteristic commandChar;
    private BluetoothGattCharacteristic modelChar;

    /**
     * Callback listener for an active connection.
     */
    private final BluetoothGattCallback bluetoothCallback =  new BluetoothGattCallback() {
        /**
         * Start discovering services if connected.
         * @param gatt      GATT client
         * @param status    Status of operation
         * @param newState  New state
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.w(TAG, "onConnectionStateChange: error occurred, disconnecting");
                gatt.disconnect();
            }
            else {
                Log.d(TAG, "onConnectionStateChange: newState = " + newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    broadcastConnected();
                    gatt.discoverServices();
                } else {
                    broadcastDisconnected();
                }
                gattStatus = newState;
            }
        }

        /**
         * Store all appropriate characteristics on success.
         * If failed, retry service discovery.
         * @param gatt      GATT client
         * @param status    GATT_SUCCESS if device was successfully explored.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered: status = " + status);

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.w(TAG, "onServicesDiscovered: retrying discovery");
                gatt.discoverServices();
            }
            else {
                for (BluetoothGattService service : gatt.getServices()) {
                    Log.v(TAG, "onServicesDiscovered: service uuid = " + service.getUuid().toString());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        Log.v(TAG, "onServicesDiscovered: characteristic uuid = " + characteristic.getUuid().toString());
                        switch (characteristic.getUuid().toString()) {
                            case HARDWARE_SERIAL_UUID:
                                Log.v(TAG, "onServicesDiscovered: found Serial Port characteristic");
                                serialChar = characteristic;
                                break;
                            case HARDWARE_COMMAND_UUID:
                                Log.v(TAG, "onServicesDiscovered: found Command characteristic");
                                commandChar = characteristic;
                                break;
                            case HARDWARE_MODEL_NUMBER_UUID:
                                Log.v(TAG, "onServicesDiscovered: found Model Number characteristic");
                                modelChar = characteristic;
                                break;
                        }
                    }
                }

                if (serialChar != null) {
                    Log.d(TAG, "onServicesDiscovered: enabling Serial Port notification channel");
                    gatt.setCharacteristicNotification(serialChar, true);
                    gatt.readCharacteristic(serialChar);
                }
                else {
                    Log.w(TAG, "onServicesDiscovered: no service had a Serial Port characteristic, disconnecting");
                    gatt.disconnect();

                    serialChar = null;
                    commandChar = null;
                    modelChar = null;
                }
            }
        }

        /**
         * Read loaded messages from the device. If operation failed, retry.
         * @param gatt              GATT client
         * @param characteristic    Characteristic to be read
         * @param status            GATT_SUCCESS if read operation was done properly
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead: status = " + status);

            if (status == BluetoothGatt.GATT_FAILURE) {
                Log.w(TAG, "onCharacteristicRead: retrying read");
                gatt.readCharacteristic(serialChar);
            }
            else {
                processCharacteristic(characteristic.getValue());
            }
        }

        /**
         * Read changed message from the device.
         * @param gatt              GATT client
         * @param characteristic    Characteristic to be read
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged");
            processCharacteristic(characteristic.getValue());
        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: action = " + intent.getAction());

            //cancel notification
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.cancel(NOTIFICATION_INSTANCE_ID);

            switch (intent.getAction()) {
                case BluetoothBroadcast.ACTION_ADDRESS: //start activity
                    Intent activityIntent = new Intent(context, MainActivity.class);
                    startActivity(activityIntent);
                    break;
                case BluetoothBroadcast.ACTION_SNOOZE: //notify again later
                    Log.d(TAG,"onReceive: posting snooze task");
                    handler.postDelayed(() -> {
                        Log.d(TAG, "onReceive: running snooze task");
                        notifyUser();
                    }, SNOOZE_DELAY_MILLIS);
            }
        }
    };

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
        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, NOTIFICATION_SERVICE_CHANNEL)
                .setContentTitle(getText(R.string.app_name))
                .setContentText("TEMPORARY")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);       //TODO: add ticker & proper notification

        startForeground(NOTIFICATION_SERVICE_ID, notification.build());

        //register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothBroadcast.ACTION_ADDRESS);
        filter.addAction(BluetoothBroadcast.ACTION_SNOOZE);
        registerReceiver(broadcastReceiver, filter);

        //TODO: START BT
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        notifyUser();
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
        if (gattClient != null) {
            gattClient.close();
            gattClient = null;
        }
    }

    /**
     * Check if Bluetooth is on
     * @return      True if enabled
     */
    public boolean isBluetoothOn() {
        return btManager.isBluetoothOn();
    }

    /**
     * Retrieve the status of the connection.
     * @return  the BluetoothProfile.STATE_###
     */
    public int getGattStatus() {
        return gattStatus;
    }

    /**
     * Retrieve the connected Bluetooth device
     * @return  the Bluetooth device or null if not connected.
     */
    public BluetoothDevice getConnectedDevice() {
        if (gattClient == null) {
            return null;
        }
        return gattClient.getDevice();
    }

    /**
     * Enable Bluetooth
     */
    public void enableBluetooth() {
        Log.d(TAG, "enableBluetooth; posting task");
        handler.post(() -> {
            Log.d(TAG, "enableBluetooth; running task");
            btManager.enableBlueooth();
        });
    }

    /**
     * Disable Bluetooth
     */
    public void disableBluetooth() {
        Log.d(TAG, "disableBluetooth; posting task");
        handler.post(() -> {
            Log.d(TAG, "disableBluetooth; running task");
            notifyUser();
            //btManager.disableBluetooth();
        });
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
    public void connectToDevice(Device deviceData) {
        Log.d(TAG, "connectToDevice: posting task with data " + deviceData.toString());
        handler.post(() -> {
            Log.d(TAG, "connectToDevice: running task with data " + deviceData.toString());
            BluetoothDevice device = btManager.queryPaired(deviceData.getName(), deviceData.getAddress());
            if (device != null) {
                Log.d(TAG,"connectToDevice: paired device found");
                gattClient = btManager.connectToDevice(device, this, bluetoothCallback);
                broadcastConnecting(device);
            }
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
            gattClient = btManager.connectToDevice(device, this, bluetoothCallback);
            broadcastConnecting(device);
        });
    }

    /**
     * Parse characteristic value into a {@link com.bme.solon.strip.StripStatus} and submit into the database.
     * Alert all UI elements with a broadcast.
     * @param value     Characteristic value.
     */
    private void processCharacteristic(byte[] value) {

        //TODO: replace with reading from StripStatus
        if (true) {
            notifyUser();
        }
    }

    private void notifyUser() {
        Log.d(TAG, "notifyUser");

        //Fullscreen notification
        Intent notifIntent = new Intent(this, MainActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent notifPdIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);

        //Address now
        Intent addressIntent = new Intent(BluetoothBroadcast.ACTION_ADDRESS);
        PendingIntent addressPdIntent = PendingIntent.getBroadcast(this, 0, addressIntent, 0);

        //Snooze
        Intent snoozeIntent = new Intent(BluetoothBroadcast.ACTION_SNOOZE);
        PendingIntent snoozePdIntent = PendingIntent.getBroadcast(this, 0, snoozeIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_INSTANCE_CHANNEL)
                .setSmallIcon(R.mipmap.logo_round)
                .setContentTitle(getString(R.string.notif_incontinence_title))
//                .setContentText(String.format(getString(R.string.notif_incontinence_message), device.getAppName()))
                .setContentText(String.format(getString(R.string.notif_incontinence_message), "TEST_NAME"))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[] {1000})
                .setLights(Color.RED, 3000, 3000)
                .setFullScreenIntent(notifPdIntent, true)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_checkmark, getString(R.string.notif_address_message), addressPdIntent)
                .addAction(R.drawable.ic_clock_plus, getString(R.string.notif_snooze_message), snoozePdIntent)
                .build();
        notification.flags |= Notification.FLAG_INSISTENT; //make sound constantly play

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(NOTIFICATION_INSTANCE_ID, notification);
    }

    private void broadcast(int state) {
        final Intent intent = new Intent();

    }

    private void broadcastConnecting (BluetoothDevice device) {
        Log.v(TAG, "broadcastConnecting: device " + device.getName() + " " + device.getAddress());
        final Intent intent = new Intent(BluetoothBroadcast.ACTION_CONNECTING);
        intent.putExtra(BluetoothBroadcast.KEY_DEVICE_NAME, device.getName());
        intent.putExtra(BluetoothBroadcast.KEY_DEVICE_ADDRESS, device.getAddress());

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastConnected() {
        Log.v(TAG, "broadcastConnecting: update only");
        final Intent intent = new Intent(BluetoothBroadcast.ACTION_CONNECTED_UPDATE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastConnected (BluetoothDevice device) {
        Log.v(TAG, "broadcastConnected: device " + device.getName() + " " + device.getAddress());
        final Intent intent = new Intent(BluetoothBroadcast.ACTION_CONNECTED);
        intent.putExtra(BluetoothBroadcast.KEY_DEVICE_NAME, device.getName());

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastDisconnected () {
        Log.v(TAG, "broadcastDisconnected");
        final Intent intent = new Intent(BluetoothBroadcast.ACTION_DISCONNECTED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
