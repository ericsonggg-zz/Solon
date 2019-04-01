package com.bme.solon;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.bme.solon.bluetooth.BluetoothManager;
import com.bme.solon.bluetooth.BluetoothService;
import com.bme.solon.bluetooth.BluetoothUnsupportedException;
import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Device;
import com.bme.solon.database.Instance;

import java.util.ArrayList;
import java.util.List;

/**
 * Initial loading screen. Performs all necessary startup tasks.
 */
public class SplashActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "SplashActivity";

    private ServiceConnection btServiceConnection;
    private boolean isServiceBound;
    private BluetoothService btService;

    private DatabaseHelper db;
    private BluetoothManager btManager;

    private List<AsyncTask> tasks = new ArrayList<>();
    private boolean doBluetoothTask;
    private GetPermissionsAsync permissionLocationTask;

    /**
     * Initializes singleton variables.
     * Create notification channel for Anroid Oreo and higher.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Log.d(TAG, "onCreate");

        //Start the service
        Intent intent = new Intent(this, BluetoothService.class);
        startService(intent);

        db = DatabaseHelper.getInstance(this);

        // Create the NotificationChannel, but only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Service Channel
            CharSequence name = getString(R.string.channel_service_name);
            String description = getString(R.string.channel_service_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(BluetoothService.NOTIFICATION_SERVICE_CHANNEL, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null); //disable sound

            //Register service channel
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "onCreate: started service notification channel");

            //Instance channel
            name = getString(R.string.channel_instance_name);
            description = getString(R.string.channel_instance_description);
            importance = NotificationManager.IMPORTANCE_MAX;
            channel = new NotificationChannel(BluetoothService.NOTIFICATION_INSTANCE_CHANNEL, name, importance);
            channel.setDescription(description);
            channel.shouldShowLights();
            channel.enableLights(true);
            channel.shouldVibrate();
            channel.enableVibration(true);

            notificationManager.createNotificationChannel(channel);
            Log.d(TAG, "onCreate: started instance notification channel");
        }

        //Set version name
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            TextView version = findViewById(R.id.splash_version);
            version.setText(String.format(getString(R.string.splash_version), pinfo.versionName));
        } catch (PackageManager.NameNotFoundException | NullPointerException ignored) {}
    }

    /**
     * Bind to {@link BluetoothService}.
     * If Bluetooth is unsupported, notifies user and quits app.
     * If Bluetooth is off, prompts user to turn it on.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        //service bound listener
        btServiceConnection = new ServiceConnection() {
            /**
             * On connection (via bindService()).
             * @param iBinder       {@link BluetoothService.Binder}
             */
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected: " + componentName.flattenToString());
                BluetoothService.Binder binder = (BluetoothService.Binder) iBinder;
                btService = binder.getService();
                isServiceBound = true;
            }

            /**
             * On crash or disconnect
             * TODO: auto-reconnect
             */
            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected: " + componentName.flattenToString());
                btService = null;
                isServiceBound = false;
            }
        };

        //bind to service
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, btServiceConnection, Context.BIND_AUTO_CREATE);

        //Turn on bluetooth if supported by phone
        try {
            btManager = BluetoothManager.getInstance();
            BluetoothAdapter bluetoothAdapter = btManager.getAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BluetoothManager.REQUEST_ENABLE_BT);
            }
            else {
                startupTasks();
            }
        }
        catch (BluetoothUnsupportedException e) {     //Exit app since unsupported
            AlertDialog.Builder builder = new AlertDialog.Builder(this);    //Dialog to show
            builder.setMessage(R.string.splash_bluetooth_unsupported_message)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            android.os.Process.killProcess(android.os.Process.myPid()); //kill app
                        }
                    });
            builder.create().show();

            Log.e(TAG, "onStart: phone does not support Bluetooth");
        }
    }

    /**
     * Unbind the service
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");

        if (isServiceBound) {
            Log.d(TAG, "onStop: unbinding service");
            unbindService(btServiceConnection);
            btService = null;
            isServiceBound = false;
        }
    }

    /**
     * Check whether user enabled Bluetooth if previously turned off.
     * @param requestCode       Intent request code
     * @param resultCode        Result code of the intent
     * @param data              Extras
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (requestCode == BluetoothManager.REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Log.d(TAG, "onActivityResult: Bluetooth enable request denied");
                Toast.makeText(this, R.string.splash_bluetooth_disabled_toast, Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d(TAG, "onActivityResult: Bluetooth enable request approved");
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
       startupTasks();
    }

    /**
     * Manually get permissions for Android M or higher.
     * Callback is onRequestPermissionsResult()
     */
    private void getPermissionsTask() {
        //Android M Permission check 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.v(TAG, "getPermissionsTask: Android version is M+");

            //Location for Bluetooth scan
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "getPermissionsTask: requesting location");

                //Info Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.splash_permission_info_title)
                        .setMessage(R.string.splash_permission_location_message)
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        })
                        .create().show();

                permissionLocationTask = new GetPermissionsAsync();
                tasks.add(permissionLocationTask);
            }
        }
        Log.d(TAG,"getPermissionsTask: complete");
    }

    /**
     * From https://developer.radiusnetworks.com/2015/09/29/is-your-beacon-app-ready-for-android-6.html
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "onRequestPermissionsResult: Coarse location permission granted");
                } else {
                    Toast.makeText(this, R.string.splash_permission_disabled_toast, Toast.LENGTH_SHORT).show();
                }
                permissionLocationTask.cancel(true);
                break;
            }
        }
    }

    /**
     * Runs all startup tasks.
     */
    private void startupTasks() {
        Log.d(TAG,"executing all startupTasks");
        //TODO: add all remaining AsyncTasks
        getPermissionsTask();

        //TODO: remove cause testing
        //addDevices();
        //addInstances();
        //killService();
        //db.wipeData();

        ChangeActivityAsync finalTask = new ChangeActivityAsync(this, tasks);
        finalTask.execute();
    }

    private void addDevices() {
        db.addActiveDevice(new Device("TEST1", "00:11:22:33:FF:ED"));
        db.addActiveDevice(new Device("TEST2", "00:11:22:33:FF:EE"));
        db.addActiveDevice(new Device("TEST3", "00:11:22:33:FF:EF"));
    }

    private void addInstances() {
        db.addInstance(new Instance(3, 1));
    }

    private void killService() {
        btService.stopSelf();
    }
}
