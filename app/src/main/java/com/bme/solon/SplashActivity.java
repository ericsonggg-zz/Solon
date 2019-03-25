package com.bme.solon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bme.solon.bluetooth.BluetoothUnsupportedException;
import com.bme.solon.bluetooth.ConnectAsync;
import com.bme.solon.database.DatabaseHelper;

import com.bme.solon.bluetooth.BluetoothManager;
import com.bme.solon.database.Device;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    private DatabaseHelper db;
    private BluetoothManager btManager;

    private List<AsyncTask> tasks = new ArrayList<>();
    private boolean doBluetoothTask;

    /**
     * Turns on bluetooth if currently off.
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

    private void startupTasks() {
        if (doBluetoothTask) {
            bluetoothTask();
        }

        //TODO: add all remaining AsyncTasks

        ChangeActivityAsync finalTask = new ChangeActivityAsync(this, tasks);
        finalTask.execute();
        Log.d(TAG,"done");
    }

    private void bluetoothTask() {
        Map<String, String> activeDevice = db.getActiveDevice();
        if (!activeDevice.isEmpty()) {
            BluetoothDevice device = btManager.queryPaired(activeDevice.get(Device.COLUMN_NAME), activeDevice.get(Device.COLUMN_ADDRESS));

            TextView progressText = findViewById(R.id.splash_progress_text);
            try {
                ConnectAsync connectTask = btManager.connectToDevice(device);
                tasks.add(connectTask);
                connectTask.execute(progressText);
            }
            catch (IOException e) {
                Log.e(TAG,"onActivityResult: error connecting to device: " + e.toString());
            }
        }
    }
}
