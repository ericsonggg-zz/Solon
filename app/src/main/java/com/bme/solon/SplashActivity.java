package com.bme.solon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.bme.solon.bluetooth.BluetoothManager;
import com.bme.solon.bluetooth.BluetoothUnsupportedException;
import com.bme.solon.bluetooth.ConnectAsync;
import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Initial loading screen. Performs all necessary startup tasks.
 */
public class SplashActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "SplashActivity";

    private DatabaseHelper db;
    private BluetoothManager btManager;

    private List<AsyncTask> tasks = new ArrayList<>();
    private boolean doBluetoothTask;
    private GetPermissionsAsync permissionTask;

    /**
     * Initializes singleton variables.
     * If Bluetooth is unsupported, notifies user and quits app.
     * If Bluetooth is off, prompts user to turn it on.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        db = DatabaseHelper.getInstance(this);

        //Turn on bluetooth if supported by phone
        try {
            btManager = BluetoothManager.getInstance();
            BluetoothAdapter bluetoothAdapter = btManager.getAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BluetoothManager.REQUEST_ENABLE_BT);
            }
            else {
                doBluetoothTask = true;
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
                Toast.makeText(this, R.string.splash_bluetooth_disabled_toast, Toast.LENGTH_LONG).show();
                doBluetoothTask = false;
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
            doBluetoothTask = true;
        }
       startupTasks();
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
                    Log.d(TAG, "Coarse location permission granted");
                } else {
                    Toast.makeText(this, R.string.splash_permission_disabled_toast, Toast.LENGTH_SHORT).show();
                }
                permissionTask.cancel(true);
                break;
            }
        }
    }

    private void getPermissionsTask() {
        //Manually get permissions for Version M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Info Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.splash_permission_info_title)
                        .setMessage(R.string.splash_permission_info_message)
                        .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        })
                        .create().show();

                permissionTask = new GetPermissionsAsync();
                tasks.add(permissionTask);
            }
        }
    }

    /**
     * Run startup Bluetooth task.
     * Try to connect to "active" device, if exists.
     */
    private void bluetoothTask() {
        Map<String, String> activeDevice = db.getActiveDevice();
        if (!activeDevice.isEmpty()) {
            BluetoothDevice device = btManager.queryPaired(activeDevice.get(Device.COLUMN_NAME), activeDevice.get(Device.COLUMN_ADDRESS));

            try {
                ConnectAsync connectTask = btManager.connectToDevice(device);
                tasks.add(connectTask);
                connectTask.execute(this);
            }
            catch (IOException e) {
                Log.e(TAG,"onActivityResult: error connecting to device: " + e.toString());
            }
        }
    }

    /**
     * Runs all startup tasks.
     */
    private void startupTasks() {
        if (doBluetoothTask) {
            bluetoothTask();
        }

        //TODO: add all remaining AsyncTasks
        getPermissionsTask();

        ChangeActivityAsync finalTask = new ChangeActivityAsync(this, tasks);
        finalTask.execute();
        Log.d(TAG,"done");
    }
}
