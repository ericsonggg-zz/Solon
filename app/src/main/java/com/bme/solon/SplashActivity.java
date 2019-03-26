package com.bme.solon;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bme.solon.database.DatabaseHelper;
import com.bme.solon.database.Instance;

import com.bme.solon.bluetooth.BluetoothManager;
public class SplashActivity extends AppCompatActivity {
    private DatabaseHelper db;

    /**
     * Turns on bluetooth if currently off.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Turn on bluetooth
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothManager.getInstance().getAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BluetoothManager.REQUEST_ENABLE_BT);
            }
        }
        catch (BluetoothManager.BluetoothException e) {
            //TODO: Dialog & exit the app because its not compatible
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
            if (resultCode == RESULT_OK) {
                //TODO: continue
            }
            else {
                //TODO: continue but show Toast
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
